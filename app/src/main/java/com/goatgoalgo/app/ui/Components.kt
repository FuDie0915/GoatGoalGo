package com.goatgoalgo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goatgoalgo.app.data.DueDates
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.ui.theme.todoStatusColor
import com.goatgoalgo.app.ui.theme.todoStatusLabel

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun cardBorder(emphasized: Boolean = false): BorderStroke = BorderStroke(
    width = if (emphasized) 1.5.dp else 1.dp,
    color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
)

@Composable
fun FormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = cardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleSmall)
                content()
            },
        )
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
fun StatusBadge(
    status: TodoStatus,
    modifier: Modifier = Modifier,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val background = todoStatusColor(status, dark)
    val label = todoStatusLabel(status)
    Text(
        text = label,
        color = if (background.luminance() > 0.62f) Color(0xFF1A1C1E) else Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clearAndSetSemantics { contentDescription = "状态$label" }
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun WrappingTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall,
    color: Color = Color.Unspecified,
    maxLines: Int = 8,
) {
    Text(
        text = text.withWrapOpportunities(),
        modifier = modifier.fillMaxWidth(),
        style = style,
        color = color,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}

internal fun String.withWrapOpportunities(limit: Int = 18): String {
    if (length <= limit) return this
    val out = StringBuilder(length + 4)
    var run = 0
    for (ch in this) {
        out.append(ch)
        val canBreak = ch.isWhitespace() || ch == '-' || ch == '/' || ch == '_' || ch.code > 0xFF
        if (canBreak) {
            run = 0
        } else {
            run++
            if (run >= limit) {
                out.append('​')
                run = 0
            }
        }
    }
    return out.toString()
}

@Composable
fun DueDateChip(
    dueDate: String,
    status: TodoStatus,
    modifier: Modifier = Modifier,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val overdue = status == TodoStatus.ACTIVE && DueDates.isBeforeToday(dueDate)
    val label = DueDates.format(dueDate) + if (overdue) " 已过" else ""
    val tint = if (overdue) {
        if (dark) Color(0xFFFFB067) else Color(0xFF9A5B12)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = if (overdue) 0.16f else 0.10f))
            .clearAndSetSemantics { contentDescription = "截止日期$label" }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = tint,
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        confirmButton = { Button(onClick = onConfirm) { Text(confirmLabel) } },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
