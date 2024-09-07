package com.yurhel.alex.anotes.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.Database

fun getSqlDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:notes.db")
    try {
        Database.Schema.create(driver)
    } catch (_: Exception) {}
    return driver
}