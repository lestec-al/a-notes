package com.yurhel.alex.anotes

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import db.Database

actual fun getSqlTestDriver(): SqlDriver = AndroidSqliteDriver(
    schema = Database.Schema,
    context = InstrumentationRegistry.getInstrumentation().targetContext,
    name = "notes_test.db"
)