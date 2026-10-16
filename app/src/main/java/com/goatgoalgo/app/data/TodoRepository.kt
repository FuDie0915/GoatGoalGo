package com.goatgoalgo.app.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID

class TodoRepository(
    private val database: GoatGoalGoDatabase,
) {
    private val dao = database.todoDao()

    val items: Flow<List<TodoItemEntity>> = dao.observeAll().distinctUntilChanged()

    suspend fun create(title: String, note: String?, parentId: String?, dueDate: String? = null): TodoItemEntity {
        val now = System.currentTimeMillis()
        val all = dao.getAll()
        val nextOrder = all
            .asSequence()
            .filter { it.deletedAt == null && it.parentId == parentId }
            .map { it.sortOrder }
            .maxOrNull()
            ?.plus(1)
            ?: 0L
        return TodoItemEntity(
            id = UUID.randomUUID().toString(),
            parentId = parentId,
            title = title.trim(),
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            dueDate = DueDates.normalize(dueDate),
            status = TodoStatus.ACTIVE,
            sortOrder = nextOrder,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        ).also { dao.upsertAll(listOf(it)) }
    }

    suspend fun update(item: TodoItemEntity) {
        dao.upsertAll(
            listOf(
                item.copy(
                    title = item.title.trim(),
                    note = item.note?.trim()?.takeIf { it.isNotEmpty() },
                    dueDate = DueDates.normalize(item.dueDate),
                ),
            ),
        )
    }

    suspend fun deleteSubtree(rootId: String) {
        database.withTransaction {
            val all = dao.getAll()
            val ids = descendantIds(all, rootId)
            val now = System.currentTimeMillis()
            dao.upsertAll(
                all.filter { it.id in ids }.map { it.copy(deletedAt = now, updatedAt = now) },
            )
        }
    }

    suspend fun restoreSubtree(rootId: String) {
        database.withTransaction {
            val all = dao.getAll()
            val ids = descendantIds(all, rootId)
            val now = System.currentTimeMillis()
            dao.upsertAll(
                all.filter { it.id in ids && it.deletedAt != null }
                    .map { it.copy(deletedAt = null, updatedAt = now) },
            )
        }
    }

    suspend fun permanentlyDeleteSubtree(rootId: String) {
        database.withTransaction {
            val ids = descendantIds(dao.getAll(), rootId).toList()
            if (ids.isNotEmpty()) dao.deleteByIds(ids)
        }
    }

    suspend fun clearTrash() {
        database.withTransaction {
            val ids = dao.getAll().filter { it.deletedAt != null }.map { it.id }
            if (ids.isNotEmpty()) dao.deleteByIds(ids)
        }
    }

    suspend fun reorder(parentId: String?, orderedIds: List<String>) {
        database.withTransaction {
            val all = dao.getAll().associateBy { it.id }
            val updates = orderedIds.mapIndexedNotNull { index, id ->
                all[id]?.takeIf { it.parentId == parentId }?.copy(
                    sortOrder = index.toLong(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
            dao.upsertAll(updates)
        }
    }

    suspend fun replaceAll(items: List<TodoItemEntity>) {
        database.withTransaction {
            dao.deleteAll()
            dao.upsertAll(items)
        }
    }
}
