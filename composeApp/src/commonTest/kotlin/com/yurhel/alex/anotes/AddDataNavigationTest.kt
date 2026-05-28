package com.yurhel.alex.anotes

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.create
import anotes.composeapp.generated.resources.edit_note
import anotes.composeapp.generated.resources.left_side
import anotes.composeapp.generated.resources.note
import anotes.composeapp.generated.resources.open_drop_buttons
import anotes.composeapp.generated.resources.right_side
import anotes.composeapp.generated.resources.save
import anotes.composeapp.generated.resources.status
import anotes.composeapp.generated.resources.swipe_notes
import anotes.composeapp.generated.resources.task
import anotes.composeapp.generated.resources.tasks
import app.cash.sqldelight.db.SqlDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import db.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.junit.AfterClass
import org.junit.BeforeClass
import java.io.File
import kotlin.test.Test

class AddDataNavigationTest {

    private val singleNoteText = """
The Elder Scrolls III: Morrowind

It is a 2002 action role-playing game developed by Bethesda Game Studios and published by Bethesda Softworks. It is the third installment in The Elder Scrolls series, following 1996's The Elder Scrolls II: Daggerfall, and was released for Microsoft Windows and Xbox. The main story takes place on Vvardenfell, an island in the Dunmer (Dark Elf) province of Morrowind, part of the continent of Tamriel. The central quests concern the demigod Dagoth Ur, housed within the volcanic Red Mountain, who seeks to gain power and break Morrowind free from Imperial reign.[2][3][4]

Though primarily a fantasy game, with many gameplay elements and Western medieval and fantasy fiction tropes inspired by Dungeons & Dragons and previous role-playing games, Morrowind also features some steampunk elements and drew much inspiration from Middle Eastern and South Asian cultures. Morrowind was designed with an open-ended, freeform style of gameplay in mind with less of an emphasis on the main plot than its predecessors. This choice received mixed reactions while maintaining reviewers' appreciation for Morrowind's expansive, detailed game world.
        """.trimIndent()

    private val tasksTitle = "Fallout: New Vegas"
    private val tasksStatuses = listOf("Right", "Wrong")
    private val tasks = listOf(
        "The game was developed by Obsidian Entertainment",
        "Yes Man is a hidden member of the Brotherhood of Steel",
        "You can kill every NPC except for Yes Man, who respawns to prevent a softlock",
        "The Courier can join the Enclave as a major faction in the base game",
        "The best faction is the Kings — purely for the Elvis vibes",
        "The main conflict revolves around the Hoover Dam and control over the New Vegas Strip"
    )

    private val swipesTitle = "Shopping list"
    private val swipesLeft = "In process"
    private val swipesRight = "Done"
    private val swipes = listOf(
        "The Standard Book of Spells",
        "A History of Magic",
        "Magical Theory",
        "A Beginner's Guide to Transfiguration",
        "Magical Drafts and Potions",
        "The Dark Forces: A Guide to Self-Protection"
    )

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
            // Deletes data for desktop
            db.delete()
            dbSets.delete()
        }
    }

    val openDropButtons = runBlocking { getString(Res.string.open_drop_buttons) }
    val backButton = runBlocking { getString(Res.string.back) }
    val saveButton = runBlocking { getString(Res.string.save) }
    val editNoteTitle = runBlocking { getString(Res.string.edit_note) }
    val left = runBlocking { getString(Res.string.left_side) }
    val right = runBlocking { getString(Res.string.right_side) }

    // !!!
    // For Android it is required to disable all animations (device dev options)
    fun onMain(block: suspend CoroutineScope.() -> Unit) {
        runBlocking(
            Dispatchers.Main // only for Desktop !!!
        ) {
            delay(1500)
            block()
        }
    }

    fun clickOnDropButton(target: StringResource, semantics: SemanticsNodeInteractionsProvider) {
        onMain { semantics.onNodeWithContentDescription(openDropButtons).performClick() }
        val targetStr = runBlocking { getString(target) }
        onMain { semantics.onNodeWithContentDescription(targetStr).performClick() }
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
        onNodeWithText(singleNoteText).assertDoesNotExist()
        clickOnDropButton(Res.string.note, this)
        onNodeWithText("").performTextInput(singleNoteText)
        onMain { onNodeWithContentDescription(backButton).performClick() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testTasks() = runComposeUiTest {
        setUpUi()
        clickOnDropButton(Res.string.tasks, this)
        editTitle(tasksTitle, this)

        tasksStatuses.forEach {
            clickOnDropButton(Res.string.status, this)
            onNodeWithText("").performTextInput(it)
            onMain { onNodeWithContentDescription(saveButton).performClick() }
        }

        tasks.forEach {
            clickOnDropButton(Res.string.task, this)
            onNodeWithText("").performTextInput(it)
            onMain { onNodeWithContentDescription(saveButton).performClick() }
        }

        onMain { onNodeWithContentDescription(backButton).performClick() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testSwipes() = runComposeUiTest {
        setUpUi()
        clickOnDropButton(Res.string.swipe_notes, this)
        editTitle(swipesTitle, this)

        // Edit left side
        onMain { onNodeWithText(left).performClick() }
        onNodeWithText("").assertExists()
        onNodeWithText("").performTextInput(swipesLeft)
        onMain { onNodeWithContentDescription(saveButton).performClick() }
        // Edit right side
        onMain { onNodeWithText(right).performClick() }
        onNodeWithText("").assertExists()
        onNodeWithText("").performTextInput(swipesRight)
        onMain { onNodeWithContentDescription(saveButton).performClick() }

        swipes.forEach {
            val createSwipeTask = runBlocking { getString(Res.string.create) + " " + getString(Res.string.task).lowercase() }
            onMain { onNodeWithContentDescription(createSwipeTask).performClick() }
            onNodeWithText("").performTextInput(it)
            onMain { onNodeWithContentDescription(saveButton).performClick() }
        }

        onMain { onNodeWithContentDescription(backButton).performClick() }
    }
}