package com.example.myapp.game

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var speed: Float = 300f,
    var attackRange: Float = 150f,
    var attackDamage: Float = 10f,
    var attackCooldown: Float = 0.5f,
    var attackTimer: Float = 0f,
    var size: Float = 40f,
    var hp: Float = 100f,
    var maxHp: Float = 100f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var moveMagnitude: Float = 0f,
    var speedMultiplier: Float = 1f,
    var facingAngle: Float = 0f
) {
    /** Sets directional analog velocity from the virtual joystick (-1..1 normalized dir, 0..1 magnitude) */
    fun setVelocity(dirX: Float, dirY: Float, magnitude: Float) {
        vx = dirX
        vy = dirY
        moveMagnitude = magnitude.coerceIn(0f, 1f)
        if (magnitude > 0.05f) {
            facingAngle = kotlin.math.atan2(dirY.toDouble(), dirX.toDouble()).toFloat()
        }
        targetX = x
        targetY = y
    }

    /** Halts analog movement immediately when thumb is released */
    fun stopMoving() {
        vx = 0f
        vy = 0f
        moveMagnitude = 0f
        targetX = x
        targetY = y
    }

    fun moveTo(tx: Float, ty: Float) {
        val engine = GameEngineHolder.engine
        // Clamp to playable area: stay within screen bounds and below sky line
        var clampedX = tx
        var clampedY = ty
        if (engine != null) {
            val groundTop = engine.screenH * 0.12f + size  // below sky
            clampedX = clampedX.coerceIn(size, engine.screenW - size)
            clampedY = clampedY.coerceIn(groundTop, engine.screenH - size)
        }
        // Prevent moving into river: clamp to nearest non-river point
        if (engine != null && engine.isPointOnRiver(clampedX, clampedY)) {
            // Find closest point outside river by stepping away along the vector
            var safeX = clampedX
            var safeY = clampedY
            val cx = x
            val cy = y
            val maxStep = 10
            var found = false
            for (i in 1..maxStep) {
                val t = i / maxStep.toFloat()
                val testX = cx + (clampedX - cx) * (1 - t)
                val testY = cy + (clampedY - cy) * (1 - t)
                if (!engine.isPointOnRiver(testX, testY)) {
                    safeX = testX
                    safeY = testY
                    found = true
                    break
                }
            }
            targetX = safeX
            targetY = safeY
        } else {
            targetX = clampedX
            targetY = clampedY
        }
    }

    fun update(dt: Float) {
        val engine = GameEngineHolder.engine
        val effectiveSpeed = speed * speedMultiplier

        if (moveMagnitude > 0f) {
            // Direct analog joystick velocity with sub-stepping & smooth river wall-sliding
            val maxStep = 0.02f
            var remaining = dt
            while (remaining > 0f) {
                val step = remaining.coerceAtMost(maxStep)
                remaining -= step

                val moveDist = effectiveSpeed * moveMagnitude * step
                val wishDx = vx * moveDist
                val wishDy = vy * moveDist

                val groundTop = if (engine != null) engine.screenH * 0.12f + size else size
                val minX = size
                val maxX = if (engine != null) engine.screenW - size else 10000f
                val minY = groundTop
                val maxY = if (engine != null) engine.screenH - size else 10000f

                val testFullX = (x + wishDx).coerceIn(minX, maxX)
                val testFullY = (y + wishDy).coerceIn(minY, maxY)

                if (engine == null || !engine.isPointOnRiver(testFullX, testFullY)) {
                    // Path clear: advance both axes
                    x = testFullX
                    y = testFullY
                } else {
                    // Diagonal into river: attempt wall-sliding along bank
                    val testXOnly = (x + wishDx).coerceIn(minX, maxX)
                    val testYOnly = (y + wishDy).coerceIn(minY, maxY)

                    val canSlideX = !engine.isPointOnRiver(testXOnly, y)
                    val canSlideY = !engine.isPointOnRiver(x, testYOnly)

                    if (canSlideX && !canSlideY) {
                        x = testXOnly
                    } else if (canSlideY && !canSlideX) {
                        y = testYOnly
                    } else if (canSlideX && canSlideY) {
                        // Whichever has greater displacement
                        if (kotlin.math.abs(wishDx) >= kotlin.math.abs(wishDy)) {
                            x = testXOnly
                        } else {
                            y = testYOnly
                        }
                    }
                    // If neither can slide, player halts against riverbank
                }
            }
            targetX = x
            targetY = y
        } else {
            // Fallback waypoint interpolation for moveTo (tests, cutscenes)
            val maxStep = 0.02f
            var remaining = dt
            while (remaining > 0f) {
                val step = remaining.coerceAtMost(maxStep)
                remaining -= step
                val dx = targetX - x
                val dy = targetY - y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > 5f) {
                    val move = effectiveSpeed * step
                    if (move >= dist) {
                        x = targetX
                        y = targetY
                    } else {
                        x += (dx / dist) * move
                        y += (dy / dist) * move
                    }
                }
            }
        }
        if (attackTimer > 0) attackTimer -= dt
    }

    fun canAttack(): Boolean = attackTimer <= 0f

    fun attack(cooldownMult: Float = 1f) {
        attackTimer = attackCooldown * cooldownMult
    }

    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx; val dy = y - ty
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
