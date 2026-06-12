package com.yurhel.alex.anotes

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.back
import com.yurhel.alex.anotes.shared.create
import com.yurhel.alex.anotes.shared.delete
import com.yurhel.alex.anotes.shared.disable_all_actions
import com.yurhel.alex.anotes.shared.draw
import com.yurhel.alex.anotes.shared.edit_note
import com.yurhel.alex.anotes.shared.enable_draw
import com.yurhel.alex.anotes.shared.left_side
import com.yurhel.alex.anotes.shared.note
import com.yurhel.alex.anotes.shared.open_drop_buttons
import com.yurhel.alex.anotes.shared.right_side
import com.yurhel.alex.anotes.shared.save
import com.yurhel.alex.anotes.shared.status
import com.yurhel.alex.anotes.shared.swipe_notes
import com.yurhel.alex.anotes.shared.task
import com.yurhel.alex.anotes.shared.tasks
import com.yurhel.alex.anotes.shared.yes
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

// For Android, it is required to disable all animations (device dev options)
class NavigationTest {
    private val singleNoteText = """
        The Elder Scrolls III: Morrowind

        It is a 2002 action role-playing game developed by Bethesda Game Studios and published by Bethesda Softworks. It is the third installment in The Elder Scrolls series, following 1996's The Elder Scrolls II: Daggerfall, and was released for Microsoft Windows and Xbox. The main story takes place on Vvardenfell, an island in the Dunmer (Dark Elf) province of Morrowind, part of the continent of Tamriel. The central quests concern the demigod Dagoth Ur, housed within the volcanic Red Mountain, who seeks to gain power and break Morrowind free from Imperial reign.

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

    val openDropButtons = runBlocking { getString(Res.string.open_drop_buttons) }
    val backButton = runBlocking { getString(Res.string.back) }
    val saveButton = runBlocking { getString(Res.string.save) }
    val editNoteTitle = runBlocking { getString(Res.string.edit_note) }
    val deleteButton = runBlocking { getString(Res.string.delete) }
    val yes = runBlocking { getString(Res.string.yes) }
    val left = runBlocking { getString(Res.string.left_side) }
    val right = runBlocking { getString(Res.string.right_side) }

    fun onMain(block: suspend CoroutineScope.() -> Unit) {
        runBlocking {
            delay(1.5.seconds)
            block()
        }
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

    @OptIn(ExperimentalTestApi::class)
    fun ComposeUiTest.setUpUi() {
        setContent {
            val vm: MainViewModel = viewModel(
                factory = MainViewModel.Factory(
                    platform = getPlatform(),
                    showBackButton = true
                )
            )
            Navigation(vm)
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

        onMain {
            onNodeWithText(textForTaskTitle).assertExists()
            onNodeWithText(textForStatus).assertExists()
            onNodeWithText(textForTask).assertExists()
        }

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

        val createSwipeTask = getString(Res.string.create) + " " + getString(Res.string.task).lowercase()
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

        val enableDraw = getString(Res.string.enable_draw)
        onMain { onNodeWithContentDescription(enableDraw).performClick() }

        onNodeWithTag("draw_canvas").performTouchInput {
            down(center)
            advanceEventTime(200)
            moveBy(centerRight)
            up()
        }

        val disableDraw = getString(Res.string.disable_all_actions)
        onMain { onNodeWithContentDescription(disableDraw).performClick() }

        onMain { onNodeWithContentDescription(backButton).performClick() }
        onMain { onNodeWithText(drawTitle).assertExists() }

        openAndDeleteNote(drawTitle, false, semantics = this)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testAddNotes() = runComposeUiTest {
        println("--- testAddNotes 1 ---")
        setUpUi()
        println("--- testAddNotes 2 ---")
        delay(1.seconds)
        println("--- testAddNotes 3 ---")
        clickOnDropButton(Res.string.note, this)
        println("--- testAddNotes 4 ---")
        onNodeWithText("").performTextInput(singleNoteText)
        println("--- testAddNotes 5 ---")
        onMain { onNodeWithContentDescription(backButton).performClick() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testAddTasks() = runComposeUiTest {
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
    fun testAddSwipes() = runComposeUiTest {
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
            val createSwipeTask = getString(Res.string.create) + " " + getString(Res.string.task).lowercase()
            onMain { onNodeWithContentDescription(createSwipeTask).performClick() }
            onNodeWithText("").performTextInput(it)
            onMain { onNodeWithContentDescription(saveButton).performClick() }
        }

        onMain { onNodeWithContentDescription(backButton).performClick() }
    }
}