package com.goatgoalgo.app.preferences

enum class ThemeMode(val wireName: String) {
    SYSTEM("SYSTEM"),
    LIGHT("LIGHT"),
    DARK("DARK"),
    ;

    companion object {
        fun fromWire(value: String): ThemeMode =
            entries.firstOrNull { it.wireName == value } ?: SYSTEM
    }
}

enum class PaletteId(val wireName: String, val label: String) {
    OCEAN("OCEAN", "Ocean 蓝"),
    FOREST("FOREST", "Forest 绿"),
    VIOLET("VIOLET", "Violet 紫"),
    SUNSET("SUNSET", "Sunset 橙"),
    ROSE("ROSE", "Rose 粉"),
    SLATE("SLATE", "Slate 灰"),
    ;

    companion object {
        fun fromWire(value: String): PaletteId =
            entries.firstOrNull { it.wireName == value } ?: OCEAN
    }
}

enum class ColorRole(val wireName: String, val label: String) {
    BACKGROUND("BACKGROUND", "应用背景"),
    SURFACE("SURFACE", "卡片/表面"),
    PRIMARY("PRIMARY", "主操作"),
    ACCENT("ACCENT", "次要强调"),
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val globalPalette: PaletteId = PaletteId.OCEAN,
    val overrides: Map<ColorRole, PaletteId> = emptyMap(),
    val expandedNodeIds: Set<String> = emptySet(),
)
