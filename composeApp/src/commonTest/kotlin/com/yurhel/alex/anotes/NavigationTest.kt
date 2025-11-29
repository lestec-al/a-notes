package com.yurhel.alex.anotes

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
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
import anotes.composeapp.generated.resources.close_drop_buttons
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
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import com.yurhel.alex.anotes.ui.theme.ANotesTheme
import db.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.After
import org.junit.Before
import org.junit.Test

class NavigationTest {

    private lateinit var database: Database
    private lateinit var driver: SqlDriver

    @Before
    fun setup() {
        driver = getSqlTestDriver()
        database = Database.Companion(driver)
    }

    @After
    fun tearDown() {
        driver.close()
    }

    /* BEFORE RUNNING ON ANDROID:
    - add backButton to noteBottomBar
    - onMain() function make without runBlocking with Dispatchers.Main */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testCrudWithUi() = runComposeUiTest {
        val lifecycleOwner = runBlocking(Dispatchers.Main) { TestLifecycleOwner(Lifecycle.State.RESUMED) }
        val vm = MainViewModel(
            db = LocalDB.Companion.getInstance(driver),
            formatDate = { it.toString() },
            syncData = { _, _ -> },
            showToast = {},
            callExit = {},
            widgetIdWhenCreated = 0,
            callInitUpdateWidget = { _, _, _, _ -> }
        )
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                ANotesTheme {
                    Navigation(vm)
                }
            }
        }


        // SETUP TEST
        val openDropButtons = runBlocking { getString(Res.string.open_drop_buttons) }
        val closeDropButtons = runBlocking { getString(Res.string.close_drop_buttons) }
        val backButton = runBlocking { getString(Res.string.back) }
        val saveButton = runBlocking { getString(Res.string.save) }
        val editNoteTitle = runBlocking { getString(Res.string.edit_note) }
        val deleteButton = runBlocking { getString(Res.string.delete) }
        val yes = runBlocking { getString(Res.string.yes) }

        fun delay500() {
            runBlocking { delay(500) }
        }

        // For desktop
        //fun onMain(block: suspend CoroutineScope.() -> Unit) {
        //    runBlocking(Dispatchers.Main, block)
        //}
        // For android
        fun onMain(block: () -> Unit) {
            block()
            delay500()
        }

        fun clickOnDropButton(target: StringResource) {
            onMain { onNodeWithContentDescription(openDropButtons).performClick() }
            val targetStr = runBlocking { getString(target) }
            delay500()
            onMain { onNodeWithContentDescription(targetStr).performClick() }
        }

        fun editTitle(strForInput: String) {
            onMain { onNodeWithContentDescription(editNoteTitle).performClick() }
            onNodeWithText("").assertExists()
            onNodeWithText("").performTextInput(strForInput)
            delay500()
            onMain { onNodeWithContentDescription(saveButton).performClick() }
        }

        fun openAndDeleteNote(noteText: String, withTextAssert: Boolean = true) {
            onMain { onNodeWithText(noteText).performClick() }
            delay500()
            if (withTextAssert) onNodeWithText(noteText).assertExists()
            onMain { onNodeWithContentDescription(deleteButton).performClick() }
            onMain { onNodeWithContentDescription(yes).performClick() }
            delay500()
            onNodeWithContentDescription(backButton).assertDoesNotExist()
            onNodeWithText(noteText).assertDoesNotExist()
            delay500()
        }


        // DROP MENU
        onNodeWithContentDescription(openDropButtons).performClick()
        onNodeWithContentDescription(closeDropButtons).performClick()
        onNodeWithContentDescription(openDropButtons).assertExists()


        // NOTE
        val textForNote = "New text for note..."
        onNodeWithText(textForNote).assertDoesNotExist()
        clickOnDropButton(Res.string.note)
        onNodeWithText("").performTextInput(textForNote)
        onNodeWithText(textForNote).assertExists()
        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(textForNote).assertExists()


        // TASKS
        clickOnDropButton(Res.string.tasks)

        val textForTaskTitle = "Task Title"
        editTitle(textForTaskTitle)

        clickOnDropButton(Res.string.status)
        onNodeWithText("").assertExists()
        val textForStatus = "Status"
        onNodeWithText("").performTextInput(textForStatus)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        clickOnDropButton(Res.string.task)
        onNodeWithText("").assertExists()
        val textForTask = "Task text. Do something..."
        onNodeWithText("").performTextInput(textForTask)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        delay500()
        onNodeWithText(textForTaskTitle).assertExists()
        onNodeWithText(textForStatus).assertExists()
        onNodeWithText(textForTask).assertExists()

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(textForTaskTitle).assertExists()
        onNodeWithText(textForTask).assertExists()


        // SWIPES
        clickOnDropButton(Res.string.swipe_notes)

        val swipesTitle = "Swipes Title"
        editTitle(swipesTitle)

        val createSwipeTask = runBlocking { getString(Res.string.create) + " " + getString(Res.string.task).lowercase() }
        onMain { onNodeWithContentDescription(createSwipeTask).performClick() }
        delay500()
        onNodeWithText("").assertExists()
        val textForSwipeTask = "Swipe task text..."
        onNodeWithText("").performTextInput(textForSwipeTask)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        delay500()
        onNodeWithText(swipesTitle).assertExists()
        onNodeWithText(textForSwipeTask).assertExists()

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onNodeWithText(swipesTitle).assertExists()
        onNodeWithText(textForSwipeTask).assertExists()


        // DRAW
        clickOnDropButton(Res.string.draw)

        val drawTitle = "Draw Title"
        editTitle(drawTitle)

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

        delay500()

        onMain { onNodeWithContentDescription(backButton).performClick() }
        delay500()
        onNodeWithText(drawTitle).assertExists()


        // DELETE OBJs
        openAndDeleteNote(textForNote)
        openAndDeleteNote(textForTaskTitle)
        openAndDeleteNote(swipesTitle)
        openAndDeleteNote(drawTitle, false)
    }

    class TestLifecycleOwner(private val initialState: Lifecycle.State) : LifecycleOwner {
        private fun getRegistry() : LifecycleRegistry {
            val registry = LifecycleRegistry(this)
            registry.currentState = initialState
            return registry
        }
        override val lifecycle: Lifecycle = getRegistry()
    }
}