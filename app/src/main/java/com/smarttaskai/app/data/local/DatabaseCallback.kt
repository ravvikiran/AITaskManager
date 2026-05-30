package com.smarttaskai.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/**
 * Pre-populates the database with default categories on first creation.
 * Executes synchronously within the database transaction — no coroutine needed.
 */
class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        val defaultCategories = listOf(
            Triple("Work", 0xFF4A3AFF, UUID.randomUUID().toString()),
            Triple("Personal", 0xFF4CAF50, UUID.randomUUID().toString()),
            Triple("Health", 0xFFE91E63, UUID.randomUUID().toString()),
            Triple("Learning", 0xFF9C27B0, UUID.randomUUID().toString()),
            Triple("Errands", 0xFFFF9800, UUID.randomUUID().toString()),
            Triple("Other", 0xFF607D8B, UUID.randomUUID().toString())
        )

        defaultCategories.forEach { (name, color, id) ->
            db.execSQL(
                "INSERT INTO categories (id, name, color) VALUES (?, ?, ?)",
                arrayOf(id, name, color)
            )
        }
    }
}
