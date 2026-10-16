package com.goatgoalgo.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoTreeTest {
    private val root = item("root", null, "通过 CET4", 0)
    private val child = item("child", "root", "背单词", 0)
    private val grandchild = item("grandchild", "child", "完成第 1 章", 0)
    private val sibling = item("sibling", "root", "做真题", 1)

    @Test
    fun collapsedTreeOnlyShowsRoots() {
        val visible = buildVisibleTodoList(
            listOf(root, child, grandchild, sibling),
            expandedIds = emptySet(),
            query = "",
        )
        assertEquals(listOf("root"), visible.map { it.item.id })
    }

    @Test
    fun expandedTreePreservesDepthAndOrder() {
        val visible = buildVisibleTodoList(
            listOf(root, child, grandchild, sibling),
            expandedIds = setOf("root", "child"),
            query = "",
        )
        assertEquals(listOf("root", "child", "grandchild", "sibling"), visible.map { it.item.id })
        assertEquals(listOf(0, 1, 2, 1), visible.map { it.depth })
    }

    @Test
    fun searchTemporarilyIncludesAncestors() {
        val visible = buildVisibleTodoList(
            listOf(root, child, grandchild, sibling),
            expandedIds = emptySet(),
            query = "第 1 章",
        )
        assertEquals(listOf("root", "child", "grandchild"), visible.map { it.item.id })
        assertTrue(visible.last().isSearchMatch)
    }

    @Test
    fun searchMatchesDueDate() {
        val dated = grandchild.copy(dueDate = "2026-12-01", title = "其他")
        val visible = buildVisibleTodoList(
            listOf(root, child, dated),
            expandedIds = emptySet(),
            query = "2026-12",
        )
        assertEquals(listOf("root", "child", "grandchild"), visible.map { it.item.id })
    }

    @Test
    fun deletedSubtreeRootIsDetected() {
        val deletedRoot = root.copy(deletedAt = 99L)
        val deletedChild = child.copy(deletedAt = 99L)
        assertEquals(listOf("root"), deletedTreeRoots(listOf(deletedRoot, deletedChild)).map { it.id })
        assertEquals(setOf("root", "child"), descendantIds(listOf(deletedRoot, deletedChild), "root"))
    }

    private fun item(id: String, parentId: String?, title: String, sortOrder: Long) =
        TodoItemEntity(
            id = id,
            parentId = parentId,
            title = title,
            note = null,
            status = TodoStatus.ACTIVE,
            sortOrder = sortOrder,
            createdAt = sortOrder,
            updatedAt = sortOrder,
            deletedAt = null,
        )
}
