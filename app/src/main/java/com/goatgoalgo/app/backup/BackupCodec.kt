package com.goatgoalgo.app.backup

import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.data.DueDates
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
private data class BackupDocument(
    val schemaVersion: Int,
    val exportedAt: String,
    val items: List<BackupItem>,
)

@Serializable
private data class BackupItem(
    val id: String,
    val parentId: String?,
    val title: String,
    val note: String?,
    val dueDate: String? = null,
    val status: String,
    val sortOrder: Long,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
)

object BackupCodec {
    private const val CURRENT_SCHEMA = 2
    private val json = Json {
        explicitNulls = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    fun encode(items: List<TodoItemEntity>): String {
        val document = BackupDocument(
            schemaVersion = CURRENT_SCHEMA,
            exportedAt = Instant.now().toString(),
            items = items
                .sortedWith(compareBy<TodoItemEntity> { it.createdAt }.thenBy { it.id })
                .map { item ->
                    BackupItem(
                        id = item.id,
                        parentId = item.parentId,
                        title = item.title,
                        note = item.note,
                        dueDate = item.dueDate,
                        status = item.status.wireName,
                        sortOrder = item.sortOrder,
                        createdAt = Instant.ofEpochMilli(item.createdAt).toString(),
                        updatedAt = Instant.ofEpochMilli(item.updatedAt).toString(),
                        deletedAt = item.deletedAt?.let { Instant.ofEpochMilli(it).toString() },
                    )
                },
        )
        return json.encodeToString(document)
    }

    fun decode(rawJson: String): List<TodoItemEntity> {
        val document = runCatching {
            json.decodeFromString<BackupDocument>(rawJson)
        }.getOrElse { error ->
            val message = if (error is SerializationException) {
                "备份文件不是有效 JSON"
            } else {
                "备份文件读取失败"
            }
            throw IllegalArgumentException(message, error)
        }
        require(document.schemaVersion in 1..CURRENT_SCHEMA) { "不支持的备份版本" }
        val items = document.items.map { item ->
            val id = item.id.trim()
            val title = item.title.trim()
            val dueDate = item.dueDate?.trim()?.takeIf { it.isNotEmpty() }
            require(id.isNotEmpty()) { "任务 ID 不能为空" }
            require(title.isNotEmpty()) { "任务标题不能为空" }
            require(item.sortOrder >= 0) { "排序值无效" }
            require(dueDate == null || DueDates.isValid(dueDate)) { "截止日期无效" }
            TodoItemEntity(
                id = id,
                parentId = item.parentId,
                title = title,
                note = item.note?.trim()?.takeIf { it.isNotEmpty() },
                dueDate = dueDate,
                status = TodoStatus.fromWire(item.status),
                sortOrder = item.sortOrder,
                createdAt = parseTimestamp(item.createdAt),
                updatedAt = parseTimestamp(item.updatedAt),
                deletedAt = item.deletedAt?.let(::parseTimestamp),
            )
        }
        validate(items)
        return items
    }

    private fun validate(items: List<TodoItemEntity>) {
        val byId = items.associateBy { it.id }
        require(byId.size == items.size) { "备份中存在重复任务 ID" }
        items.forEach { item ->
            require(item.parentId == null || item.parentId in byId) {
                "任务 " + item.title + " 的父任务不存在"
            }
            val seen = mutableSetOf<String>()
            var current = item
            while (current.parentId != null) {
                require(seen.add(current.id)) { "备份中存在循环父子关系" }
                current = byId[current.parentId]
                    ?: throw IllegalArgumentException("父任务不存在")
            }
        }
    }

    private fun parseTimestamp(value: String): Long =
        runCatching { Instant.parse(value).toEpochMilli() }
            .getOrElse { throw IllegalArgumentException("时间格式无效", it) }
}
