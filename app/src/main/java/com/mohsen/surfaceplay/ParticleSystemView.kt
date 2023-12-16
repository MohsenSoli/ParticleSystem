package com.mohsen.surfaceplay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class ParticleSystemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val surfaceHolder: SurfaceHolder = holder
    private val particleList = CopyOnWriteArrayList<Particle>()
    private val paint: Paint = Paint()

    private val gridSize = 50
    private val grid: Array<Array<MutableList<Particle>>> by lazy {
        Array(width / gridSize) { Array(height / gridSize) { mutableListOf<Particle>() } }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    var isActive = false

    init {
        surfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        addParticles()
        startParticleSystem()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //TODO
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopParticleSystem()
    }

    private fun startParticleSystem() {
        scope.launch {
            while (isActive) {
                drawParticles()
                delay(8)
            }
        }
    }

    private fun stopParticleSystem() {
        //TODO
    }

    private fun drawParticles() {
        val canvas = surfaceHolder.lockCanvas()
        canvas?.let {
            it.drawColor(Color.BLACK)
            grid.forEach { column ->
                column.forEach { cell -> cell.clear() }
            }
            particleList.forEach { particle -> addParticleToGrid(particle) }
            particleList.forEach { particle ->
                paint.color = particle.color
                particle.update()
                it.drawCircle(particle.x, particle.y, particle.size, paint)
            }
            surfaceHolder.unlockCanvasAndPost(it)
        }
    }


    private fun Particle.update() {
        x += velocityX
        y += velocityY

        if (x - size < 0 || x + size > width) velocityX = -velocityX
        if (y - size < 0 || y + size > height) velocityY = -velocityY

        getNearbyParticles().forEach { otherParticle ->
            if (otherParticle !== this && isCollidingWith(otherParticle)) {
                handleCollisionWith(otherParticle)
                correctPosition(otherParticle)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val p = Particle(
                    x = event.x,
                    y = event.y,
                    velocityX = (Random.nextFloat() - 0.5f) * 5,
                    velocityY = (Random.nextFloat() - 0.5f) * 5,
                    size = Random.nextFloat() * 10 + 5,
                )
                particleList.add(p)
            }
        }
        return true
    }

    private fun addParticles() {
        repeat(50) {
            val particle = Particle(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * height,
                velocityX = (Random.nextFloat() - 0.5f) * 5,
                velocityY = (Random.nextFloat() - 0.5f) * 5,
                size = Random.nextFloat() * 40 + 5
            )
            particleList.add(particle)
        }
    }

    private fun addParticleToGrid(particle: Particle) {
        val gridX = (particle.x / gridSize).toInt().coerceIn(0, grid.size - 1)
        val gridY = (particle.y / gridSize).toInt().coerceIn(0, grid[0].size - 1)
        grid[gridX][gridY].add(particle)
    }

    private fun Particle.getNearbyParticles(): List<Particle> {
        val nearbyParticles = mutableListOf<Particle>()

        val gridX = (x / gridSize).toInt().coerceIn(0, grid.size - 1)
        val gridY = (y / gridSize).toInt().coerceIn(0, grid[0].size - 1)

        (-1..1).forEach { i ->
            (-1..1).forEach { j ->
                val x = (gridX + i)
                val y = (gridY + j)
                if (x >= 0 && x < grid.size && y >= 0 && y < grid[0].size) {
                    nearbyParticles.addAll(grid[x][y])
                }
            }
        }

        return nearbyParticles
    }
}

fun getRandomColor(): Int {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color.rgb(red, green, blue)
}
