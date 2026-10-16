package com.goatgoalgo.app.backup

import com.goatgoalgo.app.data.TodoItemEntity
import com.goatgoalgo.app.data.TodoStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {
    @Test
    fun roundTripPreservesNestedTasksAndStatuses() {
        val source = listOf(
            item("root", null, "通过 CET4", TodoStatus.COMPLETED, null),
            item("child", "root", "背单词", TodoStatus.FAILED, 1234L, dueDate = "2026-12-01"),
        )

        val restored = BackupCodec.decode(BackupCodec.encode(source))

        assertEquals(source.sortedBy { it.id }, restored.sortedBy { it.id })
    }

    @Test
    fun invalidParentIsRejected() {
        val json = """{
          "schemaVersion": 1,
          "exportedAt": "2026-10-16T00:00:00Z",
          "items": [{
            "id": "child",
            "parentId": "missing",
            "title": "孤儿任务",
            "note": null,
            "status": "ACTIVE",
            "sortOrder": 0,
            "createdAt": "2026-10-16T00:00:00Z",
            "updatedAt": "2026-10-16T00:00:00Z",
            "deletedAt": null
          }]
        }"""

        assertThrows(IllegalArgumentException::class.java) {
            BackupCodec.decode(json)
        }
    }

    @Test
    fun cyclesAreRejected() {
        val json = """{
          "schemaVersion": 1,
          "exportedAt": "2026-10-16T00:00:00Z",
          "items": [
            {"id":"a","parentId":"b","title":"A","note":null,"status":"ACTIVE","sortOrder":0,"createdAt":"2026-10-16T00:00:00Z","updatedAt":"2026-10-16T00:00:00Z","deletedAt":null},
            {"id":"b","parentId":"a","title":"B","note":null,"status":"ACTIVE","sortOrder":0,"createdAt":"2026-10-16T00:00:00Z","updatedAt":"2026-10-16T00:00:00Z","deletedAt":null}
          ]
        }"""

        assertThrows(IllegalArgumentException::class.java) {
            BackupCodec.decode(json)
        }
    }

    @Test
    fun schemaOneWithoutDueDateStillImports() {
        val json = """{
          "schemaVersion": 1,
          "exportedAt": "2026-10-16T00:00:00Z",
          "items": [{
            "id": "root",
            "parentId": null,
            "title": "通过 CET4",
            "note": null,
            "status": "ACTIVE",
            "sortOrder": 0,
            "createdAt": "2026-10-16T00:00:00Z",
            "updatedAt": "2026-10-16T00:00:00Z",
            "deletedAt": null
          }]
        }"""

        val restored = BackupCodec.decode(json)

        assertEquals(null, restored.single().dueDate)
        assertEquals("通过 CET4", restored.single().title)
    }

    @Test
    fun invalidDueDateIsRejected() {
        val json = """{
          "schemaVersion": 2,
          "exportedAt": "2026-10-16T00:00:00Z",
          "items": [{
            "id": "root",
            "parentId": null,
            "title": "通过 CET4",
            "note": null,
            "dueDate": "2026/10/16",
            "status": "ACTIVE",
            "sortOrder": 0,
            "createdAt": "2026-10-16T00:00:00Z",
            "updatedAt": "2026-10-16T00:00:00Z",
            "deletedAt": null
          }]
        }"""

        assertThrows(IllegalArgumentException::class.java) {
            BackupCodec.decode(json)
        }
    }

    private fun item(
        id: String,
        parentId: String?,
        title: String,
        status: TodoStatus,
        deletedAt: Long?,
        dueDate: String? = null,
    ) = TodoItemEntity(
        id = id,
        parentId = parentId,
        title = title,
        note = if (id == "child") "备注" else null,
        dueDate = dueDate,
        status = status,
        sortOrder = if (id == "root") 0 else 1,
        createdAt = 1_000L,
        updatedAt = 2_000L,
        deletedAt = deletedAt,
    )
}
