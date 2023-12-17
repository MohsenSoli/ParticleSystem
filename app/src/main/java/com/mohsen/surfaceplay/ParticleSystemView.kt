package com.mohsen.surfaceplay

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.LinkedList
import kotlin.random.Random


class ParticleSystemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val surfaceHolder: SurfaceHolder = holder
    private val pool = ParticlePool(5000)
    private val particleList = Collections.synchronizedList(LinkedList<Particle>())
    private val paint: Paint = Paint()
    private val textPaint = Paint().apply {
        textSize = 50f
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(3f, 3f, 3f, Color.DKGRAY)
    }

    private val gridSizePixel = 30
    private val grid: Array<Array<MutableList<Particle>>> by lazy {
        Array(width / gridSizePixel) { Array(height / gridSizePixel) { mutableListOf() } }
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

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

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
        isActive = false
    }

    private fun drawParticles() {
        val canvas = surfaceHolder.lockCanvas()
        canvas?.let {
            it.drawColor(Color.BLACK)
            it.drawText("Count : ${particleList.size}", 25f, 60f, textPaint)
            grid.forEach { column -> column.forEach { cell -> cell.clear() } }
            synchronized(particleList) {
                val iterator = particleList.iterator()
                while (iterator.hasNext()) {
                    val particle = iterator.next()
                    val isOut = particle.isOutOfScreen()
                    if (isOut) {
                        pool.releaseParticle(particle)
                        iterator.remove()
                    } else {
                        addParticleToGrid(particle)
                    }
                }

                particleList.forEach { particle ->
                    paint.color = particle.color
                    particle.update()
                    it.drawCircle(particle.x, particle.y, particle.size, paint)
                    if (particle.x < 400 && particle.y < 200) {
                        it.drawText("Count : ${particleList.size}", 25f, 60f, textPaint)
                    }
                }
            }
            Log.d("ParticleSystem", "Count : ${particleList.size}")
            surfaceHolder.unlockCanvasAndPost(it)
        }
    }


    private fun Particle.update() {
        x += velocityX
        y += velocityY

        if (x <= 0) {
            velocityX = -velocityX
            x = 0f
        } else if (x >= width) {
            velocityX = -velocityX
            x = width.toFloat()
        }

        if (y <= 0) {
            velocityY = -velocityY
            y = 0f
        } else if (y >= height) {
            velocityY = -velocityY
            y = height.toFloat()
        }

        getNearbyParticles().forEach { otherParticle ->
            if (otherParticle !== this && isCollidingWith(otherParticle)) {
                handleCollisionWith(otherParticle)
                correctPosition(otherParticle)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val styledAttributes: TypedArray = context.theme.obtainStyledAttributes(
            intArrayOf(android.R.attr.actionBarSize)
        )
        // getting event.y on a thread other than main, adds action bar height !!!!!!!!!!
        val actionBarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        event?.let { ev ->
            scope.launch {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        repeat(5) {
                            val p = pool.getParticle(
                                x = ev.x,
                                y = ev.y - actionBarSize,
                                velocityX = (Random.nextFloat() - 0.5f) * 5,
                                velocityY = (Random.nextFloat() - 0.5f) * 5,
                                size = Random.nextFloat() * 10 + 2,
                            )
                            particleList.add(p)
                        }
                    }
                }
            }
        }
        return true
    }

    private fun addParticles() {
        if (particleList.isNotEmpty()) return
        repeat(60) {
            val particle = pool.getParticle(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * height,
                velocityX = (Random.nextFloat() - 0.5f) * 8,
                velocityY = (Random.nextFloat() - 0.5f) * 8,
                size = Random.nextFloat() * 22 + 2
            )
            particleList.add(particle)
        }
    }

    private fun addParticleToGrid(particle: Particle) {
        val gridX = (particle.x / gridSizePixel).toInt().coerceIn(0, grid.size - 1)
        val gridY = (particle.y / gridSizePixel).toInt().coerceIn(0, grid[0].size - 1)
        grid[gridX][gridY].add(particle)
    }

    private fun Particle.getNearbyParticles(): List<Particle> {
        val nearbyParticles = mutableListOf<Particle>()

        val gridX = (x / gridSizePixel).toInt().coerceIn(0, grid.size - 1)
        val gridY = (y / gridSizePixel).toInt().coerceIn(0, grid[0].size - 1)

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

    private fun Particle.isOutOfScreen(): Boolean {
        return (x + size + THRESHOLD < 0 || y + size + THRESHOLD < 0 ||
                x - size > width + THRESHOLD || y - size > height + THRESHOLD)
    }

    companion object {
        private const val THRESHOLD = 5f
    }
}

fun getRandomColor(): Int {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color.rgb(red, green, blue)
}
