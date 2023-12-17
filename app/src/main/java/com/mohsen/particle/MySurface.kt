package com.mohsen.particle

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import android.view.SurfaceView

class MySurface(context: Context): SurfaceView(context), SurfaceHolder.Callback {

    private val paint: Paint = Paint().apply {
        color = Color.RED
    }

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // This is called when the size or format of the surface changes.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // This is called when the surface is destroyed.
    }

    fun drawRect() {
        val canvas = holder.lockCanvas()
        canvas?.let {
            // Draw a red rectangle
            it.drawRect(width.toFloat()/2, height.toFloat() / 2, width.toFloat(), height.toFloat(), paint)

            // Unlock the canvas to show the changes
            holder.unlockCanvasAndPost(it)
        }
    }
}