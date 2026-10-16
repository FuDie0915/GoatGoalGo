package com.goatgoalgo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.preferences.AppPreferences
import com.goatgoalgo.app.preferences.ColorRole
import com.goatgoalgo.app.preferences.PaletteId
import com.goatgoalgo.app.preferences.ThemeMode
import com.goatgoalgo.app.ui.theme.paletteSwatch

@Composable
fun TrashScreen(
    roots: List<TodoItemEntity>,
    onBack: () -> Unit,
    onRestore: (TodoItemEntity) -> Unit,
    onPermanentDelete: (TodoItemEntity) -> Unit,
    onClear: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "回收站",
                onBack = onBack,
                actions = {
                    if (roots.isNotEmpty()) {
                        TextButton(onClick = onClear) { Text("清空") }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (roots.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Text("回收站是空的", style = MaterialTheme.typography.titleMedium)
                Text(
                    "删除的目标会整棵分支留在这里。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(roots, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = cardBorder(),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WrappingTitle(item.title)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                item.dueDate?.let { DueDateChip(it, item.status) }
                                StatusBadge(item.status)
                            }
                            Text(
                                "恢复或删除时会处理整棵子任务。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row {
                                TextButton(onClick = { onRestore(item) }) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("恢复")
                                }
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { onPermanentDelete(item) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("永久删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onTheme: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    Scaffold(
        topBar = { AppTopBar("设置", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SectionHeading("外观") }
            item {
                SettingsAction(
                    icon = Icons.Default.Palette,
                    title = "主题与颜色",
                    subtitle = "明暗模式、主题色和组件色盘",
                    onClick = onTheme,
                )
            }
            item { SectionHeading("数据") }
            item {
                SettingsAction(
                    icon = Icons.Default.Backup,
                    title = "导出 JSON 备份",
                    subtitle = "保存任务、截止日期和回收站",
                    onClick = onExport,
                )
            }
            item {
                SettingsAction(
                    icon = Icons.Default.RestoreFromTrash,
                    title = "导入 JSON 备份",
                    subtitle = "导入后会替换当前全部任务",
                    onClick = onImport,
                )
            }
            item {
                Text(
                    "GoatGoalGo 完全离线运行。主题和折叠状态只保存在本机，不会写入任务备份。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
    )
}

@Composable
private fun SettingsAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ThemeScreen(
    preferences: AppPreferences,
    onBack: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onGlobalPalette: (PaletteId) -> Unit,
    onRolePalette: (ColorRole, PaletteId?) -> Unit,
) {
    val dark = when (preferences.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    Scaffold(
        topBar = { AppTopBar("主题与颜色", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PreviewStrip() }
            item { SectionHeading("显示模式") }
            item { ModeSelector(selected = preferences.themeMode, onSelected = onThemeMode) }
            item { SectionHeading("全局主题色") }
            item {
                PaletteGrid(
                    selected = preferences.globalPalette,
                    allowInherited = false,
                    dark = dark,
                    onSelected = { onGlobalPalette(it!!) },
                )
            }
            item { SectionHeading("组件颜色") }
            item {
                Text(
                    "点选色块即可覆盖。状态颜色始终是灰、绿、红、蓝。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            items(ColorRole.entries.toList(), key = { it.wireName }) { role ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = cardBorder(),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(role.label, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "当前：" + (preferences.overrides[role]?.label ?: "跟随全局 " + preferences.globalPalette.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        PaletteGrid(
                            selected = preferences.overrides[role],
                            allowInherited = true,
                            dark = dark,
                            onSelected = { onRolePalette(role, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewStrip() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("即时预览", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {}) { Text("主操作") }
                Text(
                    "强调",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TodoStatus.entries.forEach { StatusBadge(it) }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ThemeMode.entries.forEach { mode ->
            val active = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelected(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when (mode) {
                        ThemeMode.SYSTEM -> "跟随系统"
                        ThemeMode.LIGHT -> "浅色"
                        ThemeMode.DARK -> "深色"
                    },
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PaletteGrid(
    selected: PaletteId?,
    allowInherited: Boolean,
    dark: Boolean,
    onSelected: (PaletteId?) -> Unit,
) {
    val entries = buildList {
        if (allowInherited) add(null)
        addAll(PaletteId.entries)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { palette ->
                    PaletteChoice(
                        label = palette?.label ?: "跟随全局",
                        selected = selected == palette,
                        color = palette?.let { paletteSwatch(it, dark) } ?: MaterialTheme.colorScheme.primary,
                        onClick = { onSelected(palette) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PaletteChoice(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.62f) Color(0xFF1A1C1E) else Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
