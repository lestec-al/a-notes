package com.yurhel.alex.anotes

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.Database

actual fun getSqlTestDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.Companion.IN_MEMORY)
    Database.Companion.Schema.create(driver)
    return driver
}