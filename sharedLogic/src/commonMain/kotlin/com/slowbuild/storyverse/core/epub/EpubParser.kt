package com.slowbuild.storyverse.core.epub

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.openZip

data class EpubChapter(
    val index: Int,
    val title: String,
    val href: String,
    val paragraphs: List<String>,
    val wordCount: Int
)

data class ParsedEpub(
    val title: String,
    val author: String,
    val description: String?,
    val chapters: List<EpubChapter>,
    val coverPath: String? = null
)

class EpubParser(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    private val scriptRegex = Regex("<script\\b[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE)
    private val styleRegex = Regex("<style\\b[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE)
    private val xmlDeclRegex = Regex("<\\?xml[\\s\\S]*?\\?>", RegexOption.IGNORE_CASE)
    private val doctypeRegex = Regex("<!DOCTYPE[\\s\\S]*?>", RegexOption.IGNORE_CASE)
    private val commentRegex = Regex("<!--[\\s\\S]*?-->", RegexOption.IGNORE_CASE)
    private val htmlTagRegex = Regex("<[^>]*>")
    private val paragraphTagRegex = Regex("<(p|div|section|h[1-6])[^>]*>([\\s\\S]*?)</\\1>", RegexOption.IGNORE_CASE)
    private val breakTagRegex = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)

    fun parse(epubFilePath: Path): AppResult<ParsedEpub> {
        return try {
            val zipFs = fileSystem.openZip(epubFilePath)

            // 1. Locate container.xml
            val containerPath = "META-INF/container.xml".toPath()
            if (!zipFs.exists(containerPath)) {
                return AppResult.Error(AppError.Content("Tệp EPUB không hợp lệ (thiếu container.xml)"))
            }

            val containerXml = zipFs.read(containerPath) { this.readUtf8() }
            val opfRelativePath = extractOpfPath(containerXml)
                ?: return AppResult.Error(AppError.Content("Không tìm thấy đường dẫn content.opf trong EPUB"))

            val opfPath = opfRelativePath.toPath()
            if (!zipFs.exists(opfPath)) {
                return AppResult.Error(AppError.Content("Không tìm thấy tệp OPF: $opfRelativePath"))
            }

            val opfDir = opfPath.parent ?: "".toPath()
            val opfContent = zipFs.read(opfPath) { this.readUtf8() }

            // 2. Parse OPF metadata, manifest & spine
            val title = extractTagContent(opfContent, "dc:title") ?: "Truyện Không Tên"
            val author = extractTagContent(opfContent, "dc:creator") ?: "Khuyết Danh"
            val description = extractTagContent(opfContent, "dc:description")

            val manifest = extractManifest(opfContent)
            val spine = extractSpine(opfContent)

            // 3. Extract TOC (NCX if available)
            val ncxItem = manifest.values.find { it.href.endsWith(".ncx", ignoreCase = true) }
            val tocMap = mutableMapOf<String, String>()
            if (ncxItem != null) {
                val ncxPath = resolvePath(opfDir, ncxItem.href)
                if (zipFs.exists(ncxPath)) {
                    val ncxContent = zipFs.read(ncxPath) { this.readUtf8() }
                    parseNcx(ncxContent, tocMap)
                }
            }

            // 4. Extract chapters from spine
            val chapters = mutableListOf<EpubChapter>()
            var chapterIndex = 1

            for (idref in spine) {
                val manifestItem = manifest[idref] ?: continue
                val itemHref = manifestItem.href
                val chapterPath = resolvePath(opfDir, itemHref)

                if (!zipFs.exists(chapterPath)) continue

                val rawXhtml = zipFs.read(chapterPath) { this.readUtf8() }
                val (chapterTitle, paragraphs) = parseXhtmlContent(rawXhtml)

                if (paragraphs.isEmpty()) continue

                val finalTitle = tocMap[itemHref]
                    ?: tocMap[itemHref.substringAfterLast("/")]
                    ?: chapterTitle
                    ?: "Chương $chapterIndex"

                val words = paragraphs.sumOf { p -> p.split("\\s+".toRegex()).count { it.isNotBlank() } }

                chapters.add(
                    EpubChapter(
                        index = chapterIndex++,
                        title = finalTitle.trim(),
                        href = itemHref,
                        paragraphs = paragraphs,
                        wordCount = words
                    )
                )
            }

            if (chapters.isEmpty()) {
                return AppResult.Error(AppError.Content("Không thể trích xuất chương nào từ tệp EPUB"))
            }

            AppLogger.i("EpubParser") { "Successfully parsed '$title' by $author with ${chapters.size} chapters" }

            AppResult.Success(
                ParsedEpub(
                    title = title.trim(),
                    author = author.trim(),
                    description = description?.let { cleanHtmlToPlainText(it) },
                    chapters = chapters
                )
            )
        } catch (e: Exception) {
            AppLogger.e("EpubParser", e) { "Failed to parse EPUB: ${e.message}" }
            AppResult.Error(AppError.Content("Lỗi đọc tệp sách EPUB: ${e.message}"))
        }
    }

    private fun extractOpfPath(containerXml: String): String? {
        val match = Regex("full-path\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(containerXml)
        return match?.groupValues?.get(1)
    }

    private fun extractTagContent(xml: String, tagName: String): String? {
        val match = Regex("<$tagName\\b[^>]*>([\\s\\S]*?)</$tagName>", RegexOption.IGNORE_CASE).find(xml)
        return match?.groupValues?.get(1)?.let { decodeHtmlEntities(it.trim()) }
    }

    private data class ManifestItem(val id: String, val href: String, val mediaType: String)

    private fun extractManifest(opfContent: String): Map<String, ManifestItem> {
        val manifestMap = mutableMapOf<String, ManifestItem>()
        val itemRegex = Regex("<item\\b([^>]+)/?>", RegexOption.IGNORE_CASE)

        itemRegex.findAll(opfContent).forEach { match ->
            val attrs = match.groupValues[1]
            val id = extractAttribute(attrs, "id")
            val href = extractAttribute(attrs, "href")
            val mediaType = extractAttribute(attrs, "media-type")

            if (id != null && href != null) {
                manifestMap[id] = ManifestItem(id, href, mediaType ?: "")
            }
        }
        return manifestMap
    }

    private fun extractSpine(opfContent: String): List<String> {
        val spineList = mutableListOf<String>()
        val itemrefRegex = Regex("<itemref\\b([^>]+)/?>", RegexOption.IGNORE_CASE)

        itemrefRegex.findAll(opfContent).forEach { match ->
            val attrs = match.groupValues[1]
            val idref = extractAttribute(attrs, "idref")
            if (idref != null) {
                spineList.add(idref)
            }
        }
        return spineList
    }

    private fun parseNcx(ncxContent: String, tocMap: MutableMap<String, String>) {
        val navPointRegex = Regex("<navPoint\\b[\\s\\S]*?<navLabel>\\s*<text>([\\s\\S]*?)</text>\\s*</navLabel>\\s*<content\\s+src=[\"']([^\"']+)[\"'][^>]*>", RegexOption.IGNORE_CASE)
        navPointRegex.findAll(ncxContent).forEach { match ->
            val label = decodeHtmlEntities(match.groupValues[1].trim())
            val src = match.groupValues[2].substringBefore("#").trim()
            if (label.isNotBlank() && src.isNotBlank()) {
                tocMap[src] = label
            }
        }
    }

    private fun parseXhtmlContent(xhtml: String): Pair<String?, List<String>> {
        val clean = xhtml
            .replace(xmlDeclRegex, "")
            .replace(doctypeRegex, "")
            .replace(commentRegex, "")
            .replace(scriptRegex, "")
            .replace(styleRegex, "")

        // Extract title if present
        val headerTitle = extractTagContent(clean, "h1")
            ?: extractTagContent(clean, "h2")
            ?: extractTagContent(clean, "h3")
            ?: extractTagContent(clean, "title")

        // Format HTML tags into structural newlines:
        // 1. Replace <br> and <hr>
        val withLineBreaks = clean
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)<hr\\s*/?>"), "\n---\n")

        // 2. Replace block closing and opening tags with double newlines
        val withBlockBreaks = withLineBreaks
            .replace(Regex("(?i)</(p|div|section|article|li|tr|h[1-6])>"), "\n\n")
            .replace(Regex("(?i)<(p|div|section|article|li|tr|h[1-6])[^>]*>"), "\n")

        // 3. Strip all other XML/HTML tags
        val rawText = htmlTagRegex.replace(withBlockBreaks, "")

        // 4. Decode HTML entities and collect clean, non-empty paragraphs
        val paragraphs = rawText
            .split("\n")
            .map { decodeHtmlEntities(it).trim() }
            .filter { it.isNotBlank() }

        return Pair(headerTitle?.let { cleanHtmlToPlainText(it) }, paragraphs)
    }

    private fun cleanHtmlToPlainText(html: String): String {
        val withoutTags = htmlTagRegex.replace(html, "")
        return decodeHtmlEntities(withoutTags).trim()
    }

    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#8216;", "‘")
            .replace("&#8217;", "’")
            .replace("&#8220;", "“")
            .replace("&#8221;", "”")
            .replace("&#8230;", "…")
            .replace("&#8211;", "–")
            .replace("&#8212;", "—")
            .replace(Regex("&#(\\d+);")) { match ->
                val code = match.groupValues[1].toIntOrNull()
                if (code != null) code.toChar().toString() else match.value
            }
            .replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
                val code = match.groupValues[1].toIntOrNull(16)
                if (code != null) code.toChar().toString() else match.value
            }
            .replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "") // Remove zero-width characters
    }

    private fun extractAttribute(attributesString: String, attributeName: String): String? {
        val match = Regex("$attributeName\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(attributesString)
        return match?.groupValues?.get(1)
    }

    private fun resolvePath(baseDir: Path, relativeHref: String): Path {
        val cleanHref = relativeHref.substringBefore("#")
        return if (baseDir.name.isEmpty()) {
            cleanHref.toPath()
        } else {
            baseDir / cleanHref.toPath()
        }
    }
}
