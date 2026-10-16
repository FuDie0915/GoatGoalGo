package com.goatgoalgo.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goatgoalgo.app.backup.BackupCodec
import com.goatgoalgo.app.data.GoatGoalGoDatabase
import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoRepository
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.data.deletedTreeRoots
import com.goatgoalgo.app.preferences.AppPreferences
import com.goatgoalgo.app.preferences.ColorRole
import com.goatgoalgo.app.preferences.PaletteId
import com.goatgoalgo.app.preferences.PreferencesRepository
import com.goatgoalgo.app.preferences.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoatGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TodoRepository(GoatGoalGoDatabase.get(application))
    private val preferencesRepository = PreferencesRepository(application)

    val items: StateFlow<List<TodoItemEntity>> = repository.items.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    val preferences: StateFlow<AppPreferences> = preferencesRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppPreferences(),
    )

    private val searchQuery = MutableStateFlow("")
    val query: StateFlow<String> = searchQuery

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val trashRoots: StateFlow<List<TodoItemEntity>> = items
        .combine(MutableStateFlow(Unit)) { current, _ -> deletedTreeRoots(current) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        searchQuery.value = value
    }

    fun clearMessage() {
        _message.value = null
    }

    fun showMessage(value: String) {
        _message.value = value
    }

    fun toggleExpanded(id: String) {
        viewModelScope.launch {
            val current = preferences.value.expandedNodeIds
            val next = if (id in current) current - id else current + id
            preferencesRepository.setExpandedNodeIds(next)
        }
    }

    fun create(title: String, note: String?, parentId: String?, dueDate: String? = null) {
        if (title.trim().isEmpty()) {
            _message.value = "标题不能为空"
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.create(title, note, parentId, dueDate)
                if (parentId != null) {
                    preferencesRepository.setExpandedNodeIds(
                        preferences.value.expandedNodeIds + parentId,
                    )
                }
            }.onFailure { _message.value = it.message ?: "创建失败" }
        }
    }

    fun update(item: TodoItemEntity, title: String, note: String?, dueDate: String? = null) {
        if (title.trim().isEmpty()) {
            _message.value = "标题不能为空"
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.update(
                    item.copy(
                        title = title,
                        note = note,
                        dueDate = dueDate,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }.onFailure { _message.value = it.message ?: "保存失败" }
        }
    }

    fun updateStatus(item: TodoItemEntity, status: TodoStatus) {
        viewModelScope.launch {
            runCatching {
                repository.update(item.copy(status = status, updatedAt = System.currentTimeMillis()))
            }.onFailure { _message.value = it.message ?: "状态更新失败" }
        }
    }

    fun delete(item: TodoItemEntity) {
        viewModelScope.launch {
            runCatching {
                repository.deleteSubtree(item.id)
                preferencesRepository.setExpandedNodeIds(
                    preferences.value.expandedNodeIds - item.id,
                )
            }.onFailure { _message.value = it.message ?: "删除失败" }
        }
    }

    fun restore(item: TodoItemEntity) {
        viewModelScope.launch {
            runCatching { repository.restoreSubtree(item.id) }
                .onFailure { _message.value = it.message ?: "恢复失败" }
        }
    }

    fun permanentlyDelete(item: TodoItemEntity) {
        viewModelScope.launch {
            runCatching { repository.permanentlyDeleteSubtree(item.id) }
                .onFailure { _message.value = it.message ?: "永久删除失败" }
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            runCatching { repository.clearTrash() }
                .onFailure { _message.value = it.message ?: "清空回收站失败" }
        }
    }

    fun reorder(parentId: String?, orderedIds: List<String>) {
        viewModelScope.launch {
            runCatching { repository.reorder(parentId, orderedIds) }
                .onFailure { _message.value = it.message ?: "排序失败" }
        }
    }

    fun moveSibling(item: TodoItemEntity, direction: Int) {
        viewModelScope.launch {
            runCatching {
                val siblings = items.value
                    .filter { it.deletedAt == null && it.parentId == item.parentId }
                    .sortedWith(compareBy<TodoItemEntity> { it.sortOrder }.thenBy { it.createdAt }.thenBy { it.id })
                    .toMutableList()
                val index = siblings.indexOfFirst { it.id == item.id }
                val target = index + direction
                if (index < 0 || target !in siblings.indices) return@runCatching
                val moved = siblings.removeAt(index)
                siblings.add(target, moved)
                repository.reorder(item.parentId, siblings.map { it.id })
            }.onFailure { _message.value = it.message ?: "排序失败" }
        }
    }

    fun exportJson(): String = BackupCodec.encode(items.value)

    fun replaceFromJson(json: String) {
        viewModelScope.launch {
            runCatching {
                val imported = BackupCodec.decode(json)
                repository.replaceAll(imported)
                preferencesRepository.setExpandedNodeIds(emptySet())
            }.onSuccess {
                _message.value = "备份导入成功"
            }.onFailure {
                _message.value = it.message ?: "备份导入失败"
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setGlobalPalette(palette: PaletteId) {
        viewModelScope.launch {
            preferencesRepository.setGlobalPalette(palette)
        }
    }

    fun setRolePalette(role: ColorRole, palette: PaletteId?) {
        viewModelScope.launch {
            preferencesRepository.setRolePalette(role, palette)
        }
    }
}
