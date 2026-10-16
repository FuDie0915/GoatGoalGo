package com.goatgoalgo.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goatgoalgo.app.GoatGoalViewModel
import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.buildVisibleTodoList
import com.goatgoalgo.app.data.deletedTreeRoots

private enum class AppRoute {
    HOME,
    DETAIL,
    TRASH,
    SETTINGS,
    THEME,
}

@Composable
fun GoatGoalGoApp(viewModel: GoatGoalViewModel) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf(AppRoute.HOME) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var draftParentId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<TodoItemEntity?>(null) }
    var permanentDeleteTarget by remember { mutableStateOf<TodoItemEntity?>(null) }
    var clearTrashRequested by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    var exportPayload by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            viewModel.clearMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val payload = exportPayload
        if (uri != null && payload != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(payload.toByteArray(Charsets.UTF_8))
                } ?: error("无法打开导出文件")
            }.onSuccess {
                viewModel.showMessage("备份已导出")
            }.onFailure {
                viewModel.showMessage(it.message ?: "导出失败")
            }
        }
        exportPayload = null
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("无法打开导入文件")
            }.onSuccess {
                pendingImport = it
            }.onFailure {
                viewModel.showMessage(it.message ?: "读取备份失败")
            }
        }
    }

    val visibleTodos = buildVisibleTodoList(items, preferences.expandedNodeIds, query)
    val trashRoots = deletedTreeRoots(items)

    BackHandler(enabled = route != AppRoute.HOME) {
        route = if (route == AppRoute.THEME) AppRoute.SETTINGS else AppRoute.HOME
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (route) {
                AppRoute.HOME -> HomeScreen(
                    visibleTodos = visibleTodos,
                    expandedIds = preferences.expandedNodeIds,
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    onToggleExpanded = viewModel::toggleExpanded,
                    onOpen = {
                        selectedId = it.id
                        draftParentId = null
                        route = AppRoute.DETAIL
                    },
                    onCreate = {
                        selectedId = null
                        draftParentId = it
                        route = AppRoute.DETAIL
                    },
                    onStatus = viewModel::updateStatus,
                    onMove = viewModel::moveSibling,
                    onDelete = { deleteTarget = it },
                    onOpenTrash = { route = AppRoute.TRASH },
                    onOpenSettings = { route = AppRoute.SETTINGS },
                )

                AppRoute.DETAIL -> {
                    val item = items.firstOrNull { it.id == selectedId }
                    DetailScreen(
                        item = item,
                        parentId = draftParentId,
                        onBack = { route = AppRoute.HOME },
                        onSave = { title, note, dueDate ->
                            if (item == null) {
                                viewModel.create(title, note, draftParentId, dueDate)
                            } else {
                                viewModel.update(item, title, note, dueDate)
                            }
                            route = AppRoute.HOME
                        },
                        onStatus = { status ->
                            item?.let { viewModel.updateStatus(it, status) }
                        },
                        onAddChild = {
                            item?.let {
                                selectedId = null
                                draftParentId = it.id
                                route = AppRoute.DETAIL
                            }
                        },
                        onDelete = { item?.let { deleteTarget = it } },
                    )
                }

                AppRoute.TRASH -> TrashScreen(
                    roots = trashRoots,
                    onBack = { route = AppRoute.HOME },
                    onRestore = viewModel::restore,
                    onPermanentDelete = { permanentDeleteTarget = it },
                    onClear = { clearTrashRequested = true },
                )

                AppRoute.SETTINGS -> SettingsScreen(
                    onBack = { route = AppRoute.HOME },
                    onTheme = { route = AppRoute.THEME },
                    onExport = {
                        exportPayload = viewModel.exportJson()
                        exportLauncher.launch("goatgoalgo-backup.json")
                    },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                )

                AppRoute.THEME -> ThemeScreen(
                    preferences = preferences,
                    onBack = { route = AppRoute.SETTINGS },
                    onThemeMode = viewModel::setThemeMode,
                    onGlobalPalette = viewModel::setGlobalPalette,
                    onRolePalette = viewModel::setRolePalette,
                )
            }
        }
    }

    deleteTarget?.let { item ->
        ConfirmDialog(
            title = "删除任务？",
            message = "“" + item.title + "”及其所有子任务会进入回收站。",
            confirmLabel = "移入回收站",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.delete(item)
                deleteTarget = null
                route = AppRoute.HOME
            },
        )
    }

    permanentDeleteTarget?.let { item ->
        ConfirmDialog(
            title = "永久删除？",
            message = "“" + item.title + "”及其所有子任务将无法恢复。",
            confirmLabel = "永久删除",
            onDismiss = { permanentDeleteTarget = null },
            onConfirm = {
                viewModel.permanentlyDelete(item)
                permanentDeleteTarget = null
            },
        )
    }

    if (clearTrashRequested) {
        ConfirmDialog(
            title = "清空回收站？",
            message = "所有已删除任务将永久删除。",
            confirmLabel = "清空",
            onDismiss = { clearTrashRequested = false },
            onConfirm = {
                viewModel.clearTrash()
                clearTrashRequested = false
            },
        )
    }

    pendingImport?.let { json ->
        ConfirmDialog(
            title = "导入备份？",
            message = "导入会替换当前全部任务，主题和折叠状态不会被修改。",
            confirmLabel = "替换并导入",
            onDismiss = { pendingImport = null },
            onConfirm = {
                viewModel.replaceFromJson(json)
                pendingImport = null
            },
        )
    }
}
