package com.yurhel.alex.anotes

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.create
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.disable_all_actions
import anotes.composeapp.generated.resources.draw
import anotes.composeapp.generated.resources.edit_note
import anotes.composeapp.generated.resources.enable_draw
import anotes.composeapp.generated.resources.note
import anotes.composeapp.generated.resources.open_drop_buttons
import anotes.composeapp.generated.resources.save
import anotes.composeapp.generated.resources.status
import anotes.composeapp.generated.resources.swipe_notes
import anotes.composeapp.generated.resources.task
import anotes.composeapp.generated.resources.tasks
import anotes.composeapp.generated.resources.yes
import app.cash.sqldelight.db.SqlDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.AfterClass
import org.junit.BeforeClass
import java.io.File
import kotlin.test.Test

class NavigationTest {

    companion object {
        private lateinit var database: Database
        private lateinit var driver: SqlDriver
        private lateinit var lifecycleOwner: TestLifecycleOwner
        private lateinit var vm: MainViewModel

        @BeforeClass
        @JvmStatic
        fun setup() {
            driver = getSqlDriver()
            database = Database.Companion(driver)
        }

        @AfterClass
        @JvmStatic
        fun closeDB() {
            driver.close()
            val db = File("notes.db")
            val dbSets = File("notes.preferences_pb")
            db.delete()
            dbSets.delete()
        }
    }

    val openDropButtons = runBlocking { getString(Res.string.open_drop_buttons) }
    val backButton = runBlocking { getString(Res.string.back) }
    val saveButton = runBlocking { getString(Res.string.save) }
    val editNoteTitle = runBlocking { getString(Res.string.edit_note) }
    val deleteButton = runBlocking { getString(Res.string.delete) }
    val yes = runBlocking { getString(Res.string.yes) }

    // BEFORE RUNNING TEST (here are different setups for platforms)
    fun onMain(
        block: () -> Unit // Android
//        block: suspend CoroutineScope.() -> Unit // Desktop
    ) {
        // Android
        runBlocking {
            delay(2000)
            block()
        }
        // Desktop
//        runBlocking(Dispatchers.Main) {
//            block()
//            delay(1000)
//        }
    }
    fun clickOnDropButton(target: StringResource, semantics: SemanticsNodeInteractionsProvider) {
        onMain { semantics.onNodeWithContentDescription(openDropButtons).performClick() }
        val targetStr = runBlocking { getString(target) }
        onMain { semantics.onNodeWithContentDescription(targetStr).performClick() }
    }
    fun openAndDeleteNote(noteText: String, withTextAssert: Boolean = true, semantics: SemanticsNodeInteractionsProvider) {
        onMain { semantics.onNodeWithText(noteText).performClick() }
        if (withTextAssert) semantics.onNodeWithText(noteText).assertExists()
        onMain { semantics.onNodeWithContentDescription(deleteButton).performClick() }
        onMain { semantics.onNodeWithContentDescription(yes).performClick() }
        semantics.onNodeWithContentDescription(backButton).assertDoesNotExist()
        semantics.onNodeWithText(noteText).assertDoesNotExist()
    }
    fun editTitle(strForInput: String, semantics: SemanticsNodeInteractionsProvider) {
        onMain { semantics.onNodeWithContentDescription(editNoteTitle).performClick() }
        semantics.onNodeWithText("").assertExists()
        semantics.onNodeWithText("").performTextInput(strForInput)
        onMain { semantics.onNodeWithContentDescription(saveButton).performClick() }
    }


    class TestLifecycleOwner(private val initialState: Lifecycle.State) : LifecycleOwner {
        private fun getRegistry() : LifecycleRegistry {
            val registry = LifecycleRegistry(this)
            registry.currentState = initialState
            return registry
        }
        override val lifecycle: Lifecycle = getRegistry()
    }

    @OptIn(ExperimentalTestApi::class)
    fun ComposeUiTest.setUpUi() {
        lifecycleOwner = runBlocking(Dispatchers.Main) { TestLifecycleOwner(Lifecycle.State.RESUMED) }
        vm = MainViewModel(
            db = LocalDB.Companion.getInstance(driver),
            settings = SettingsDataStore.getInstance { createDataStorePlatform() },
            callExit = {},
            widgetIdWhenCreated = 0,
            callInitUpdateWidget = { _, _, _, _ -> },
            isTest = true
        )
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                Navigation(vm)
            }
        }
    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testNotes() = runComposeUiTest {
        setUpUi()
        val textForNote = "New text for note..."
        onNodeWithText(textForNote).assertDoesNotExist()
        clickOnDropButton(Res.string.note, this)
        onNodeWithText("").performTextInput(textForNote)
        onNodeWithText(textForNote).assertExists()
        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(textForNote).assertExists()
        openAndDeleteNote(textForNote, semantics = this)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testTasks() = runComposeUiTest {
        setUpUi()

        clickOnDropButton(Res.string.tasks, this)

        val textForTaskTitle = "Task Title"
        editTitle(textForTaskTitle, this)

        clickOnDropButton(Res.string.status, this)
        onNodeWithText("").assertExists()
        val textForStatus = "Status"
        onNodeWithText("").performTextInput(textForStatus)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        clickOnDropButton(Res.string.task, this)
        onNodeWithText("").assertExists()
        val textForTask = "Task text. Do something..."
        onNodeWithText("").performTextInput(textForTask)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        onNodeWithText(textForTaskTitle).assertExists()
        onNodeWithText(textForStatus).assertExists()
        onNodeWithText(textForTask).assertExists()

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(textForTaskTitle).assertExists()
        onNodeWithText(textForTask).assertExists()

        openAndDeleteNote(textForTaskTitle, semantics = this)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testSwipes() = runComposeUiTest {
        setUpUi()

        clickOnDropButton(Res.string.swipe_notes, this)

        val swipesTitle = "Swipes Title"
        editTitle(swipesTitle, this)

        val createSwipeTask = runBlocking { getString(Res.string.create) + " " + getString(Res.string.task).lowercase() }
        onMain { onNodeWithContentDescription(createSwipeTask).performClick() }

        onNodeWithText("").assertExists()
        val textForSwipeTask = "Swipe task text..."
        onNodeWithText("").performTextInput(textForSwipeTask)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        onNodeWithText(swipesTitle).assertExists()
        onNodeWithText(textForSwipeTask).assertExists()

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(swipesTitle).assertExists()
        onNodeWithText(textForSwipeTask).assertExists()

        openAndDeleteNote(swipesTitle, semantics = this)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testDraw() = runComposeUiTest {
        setUpUi()

        clickOnDropButton(Res.string.draw, this)

        val drawTitle = "Draw Title"
        editTitle(drawTitle, this)

        val enableDraw = runBlocking { getString(Res.string.enable_draw) }
        onMain { onNodeWithContentDescription(enableDraw).performClick() }

        onNodeWithTag("draw_canvas").performTouchInput {
            down(center)
            advanceEventTime(200)
            moveBy(centerRight)
            up()
        }

        val disableDraw = runBlocking { getString(Res.string.disable_all_actions) }
        onMain { onNodeWithContentDescription(disableDraw).performClick() }

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(drawTitle).assertExists()

        openAndDeleteNote(drawTitle, false, semantics = this)
    }
}