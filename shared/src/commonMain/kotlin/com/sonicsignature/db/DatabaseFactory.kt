package com.sonicsignature.db

interface DatabaseFactory {
    // fun createDriver(): SqlDriver // commented out since wasmJs misses SqlDriver
}

// fun createDatabase(factory: DatabaseFactory): SonicDatabase {
//     return SonicDatabase(factory.createDriver())
// }
