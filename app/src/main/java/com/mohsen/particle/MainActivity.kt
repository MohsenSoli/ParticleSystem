package com.mohsen.particle

import android.os.Bundle
import androidx.activity.ComponentActivity

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