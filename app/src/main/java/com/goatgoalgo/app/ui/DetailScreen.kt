package com.goatgoalgo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.goatgoalgo.app.data.DueDates
import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.ui.theme.todoStatusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    item: TodoItemEntity?,
    parentId: String?,
    onBack: () -> Unit,
    onSave: (String, String?, String?) -> Unit,
    onStatus: (TodoStatus) -> Unit,
    onAddChild: () -> Unit,
    onDelete: () -> Unit,
) {
    key(item?.id ?: "new_" + parentId.orEmpty()) {
        var title by rememberSaveable(item?.id, parentId) { mutableStateOf(item?.title.orEmpty()) }
        var note by rememberSaveable(item?.id, parentId) { mutableStateOf(item?.note.orEmpty()) }
        var dueDate by rememberSaveable(item?.id, parentId) { mutableStateOf(item?.dueDate) }
        var pickingDate by rememberSaveable { mutableStateOf(false) }
        Scaffold(
            topBar = { AppTopBar(if (item == null) "新建目标" else "目标详情", onBack) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FormCard("内容") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("目标标题") },
                        placeholder = { Text("例如：通过 CET4") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("备注（可选）") },
                        minLines = 4,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
                FormCard("截止日期") {
                    Text(
                        dueDate?.let(DueDates::format) ?: "不设置",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "可选。保存后显示在任务卡片上，不会发送提醒。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = { pickingDate = true }) { Text("选择日期") }
                        OutlinedButton(onClick = { dueDate = DueDates.todayIso() }) { Text("今天") }
                        if (dueDate != null) {
                            TextButton(onClick = { dueDate = null }) { Text("清除") }
                        }
                    }
                }
                if (item != null) {
                    FormCard("状态") {
                        Text(
                            "状态只属于这一条任务，不会自动带动子任务。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TodoStatus.entries.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                row.forEach { status ->
                                    StatusTile(
                                        status = status,
                                        selected = item.status == status,
                                        onClick = { onStatus(status) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = onAddChild) {
                            Icon(Icons.Default.SubdirectoryArrowRight, null)
                            Spacer(Modifier.width(6.dp))
                            Text("子任务")
                        }
                        OutlinedButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(Modifier.width(6.dp))
                            Text("回收站")
                        }
                    }
                }
                Button(
                    onClick = { onSave(title, note.takeIf { it.isNotBlank() }, dueDate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = title.trim().isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("保存", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        if (pickingDate) {
            DueDatePickerDialog(
                initial = dueDate,
                onDismiss = { pickingDate = false },
                onConfirm = {
                    dueDate = it
                    pickingDate = false
                },
            )
        }
    }
}

@Composable
private fun StatusTile(
    status: TodoStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val color = todoStatusColor(status, dark)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) color else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp),
            )
            .background(color.copy(alpha = if (selected) 0.14f else 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusBadge(status)
        if (selected) {
            Text("当前", style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initial: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.let(DueDates::toPickerUtcMillis),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis == null) onDismiss() else onConfirm(DueDates.fromPickerUtcMillis(millis))
                },
            ) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    ) {
        DatePicker(state = state)
    }
}
