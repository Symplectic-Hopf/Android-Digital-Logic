package com.digitallogic.karnaughmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digitallogic.karnaughmap.ui.screens.MainScreen
import com.digitallogic.karnaughmap.ui.theme.AndroidDigitalLogicTheme
import com.digitallogic.karnaughmap.ui.viewmodel.KMapViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidDigitalLogicTheme {
                val viewModel: KMapViewModel = viewModel()
                val state by viewModel.state.collectAsState()
                MainScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}
