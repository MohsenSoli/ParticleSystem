package com.mohsen.surfaceplay

import java.util.LinkedList

class ParticlePool(private val maxParticles: Int) {

    private val particles = LinkedList<Particle>()

    init {
        repeat(maxParticles) {
            particles.add(Particle())
        }
    }

    fun getParticle(
        x: Float,
        y: Float,
        velocityX: Float,
        velocityY: Float,
        size: Float
    ): Particle? {

        if (particles.isEmpty()) {
            return Particle(
                x = x,
                y = y,
                velocityX = velocityX,
                velocityY = velocityY,
                size = size
            )
        }

        // Double check locking
        synchronized(particles) {
            if (particles.isEmpty()) {
                return Particle(
                    x = x,
                    y = y,
                    velocityX = velocityX,
                    velocityY = velocityY,
                    size = size
                )
            }
            return particles.removeFirst().also {
                it.reset(
                    x = x,
                    y = y,
                    velocityX = velocityX,
                    velocityY = velocityY,
                    size = size
                )
            }
        }
    }

    fun releaseParticle(particle: Particle) {
        if (particles.size < maxParticles) {
            particles.add(particle)
        }
    }
}

private fun Particle.reset(
    x: Float,
    y: Float,
    velocityX: Float,
    velocityY: Float,
    size: Float
) = apply {
    this.x = x
    this.y = y
    this.velocityX = velocityX
    this.velocityY = velocityY
    this.size = size
}