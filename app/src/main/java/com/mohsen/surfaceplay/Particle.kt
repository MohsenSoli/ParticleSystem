package com.mohsen.surfaceplay

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val size: Float,
    val color: Int = getRandomColor()
) {

    private val mass = (size + 1).pow(2)

    fun isCollidingWith(other: Particle): Boolean {
        val distance = sqrt((x - other.x).pow(2f) + (abs(y - other.y)).pow(2f))

        // check collision in the next frame
        val nX = x + velocityX
        val nY = y + velocityY
        val oNX = other.x + other.velocityX
        val oNY = other.y + other.velocityY
        val nDistance = sqrt((nX - oNX).pow(2f) + (abs(nY - oNY)).pow(2f))

        return distance < size + other.size || nDistance < size + other.size
    }

    fun correctPosition(other: Particle) {
        val overlap = size + other.size - distanceBetween(other)
        val directionX = (x - other.x) / distanceBetween(other)
        val directionY = (y - other.y) / distanceBetween(other)

        if (overlap > 0) {
            x += directionX * overlap / 2
            y += directionY * overlap / 2
            other.x -= directionX * overlap / 2
            other.y -= directionY * overlap / 2
        }
    }

    private fun distanceBetween(other: Particle): Float {
        return sqrt((x - other.x).pow(2f) + (y - other.y).pow(2f))
    }

    fun handleCollisionWith(other: Particle) {
        // Calculate the normalized collision vector
        val collisionVectorX = other.x - x
        val collisionVectorY = other.y - y
        val collisionVectorLength = sqrt(
            collisionVectorX.pow(2.0f) + collisionVectorY.pow(2.0f)
        )
        val collisionNormalX = collisionVectorX / collisionVectorLength
        val collisionNormalY = collisionVectorY / collisionVectorLength

        // Calculate relative velocity
        val relativeVelocityX = other.velocityX - velocityX
        val relativeVelocityY = other.velocityY - velocityY

        // Calculate the dot product of relative velocity and collision normal
        val dotProduct = relativeVelocityX * collisionNormalX + relativeVelocityY * collisionNormalY

        // Calculate the impulse (change in velocity) along the collision normal
        val impulse = (2.0f * dotProduct) / (mass + other.mass)

        // Apply the impulse to the velocities
        velocityX += impulse * other.mass * collisionNormalX
        velocityY += impulse * other.mass * collisionNormalY
        other.velocityX -= impulse * mass * collisionNormalX
        other.velocityY -= impulse * mass * collisionNormalY
    }

    fun handleCollisionWith2(other: Particle) {
        val allMass = mass + other.mass

        val finalVx = ((mass - other.mass) * velocityX + 2 * other.mass * other.velocityX) / allMass
        val finalOtherVx = ((other.mass - mass) * other.velocityX + 2 * mass * velocityX) / allMass

        val finalVy = ((mass - other.mass) * velocityY + 2 * other.mass * other.velocityY) / allMass
        val finalOtherVy= ((other.mass - mass) * other.velocityY + 2 * mass * velocityY) / allMass

        velocityX = finalVx
        velocityY = finalVy

        other.velocityX = finalOtherVx
        other.velocityY = finalOtherVy
    }
}