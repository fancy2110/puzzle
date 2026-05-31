package com.puzzle.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.puzzle.game.data.AssetLoader
import com.puzzle.game.data.PreferencesFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        AssetLoader.init(this)
        PreferencesFactory.init(this)
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
