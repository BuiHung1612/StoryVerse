package com.slowbuild.storyverse.domain.theme

import kotlinx.serialization.Serializable

@Serializable
enum class AppThemePreset(
    val id: String,
    val displayNameVi: String,
    val displayNameEn: String,
    val previewHex: Long
) {
    LIGHT(
        id = "light",
        displayNameVi = "Sáng Tiêu Chuẩn",
        displayNameEn = "Classic Light",
        previewHex = 0xFFF8F9FA
    ),
    DARK(
        id = "dark",
        displayNameVi = "Tối Dịu Mắt",
        displayNameEn = "Soft Dark",
        previewHex = 0xFF181A1E
    ),
    SEPIA(
        id = "sepia",
        displayNameVi = "Vàng Ấm / Giấy Sách",
        displayNameEn = "Sepia / Warm Paper",
        previewHex = 0xFFF5EFEB
    ),
    PARCHMENT(
        id = "parchment",
        displayNameVi = "Giấy Cổ / Vintage",
        displayNameEn = "Parchment / Vintage",
        previewHex = 0xFFEFE6D5
    ),
    MIDNIGHT(
        id = "midnight",
        displayNameVi = "OLED Đen Tuyền",
        displayNameEn = "Midnight / OLED",
        previewHex = 0xFF000000
    ),
    FOREST(
        id = "forest",
        displayNameVi = "Xanh Rêu Thư Giãn",
        displayNameEn = "Forest / Emerald",
        previewHex = 0xFF14241C
    );

    companion object {
        val DEFAULT = LIGHT

        fun fromId(id: String): AppThemePreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
