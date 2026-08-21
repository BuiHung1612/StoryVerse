package com.slowbuild.storyverse.domain.reader

import kotlinx.serialization.Serializable

@Serializable
enum class ReaderFontFamily(val displayName: String) {
    SYSTEM("Mặc định"),
    SERIF("Có chân (Serif)"),
    SANS_SERIF("Không chân (Sans)"),
    MONOSPACE("Đơn cách (Mono)")
}

@Serializable
enum class ReaderThemePreset(
    val id: String,
    val displayName: String,
    val backgroundColorHex: String,
    val textColorHex: String
) {
    DEFAULT("default", "Theo ứng dụng", "", ""),
    LIGHT("light", "Trắng sáng", "FFFFFFFF", "FF1A1A1A"),
    SEPIA("sepia", "Giấy ngà (Sepia)", "FFFBF0D9", "FF5F4B32"),
    DARK("dark", "Xám tối", "FF1E1E24", "FFE0E0E0"),
    BLACK("black", "OLED Đen", "FF000000", "FFA0A0A0")
}

@Serializable
data class ReaderPreferences(
    val fontSize: Float = 17f,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SYSTEM,
    val lineSpacingMultiplier: Float = 1.55f,
    val paragraphSpacingDp: Float = 14f,
    val horizontalPaddingDp: Float = 20f,
    val themePreset: ReaderThemePreset = ReaderThemePreset.DEFAULT,
    val keepScreenOn: Boolean = true
)
