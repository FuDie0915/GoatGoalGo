package com.goatgoalgo.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goatgoalgo.app.ui.GoatGoalGoApp
import com.goatgoalgo.app.ui.theme.GoatGoalGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: GoatGoalViewModel = viewModel()
            val preferences = viewModel.preferences.collectAsStateWithLifecycle()
            GoatGoalGoTheme(preferences.value) {
                GoatGoalGoApp(viewModel)
            }
        }
    }
}
