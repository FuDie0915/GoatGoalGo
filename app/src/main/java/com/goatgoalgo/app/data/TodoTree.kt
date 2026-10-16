package com.goatgoalgo.app.data

import java.util.ArrayDeque

data class VisibleTodo(
    val item: TodoItemEntity,
    val depth: Int,
    val hasChildren: Boolean,
    val isSearchMatch: Boolean,
)

private fun TodoItemEntity.orderKey(): List<Comparable<*>> =
    listOf(sortOrder, createdAt, id)

private fun List<TodoItemEntity>.ordered(): List<TodoItemEntity> =
    sortedWith(
        compareBy<TodoItemEntity> { it.sortOrder }
            .thenBy { it.createdAt }
            .thenBy { it.id },
    )

fun buildVisibleTodoList(
    items: List<TodoItemEntity>,
    expandedIds: Set<String>,
    query: String,
): List<VisibleTodo> {
    val activeItems = items.filter { it.deletedAt == null }
    val byParent = activeItems.groupBy { it.parentId }
    val byId = activeItems.associateBy { it.id }
    val roots = byParent[null].orEmpty().ordered()
    val normalizedQuery = query.trim().lowercase()
    val matchingIds = if (normalizedQuery.isBlank()) {
        emptySet()
    } else {
        activeItems.filter {
            it.title.lowercase().contains(normalizedQuery) ||
                it.note.orEmpty().lowercase().contains(normalizedQuery) ||
                it.dueDate.orEmpty().lowercase().contains(normalizedQuery)
        }.mapTo(mutableSetOf()) { it.id }
    }

    val includedIds = if (normalizedQuery.isBlank()) {
        activeItems.mapTo(mutableSetOf()) { it.id }
    } else {
        val included = mutableSetOf<String>()
        matchingIds.forEach { matchingId ->
            var current: TodoItemEntity? = byId[matchingId]
            val visited = mutableSetOf<String>()
            while (current != null && visited.add(current.id)) {
                included += current.id
                current = current.parentId?.let(byId::get)
            }
        }
        included
    }

    data class StackItem(val item: TodoItemEntity, val depth: Int)
    val stack = ArrayDeque<StackItem>()
    roots.asReversed().forEach { stack.addLast(StackItem(it, 0)) }
    val result = mutableListOf<VisibleTodo>()

    while (stack.isNotEmpty()) {
        val current = stack.removeLast()
        if (current.item.id !in includedIds) continue

        val children = byParent[current.item.id].orEmpty().ordered()
        result += VisibleTodo(
            item = current.item,
            depth = current.depth,
            hasChildren = children.isNotEmpty(),
            isSearchMatch = current.item.id in matchingIds,
        )

        val shouldExpand = normalizedQuery.isNotBlank() || current.item.id in expandedIds
        if (shouldExpand) {
            children.asReversed().forEach { child ->
                stack.addLast(StackItem(child, current.depth + 1))
            }
        }
    }
    return result
}

fun descendantIds(items: List<TodoItemEntity>, rootId: String): Set<String> {
    val byParent = items.groupBy { it.parentId }
    val result = mutableSetOf<String>()
    val stack = ArrayDeque<String>()
    stack.addLast(rootId)
    while (stack.isNotEmpty()) {
        val id = stack.removeLast()
        if (!result.add(id)) continue
        byParent[id].orEmpty().forEach { stack.addLast(it.id) }
    }
    return result
}

fun deletedTreeRoots(items: List<TodoItemEntity>): List<TodoItemEntity> {
    val deleted = items.filter { it.deletedAt != null }
    val deletedIds = deleted.mapTo(mutableSetOf()) { it.id }
    return deleted.filter { it.parentId !in deletedIds }.sortedWith(
        compareBy<TodoItemEntity> { it.deletedAt ?: Long.MAX_VALUE }
            .thenBy { it.createdAt }
            .thenBy { it.id },
    )
}
