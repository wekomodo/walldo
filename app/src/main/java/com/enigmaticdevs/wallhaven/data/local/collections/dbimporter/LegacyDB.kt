package com.enigmaticdevs.wallhaven.data.local.collections.dbimporter

import android.content.Context
import android.database.sqlite.SQLiteDatabase


// Opens an old database read-only. Returns null if the file doesn't exist (fresh install).
fun <T> Context.readLegacyDb(name: String, block: (SQLiteDatabase) -> T): T? {
    val file = getDatabasePath(name)
    if (!file.exists()) return null
    return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use(block)
}