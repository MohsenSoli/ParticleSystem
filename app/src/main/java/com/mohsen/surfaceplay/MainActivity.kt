package com.mohsen.surfaceplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mohsen.surfaceplay.ui.theme.SurfacePlayTheme

class MainActivity : ComponentActivity() {
    private val surface by lazy { ParticleSystemView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(surface)
    }

    override fun onResume() {
        super.onResume()
        surface.isActive = true
    }

    override fun onPause() {
        super.onPause()
        surface.isActive = false
    }
}