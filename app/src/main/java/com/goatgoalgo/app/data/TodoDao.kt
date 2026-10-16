package com.goatgoalgo.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items")
    fun observeAll(): Flow<List<TodoItemEntity>>

    @Query("SELECT * FROM todo_items")
    suspend fun getAll(): List<TodoItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TodoItemEntity>)

    @Query("DELETE FROM todo_items")
    suspend fun deleteAll()

    @Query("DELETE FROM todo_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
