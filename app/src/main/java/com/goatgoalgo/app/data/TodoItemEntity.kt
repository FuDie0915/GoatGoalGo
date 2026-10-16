package com.goatgoalgo.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo_items",
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["deletedAt"]),
    ],
)
data class TodoItemEntity(
    @PrimaryKey val id: String,
    val parentId: String?,
    val title: String,
    val note: String?,
    val dueDate: String? = null,
    val status: TodoStatus,
    val sortOrder: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
