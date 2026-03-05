package com.sonicsignature.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver

class AndroidDatabaseFactory(private val context: Context) : DatabaseFactory {
    fun createDriver(): SqlDriver {
        TODO("Not yet implemented")
        // return AndroidSqliteDriver(SonicDatabase.Schema, context, "sonic.db")
    }
}
