package com.example.memorygdynia

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Score(val id: Long, val nickname: String, val moves: Int)

class ScoresDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_SCORES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NICKNAME TEXT," +
                "$COLUMN_MOVES INTEGER)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        onCreate(db)
    }

    fun insertScore(nickname: String, moves: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NICKNAME, nickname)
            put(COLUMN_MOVES, moves)
        }
        return db.insert(TABLE_SCORES, null, values)
    }

    fun getTopScores(limit: Int = 10): List<Score> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCORES,
            arrayOf(COLUMN_ID, COLUMN_NICKNAME, COLUMN_MOVES),
            null,
            null,
            null,
            null,
            "$COLUMN_MOVES ASC",
            limit.toString()
        )

        val scores = mutableListOf<Score>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
                val nickname = getString(getColumnIndexOrThrow(COLUMN_NICKNAME))
                val moves = getInt(getColumnIndexOrThrow(COLUMN_MOVES))
                scores.add(Score(id, nickname, moves))
            }
        }
        cursor.close()
        return scores
    }

    companion object {
        const val DATABASE_NAME = "memorygame.db"
        const val DATABASE_VERSION = 1
        const val TABLE_SCORES = "scores"
        const val COLUMN_ID = "id"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_MOVES = "moves"
    }
}
