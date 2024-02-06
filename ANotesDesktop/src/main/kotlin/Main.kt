import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainViewModel
import ui.Screens
import ui.theme.ANotesDesktopTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ANotes",
        icon = painterResource("icon.png")
    ) {
        val vm = MainViewModel()
        vm.tryDriveSync()
        ANotesDesktopTheme { Screens(vm = vm) }
    }
}