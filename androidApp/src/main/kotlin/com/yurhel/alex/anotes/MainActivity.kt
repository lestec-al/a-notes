package com.yurhel.alex.anotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult as rememberLauncher
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts as Contracts
import com.yurhel.alex.anotes.ui.App
import com.yurhel.alex.anotes.ui.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val platform = Platform(this)
            val vm: MainViewModel by viewModels { MainViewModel.Factory(platform = platform) }
            platform.importLauncher = rememberLauncher(
                Contracts.StartActivityForResult(),
                platform::resultImportImage
            )
            App(vm)
        }
    }
}