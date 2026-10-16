package com.goatgoalgo.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TodoItemEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(TodoConverters::class)
abstract class GoatGoalGoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE todo_items ADD COLUMN dueDate TEXT")
            }
        }

        @Volatile
        private var instance: GoatGoalGoDatabase? = null

        fun get(context: Context): GoatGoalGoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GoatGoalGoDatabase::class.java,
                    "goatgoalgo.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
