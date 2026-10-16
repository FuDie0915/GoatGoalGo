package com.goatgoalgo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.data.VisibleTodo
import com.goatgoalgo.app.ui.theme.todoStatusColor

@Composable
fun HomeScreen(
    visibleTodos: List<VisibleTodo>,
    expandedIds: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleExpanded: (String) -> Unit,
    onOpen: (TodoItemEntity) -> Unit,
    onCreate: (String?) -> Unit,
    onStatus: (TodoItemEntity, TodoStatus) -> Unit,
    onMove: (TodoItemEntity, Int) -> Unit,
    onDelete: (TodoItemEntity) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            HomeHeader(
                searchOpen = searchOpen,
                query = query,
                onQueryChange = onQueryChange,
                onToggleSearch = {
                    searchOpen = !searchOpen
                    if (!searchOpen) onQueryChange("")
                },
                onOpenTrash = onOpenTrash,
                onOpenSettings = onOpenSettings,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onCreate(null) },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("写下目标", fontWeight = FontWeight.SemiBold) },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (visibleTodos.isEmpty()) {
            EmptyHome(
                query = query,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onCreate = { onCreate(null) },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visibleTodos, key = { it.item.id }) { visible ->
                    var dragOffset by remember(visible.item.id) { mutableFloatStateOf(0f) }
                    TodoCard(
                        visible = visible,
                        expanded = visible.item.id in expandedIds || query.isNotBlank(),
                        onToggleExpanded = { onToggleExpanded(visible.item.id) },
                        onOpen = { onOpen(visible.item) },
                        onAddChild = { onCreate(visible.item.id) },
                        onStatus = { onStatus(visible.item, it) },
                        onMoveUp = { onMove(visible.item, -1) },
                        onMoveDown = { onMove(visible.item, 1) },
                        onDelete = { onDelete(visible.item) },
                        modifier = Modifier
                            .graphicsLayer { translationY = dragOffset }
                            .pointerInput(visible.item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { dragOffset = 0f },
                                    onDragCancel = { dragOffset = 0f },
                                    onDragEnd = { dragOffset = 0f },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount.y
                                        if (dragOffset >= 56f) {
                                            onMove(visible.item, 1)
                                            dragOffset = 0f
                                        } else if (dragOffset <= -56f) {
                                            onMove(visible.item, -1)
                                            dragOffset = 0f
                                        }
                                    },
                                )
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    searchOpen: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < 360.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderTitle()
                    HeaderActions(onToggleSearch, onOpenTrash, onOpenSettings)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderTitle(Modifier.weight(1f))
                    HeaderActions(onToggleSearch, onOpenTrash, onOpenSettings)
                }
            }
        }
        if (searchOpen) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("搜索目标、备注或日期") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "清除搜索")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
    }
}

@Composable
private fun HeaderTitle(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            "GoatGoalGo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "一步步成为梦想中的自己",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HeaderActions(
    onToggleSearch: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RoundIconButton(Icons.Default.Search, "搜索", onToggleSearch)
        RoundIconButton(Icons.Default.Delete, "回收站", onOpenTrash)
        RoundIconButton(Icons.Default.Settings, "设置", onOpenSettings)
    }
}

@Composable
private fun EmptyHome(
    query: String,
    modifier: Modifier = Modifier,
    onCreate: () -> Unit,
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (query.isBlank()) "从一个想成为的 GOAT 开始" else "没有找到匹配的目标",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        if (query.isBlank()) {
            Text(
                "先写下一个目标，再把它拆成可以一步步完成的子任务。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onCreate, shape = RoundedCornerShape(14.dp)) {
                Text("创建第一个目标")
            }
        }
    }
}

@Composable
private fun TodoCard(
    visible: VisibleTodo,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpen: () -> Unit,
    onAddChild: () -> Unit,
    onStatus: (TodoStatus) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val statusColor = todoStatusColor(visible.item.status, dark)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        val guideCount = visible.depth.coerceAtMost(5)
        repeat(guideCount) {
            Box(
                Modifier
                    .width(12.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline),
                )
            }
        }
        Card(
            onClick = onOpen,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (visible.isSearchMatch) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            border = cardBorder(visible.isSearchMatch),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(statusColor),
                )
                Row(
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        Modifier
                            .padding(top = 2.dp)
                            .size(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (visible.hasChildren) {
                            IconButton(
                                onClick = onToggleExpanded,
                                modifier = Modifier
                                    .size(32.dp)
                                    .semantics { contentDescription = if (expanded) "折叠" else "展开" },
                            ) {
                                Icon(
                                    if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(top = 6.dp, end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        WrappingTitle(visible.item.title)
                        visible.item.note?.takeIf { it.isNotBlank() }?.let { note ->
                            WrappingTitle(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            visible.item.dueDate?.let { DueDateChip(it, visible.item.status) }
                            StatusBadge(visible.item.status)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            IconButton(
                                onClick = { menuOpen = true },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("添加子任务") },
                                    leadingIcon = { Icon(Icons.Default.SubdirectoryArrowRight, null) },
                                    onClick = { menuOpen = false; onAddChild() },
                                )
                                DropdownMenuItem(
                                    text = { Text("上移") },
                                    leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                                    onClick = { menuOpen = false; onMoveUp() },
                                )
                                DropdownMenuItem(
                                    text = { Text("下移") },
                                    leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                                    onClick = { menuOpen = false; onMoveDown() },
                                )
                                HorizontalDivider()
                                TodoStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Text("标记为")
                                                StatusBadge(status)
                                            }
                                        },
                                        onClick = { menuOpen = false; onStatus(status) },
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("移入回收站") },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    onClick = { menuOpen = false; onDelete() },
                                )
                            }
                        }
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "长按拖动排序",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
