package com.yurhel.alex.anotes.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import db.Database

fun getSqlDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(Database.Schema, context, "notes.db")
}