package com.sonicsignature.db

import app.cash.sqldelight.db.SqlDriver

class IosDatabaseFactory : DatabaseFactory {
    override fun createDriver(): SqlDriver {
        TODO("Not yet implemented")
        // return NativeSqliteDriver(SonicDatabase.Schema, "sonic.db")
    }
}
