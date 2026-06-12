package com.yurhel.alex.anotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel by viewModels {
                MainViewModel.Factory(platform = Platform(this))
            }
            Navigation(vm)
        }
    }
}