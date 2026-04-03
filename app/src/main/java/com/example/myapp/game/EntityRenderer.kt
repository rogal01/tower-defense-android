package com.example.myapp.game

import android.graphics.*

/**
 * Procedural shape renderer for all game entities.
 * Each enemy type, boss, tower, and player gets a unique hand-drawn look
 * using Canvas Path drawing — no external assets needed.
 */
object EntityRenderer {

    // Thread-local Paint and Path to avoid data races when called from render thread
    private val tlPaint = ThreadLocal<Paint>().apply { set(Paint().apply { isAntiAlias = true }) }
    private val tlStrokePaint = ThreadLocal<Paint>().apply { set(Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }) }
    private val tlPath = ThreadLocal<Path>().apply { set(Path()) }

    private val paint: Paint get() = tlPaint.get() ?: Paint().apply { isAntiAlias = true }.also { tlPaint.set(it) }
    private val strokePaint: Paint get() = tlStrokePaint.get() ?: Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE }.also { tlStrokePaint.set(it) }
    private val path: Path get() = tlPath.get() ?: Path().also { tlPath.set(it) }

    // ========== PLAYER ==========

    fun drawPlayer(canvas: Canvas, x: Float, y: Float, size: Float, hpRatio: Float) {
        // Body — shield-shaped torso
        paint.color = 0xFF42A5F5.toInt()
        path.reset()
        path.moveTo(x, y - size * 0.8f)           // top of head
        path.lineTo(x - size * 0.5f, y - size * 0.2f)
        path.lineTo(x - size * 0.6f, y + size * 0.3f)
        path.lineTo(x - size * 0.3f, y + size * 0.7f)
        path.lineTo(x + size * 0.3f, y + size * 0.7f)
        path.lineTo(x + size * 0.6f, y + size * 0.3f)
        path.lineTo(x + size * 0.5f, y - size * 0.2f)
        path.close()
        canvas.drawPath(path, paint)

        // Head circle
        paint.color = 0xFF90CAF9.toInt()
        canvas.drawCircle(x, y - size * 0.45f, size * 0.35f, paint)

        // Eyes
        paint.color = Color.WHITE
        canvas.drawCircle(x - size * 0.12f, y - size * 0.5f, size * 0.08f, paint)
        canvas.drawCircle(x + size * 0.12f, y - size * 0.5f, size * 0.08f, paint)
        paint.color = 0xFF1565C0.toInt()
        canvas.drawCircle(x - size * 0.12f, y - size * 0.5f, size * 0.04f, paint)
        canvas.drawCircle(x + size * 0.12f, y - size * 0.5f, size * 0.04f, paint)

        // Sword on right side
        paint.color = 0xFFBDBDBD.toInt()
        canvas.drawRect(x + size * 0.55f, y - size * 0.5f, x + size * 0.65f, y + size * 0.4f, paint)
        paint.color = 0xFF795548.toInt()
        canvas.drawRect(x + size * 0.45f, y + size * 0.1f, x + size * 0.75f, y + size * 0.2f, paint)

        // Shield on left side
        paint.color = 0xFF1E88E5.toInt()
        path.reset()
        path.addRoundRect(x - size * 0.85f, y - size * 0.3f, x - size * 0.5f, y + size * 0.3f, 6f, 6f, Path.Direction.CW)
        canvas.drawPath(path, paint)
        strokePaint.color = 0xFFFFD700.toInt()
        strokePaint.strokeWidth = 2f
        canvas.drawPath(path, strokePaint)
    }

    // ========== REGULAR ENEMIES ==========

    fun drawEnemy(canvas: Canvas, enemy: Enemy, frozen: Boolean) {
        val x = enemy.x
        val y = enemy.y
        val s = enemy.size
        val flash = enemy.hitFlash > 0

        if (enemy.type == EnemyType.BOSS && enemy.bossType != null) {
            drawBoss(canvas, enemy, frozen)
            return
        }

        when (enemy.type) {
            EnemyType.GOBLIN -> drawGoblin(canvas, x, y, s, flash, frozen)
            EnemyType.SKELETON -> drawSkeleton(canvas, x, y, s, flash, frozen)
            EnemyType.ORC -> drawOrc(canvas, x, y, s, flash, frozen)
            EnemyType.DEMON -> drawDemon(canvas, x, y, s, flash, frozen)
            EnemyType.DRAGON -> drawDragon(canvas, x, y, s, flash, frozen)
            EnemyType.MINI_ORC -> drawMiniOrc(canvas, x, y, s, flash, frozen)
            EnemyType.MINI_SKELETON -> drawMiniSkeleton(canvas, x, y, s, flash, frozen)
            EnemyType.MINI_DEMON -> drawMiniDemon(canvas, x, y, s, flash, frozen)
            EnemyType.MINI_DRAGON -> drawMiniDragon(canvas, x, y, s, flash, frozen)
            EnemyType.SHADOW -> drawShadow(canvas, x, y, s, flash, frozen)
            EnemyType.SLIME -> drawSlime(canvas, x, y, s, flash, frozen)
            EnemyType.BAT -> drawBat(canvas, x, y, s, flash, frozen)
            EnemyType.SPIDER -> drawSpider(canvas, x, y, s, flash, frozen)
            EnemyType.WISP -> drawWisp(canvas, x, y, s, flash, frozen)
            EnemyType.GOLEM_SHARD -> drawGolemShard(canvas, x, y, s, flash, frozen)
            EnemyType.FAST_SKELETON -> drawFastSkeleton(canvas, x, y, s, flash, frozen)
            EnemyType.ARMORED_GOLEM -> drawArmoredGolem(canvas, x, y, s, flash, frozen)
            EnemyType.BERSERKER -> drawBerserker(canvas, x, y, s, flash, frozen, enemy.berserkerRage)
            EnemyType.COMMANDER -> drawCommander(canvas, x, y, s, flash, frozen)
            EnemyType.SHAPESHIFTER -> drawShapeshifter(canvas, x, y, s, flash, frozen, enemy.shapeshiftPhase)
            else -> drawDefaultCircle(canvas, x, y, s, enemy.displayColor, flash, frozen)
        }
        drawStatusIcons(canvas, enemy, frozen)
    }

    private fun color(baseColor: Int, flash: Boolean, frozen: Boolean): Int = when {
        flash -> Color.WHITE
        frozen -> 0xFF81D4FA.toInt()
        else -> baseColor
    }

    private fun drawStatusIcons(canvas: Canvas, enemy: Enemy, frozen: Boolean) {
        val hasSlow = frozen || enemy.iceSlowFactor < 0.95f || enemy.deepFreezeTimer > 0f
        val hasBurn = enemy.burnTimer > 0f && enemy.burnDps > 10f
        val hasPoison = enemy.burnTimer > 0f && enemy.burnDps in 0.01f..10f
        val hasTar = enemy.tarSlowTimer > 0f
        val hasShield = enemy.shieldTimer > 0f

        val icons = mutableListOf<Int>()
        if (hasSlow)   icons.add(0xFF29B6F6.toInt())  // ice blue
        if (hasBurn)   icons.add(0xFFFF7043.toInt())  // fire orange
        if (hasPoison) icons.add(0xFF81C784.toInt())  // poison green
        if (hasTar)    icons.add(0xFF8D6E63.toInt())  // tar brown
        if (hasShield) icons.add(0xFFFFD700.toInt())  // shield gold
        if (icons.isEmpty()) return

        val iconR = 5f
        val spacing = iconR * 2 + 3f
        val totalW = icons.size * spacing - 3f
        var ix = enemy.x - totalW / 2f + iconR
        val iy = enemy.y - enemy.size - 10f

        for (col in icons) {
            paint.style = Paint.Style.FILL
            paint.color = col
            canvas.drawCircle(ix, iy, iconR, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = 0xAAFFFFFF.toInt()
            canvas.drawCircle(ix, iy, iconR, paint)
            paint.style = Paint.Style.FILL
            ix += spacing
        }
    }

    // --- GOBLIN: small green creature with pointy ears ---
    private fun drawGoblin(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF4CAF50.toInt(), flash, frozen)
        paint.color = col
        // Body
        c.drawCircle(x, y, s * 0.8f, paint)
        // Ears (triangles)
        path.reset()
        path.moveTo(x - s * 0.7f, y - s * 0.3f)
        path.lineTo(x - s * 1.0f, y - s * 1.0f)
        path.lineTo(x - s * 0.3f, y - s * 0.6f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.7f, y - s * 0.3f)
        path.lineTo(x + s * 1.0f, y - s * 1.0f)
        path.lineTo(x + s * 0.3f, y - s * 0.6f)
        path.close()
        c.drawPath(path, paint)
        // Eyes
        paint.color = Color.WHITE
        c.drawCircle(x - s * 0.25f, y - s * 0.15f, s * 0.18f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.15f, s * 0.18f, paint)
        paint.color = Color.RED
        c.drawCircle(x - s * 0.25f, y - s * 0.12f, s * 0.08f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.12f, s * 0.08f, paint)
        // Mouth
        strokePaint.color = 0xFF1B5E20.toInt()
        strokePaint.strokeWidth = 2f
        c.drawArc(x - s * 0.3f, y + s * 0.0f, x + s * 0.3f, y + s * 0.4f, 0f, 180f, false, strokePaint)
    }

    // --- SKELETON: skull-like with bone limbs ---
    private fun drawSkeleton(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFBDBDBD.toInt(), flash, frozen)
        paint.color = col
        // Skull
        c.drawCircle(x, y - s * 0.2f, s * 0.7f, paint)
        // Jaw
        c.drawRect(x - s * 0.4f, y + s * 0.1f, x + s * 0.4f, y + s * 0.5f, paint)
        // Eye sockets (dark)
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * 0.25f, y - s * 0.3f, s * 0.18f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.3f, s * 0.18f, paint)
        // Nose hole
        path.reset()
        path.moveTo(x, y - s * 0.05f)
        path.lineTo(x - s * 0.08f, y + s * 0.1f)
        path.lineTo(x + s * 0.08f, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Teeth
        paint.color = col
        for (i in -2..2) {
            c.drawRect(x + i * s * 0.14f - s * 0.05f, y + s * 0.1f,
                x + i * s * 0.14f + s * 0.05f, y + s * 0.3f, paint)
        }
        // Rib lines
        strokePaint.color = col
        strokePaint.strokeWidth = 2f
        for (i in 0..2) {
            val ry = y + s * 0.5f + i * s * 0.15f
            c.drawLine(x - s * 0.3f, ry, x + s * 0.3f, ry, strokePaint)
        }
    }

    // --- ORC: big, muscular green brute ---
    private fun drawOrc(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF8BC34A.toInt(), flash, frozen)
        // Body (wide)
        paint.color = color(0xFF558B2F.toInt(), flash, frozen)
        c.drawRect(x - s * 0.7f, y - s * 0.1f, x + s * 0.7f, y + s * 0.8f, paint)
        // Head
        paint.color = col
        c.drawCircle(x, y - s * 0.3f, s * 0.65f, paint)
        // Tusks
        paint.color = 0xFFFFFFCC.toInt()
        path.reset()
        path.moveTo(x - s * 0.35f, y + s * 0.0f)
        path.lineTo(x - s * 0.45f, y - s * 0.4f)
        path.lineTo(x - s * 0.25f, y + s * 0.0f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.35f, y + s * 0.0f)
        path.lineTo(x + s * 0.45f, y - s * 0.4f)
        path.lineTo(x + s * 0.25f, y + s * 0.0f)
        path.close()
        c.drawPath(path, paint)
        // Eyes (angry)
        paint.color = 0xFFFF6F00.toInt()
        c.drawCircle(x - s * 0.2f, y - s * 0.35f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.35f, s * 0.12f, paint)
        // Angry brow
        strokePaint.color = 0xFF33691E.toInt()
        strokePaint.strokeWidth = 3f
        c.drawLine(x - s * 0.4f, y - s * 0.55f, x - s * 0.1f, y - s * 0.45f, strokePaint)
        c.drawLine(x + s * 0.4f, y - s * 0.55f, x + s * 0.1f, y - s * 0.45f, strokePaint)
    }

    // --- DEMON: fiery red with horns ---
    private fun drawDemon(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFFF5722.toInt(), flash, frozen)
        paint.color = col
        c.drawCircle(x, y, s * 0.8f, paint)
        // Horns
        paint.color = color(0xFF4E342E.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.5f)
        path.lineTo(x - s * 0.7f, y - s * 1.3f)
        path.lineTo(x - s * 0.1f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.4f, y - s * 0.5f)
        path.lineTo(x + s * 0.7f, y - s * 1.3f)
        path.lineTo(x + s * 0.1f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        // Glowing eyes
        paint.color = 0xFFFFEB3B.toInt()
        c.drawCircle(x - s * 0.25f, y - s * 0.15f, s * 0.14f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.15f, s * 0.14f, paint)
        // Mouth (grin)
        strokePaint.color = 0xFFBF360C.toInt()
        strokePaint.strokeWidth = 2f
        c.drawArc(x - s * 0.35f, y + s * 0.0f, x + s * 0.35f, y + s * 0.5f, 0f, 180f, false, strokePaint)
    }

    // --- DRAGON: winged lizard ---
    private fun drawDragon(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFFF9800.toInt(), flash, frozen)
        // Wings
        paint.color = color(0xFFE65100.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.3f, y - s * 0.2f)
        path.lineTo(x - s * 1.4f, y - s * 0.8f)
        path.lineTo(x - s * 1.0f, y + s * 0.2f)
        path.lineTo(x - s * 0.3f, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.3f, y - s * 0.2f)
        path.lineTo(x + s * 1.4f, y - s * 0.8f)
        path.lineTo(x + s * 1.0f, y + s * 0.2f)
        path.lineTo(x + s * 0.3f, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Body
        paint.color = col
        c.drawOval(x - s * 0.6f, y - s * 0.5f, x + s * 0.6f, y + s * 0.6f, paint)
        // Head
        c.drawCircle(x, y - s * 0.4f, s * 0.4f, paint)
        // Eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.15f, y - s * 0.45f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.15f, y - s * 0.45f, s * 0.1f, paint)
        // Tail
        strokePaint.color = col
        strokePaint.strokeWidth = s * 0.15f
        c.drawArc(x - s * 0.3f, y + s * 0.1f, x + s * 0.8f, y + s * 1.0f, 90f, 120f, false, strokePaint)
    }

    // ========== MINION VARIANTS ==========

    private fun drawMiniOrc(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF689F38.toInt(), flash, frozen)
        paint.color = col
        c.drawCircle(x, y, s * 0.8f, paint)
        // Small tusks
        paint.color = 0xFFFFFFCC.toInt()
        c.drawRect(x - s * 0.25f, y - s * 0.1f, x - s * 0.15f, y - s * 0.35f, paint)
        c.drawRect(x + s * 0.15f, y - s * 0.1f, x + s * 0.25f, y - s * 0.35f, paint)
        // Eyes
        paint.color = 0xFFFF6F00.toInt()
        c.drawCircle(x - s * 0.2f, y - s * 0.15f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.15f, s * 0.1f, paint)
    }

    private fun drawMiniSkeleton(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF9E9E9E.toInt(), flash, frozen)
        paint.color = col
        c.drawCircle(x, y, s * 0.7f, paint)
        // Eye sockets
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * 0.2f, y - s * 0.15f, s * 0.15f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.15f, s * 0.15f, paint)
        // Jaw line
        strokePaint.color = 0xFF616161.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawLine(x - s * 0.25f, y + s * 0.2f, x + s * 0.25f, y + s * 0.2f, strokePaint)
    }

    private fun drawMiniDemon(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFE64A19.toInt(), flash, frozen)
        paint.color = col
        c.drawCircle(x, y, s * 0.75f, paint)
        // Small horns
        paint.color = color(0xFF4E342E.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.3f, y - s * 0.45f)
        path.lineTo(x - s * 0.45f, y - s * 0.9f)
        path.lineTo(x - s * 0.1f, y - s * 0.45f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.3f, y - s * 0.45f)
        path.lineTo(x + s * 0.45f, y - s * 0.9f)
        path.lineTo(x + s * 0.1f, y - s * 0.45f)
        path.close()
        c.drawPath(path, paint)
        // Eyes
        paint.color = 0xFFFFEB3B.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.1f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.1f, s * 0.1f, paint)
    }

    private fun drawMiniDragon(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFFFA726.toInt(), flash, frozen)
        // Small wings
        paint.color = color(0xFFE65100.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.3f, y)
        path.lineTo(x - s * 1.0f, y - s * 0.5f)
        path.lineTo(x - s * 0.5f, y + s * 0.2f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.3f, y)
        path.lineTo(x + s * 1.0f, y - s * 0.5f)
        path.lineTo(x + s * 0.5f, y + s * 0.2f)
        path.close()
        c.drawPath(path, paint)
        // Body
        paint.color = col
        c.drawCircle(x, y, s * 0.65f, paint)
        // Eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.15f, y - s * 0.15f, s * 0.08f, paint)
        c.drawCircle(x + s * 0.15f, y - s * 0.15f, s * 0.08f, paint)
    }

    private fun drawShadow(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF37474F.toInt(), flash, frozen)
        // Ghostly teardrop shape
        paint.color = col
        paint.alpha = 180
        path.reset()
        path.moveTo(x, y - s * 0.9f)
        path.quadTo(x - s * 0.8f, y - s * 0.2f, x - s * 0.5f, y + s * 0.6f)
        path.quadTo(x, y + s * 0.9f, x + s * 0.5f, y + s * 0.6f)
        path.quadTo(x + s * 0.8f, y - s * 0.2f, x, y - s * 0.9f)
        path.close()
        c.drawPath(path, paint)
        paint.alpha = 255
        // Glowing eyes
        paint.color = 0xFFCFD8DC.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.15f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.15f, s * 0.1f, paint)
    }

    private fun drawSlime(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF00C853.toInt(), flash, frozen)
        // Blob body
        paint.color = col
        path.reset()
        path.moveTo(x - s * 0.7f, y + s * 0.3f)
        path.quadTo(x - s * 0.8f, y - s * 0.6f, x, y - s * 0.7f)
        path.quadTo(x + s * 0.8f, y - s * 0.6f, x + s * 0.7f, y + s * 0.3f)
        path.quadTo(x, y + s * 0.5f, x - s * 0.7f, y + s * 0.3f)
        path.close()
        c.drawPath(path, paint)
        // Shine
        paint.color = 0x4DFFFFFF
        c.drawCircle(x - s * 0.15f, y - s * 0.3f, s * 0.15f, paint)
        // Eyes
        paint.color = Color.WHITE
        c.drawCircle(x - s * 0.18f, y - s * 0.1f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.1f, s * 0.12f, paint)
        paint.color = Color.BLACK
        c.drawCircle(x - s * 0.18f, y - s * 0.08f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.08f, s * 0.06f, paint)
    }

    private fun drawBat(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF4A148C.toInt(), flash, frozen)
        // Wings
        paint.color = col
        path.reset()
        path.moveTo(x, y)
        path.lineTo(x - s * 1.2f, y - s * 0.6f)
        path.lineTo(x - s * 0.8f, y + s * 0.1f)
        path.lineTo(x - s * 0.4f, y - s * 0.2f)
        path.lineTo(x, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x, y)
        path.lineTo(x + s * 1.2f, y - s * 0.6f)
        path.lineTo(x + s * 0.8f, y + s * 0.1f)
        path.lineTo(x + s * 0.4f, y - s * 0.2f)
        path.lineTo(x, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Body
        paint.color = color(0xFF6A1B9A.toInt(), flash, frozen)
        c.drawCircle(x, y, s * 0.4f, paint)
        // Eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.12f, y - s * 0.08f, s * 0.07f, paint)
        c.drawCircle(x + s * 0.12f, y - s * 0.08f, s * 0.07f, paint)
        // Ears
        path.reset()
        path.moveTo(x - s * 0.15f, y - s * 0.3f)
        path.lineTo(x - s * 0.25f, y - s * 0.65f)
        path.lineTo(x - s * 0.05f, y - s * 0.35f)
        path.close()
        paint.color = col
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.15f, y - s * 0.3f)
        path.lineTo(x + s * 0.25f, y - s * 0.65f)
        path.lineTo(x + s * 0.05f, y - s * 0.35f)
        path.close()
        c.drawPath(path, paint)
    }

    private fun drawSpider(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF4E342E.toInt(), flash, frozen)
        // 8 legs
        strokePaint.color = col
        strokePaint.strokeWidth = 2f
        for (i in 0..3) {
            val angle = (-0.6f + i * 0.4f)
            val lx = s * 1.1f * kotlin.math.cos(angle)
            val ly = s * 0.8f * kotlin.math.sin(angle)
            c.drawLine(x, y, x - lx, y + ly, strokePaint)
            c.drawLine(x, y, x + lx, y + ly, strokePaint)
        }
        // Body (two ovals)
        paint.color = col
        c.drawCircle(x, y - s * 0.15f, s * 0.4f, paint)
        c.drawOval(x - s * 0.35f, y + s * 0.0f, x + s * 0.35f, y + s * 0.55f, paint)
        // Eyes (multiple tiny ones)
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.12f, y - s * 0.25f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.12f, y - s * 0.25f, s * 0.06f, paint)
        c.drawCircle(x - s * 0.06f, y - s * 0.18f, s * 0.04f, paint)
        c.drawCircle(x + s * 0.06f, y - s * 0.18f, s * 0.04f, paint)
    }

    private fun drawWisp(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF00BCD4.toInt(), flash, frozen)
        // Outer glow
        paint.color = col
        paint.alpha = 60
        c.drawCircle(x, y, s * 1.1f, paint)
        paint.alpha = 120
        c.drawCircle(x, y, s * 0.8f, paint)
        paint.alpha = 255
        // Core
        paint.color = Color.WHITE
        c.drawCircle(x, y, s * 0.35f, paint)
        // Inner sparkle
        paint.color = col
        c.drawCircle(x, y, s * 0.2f, paint)
    }

    private fun drawGolemShard(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF795548.toInt(), flash, frozen)
        // Irregular rock shape
        paint.color = col
        path.reset()
        path.moveTo(x, y - s * 0.8f)
        path.lineTo(x + s * 0.6f, y - s * 0.4f)
        path.lineTo(x + s * 0.7f, y + s * 0.3f)
        path.lineTo(x + s * 0.2f, y + s * 0.7f)
        path.lineTo(x - s * 0.4f, y + s * 0.6f)
        path.lineTo(x - s * 0.7f, y + s * 0.1f)
        path.lineTo(x - s * 0.5f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        // Crack lines
        strokePaint.color = 0xFF3E2723.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawLine(x - s * 0.1f, y - s * 0.5f, x + s * 0.15f, y + s * 0.3f, strokePaint)
        c.drawLine(x + s * 0.15f, y + s * 0.3f, x - s * 0.2f, y + s * 0.5f, strokePaint)
        // Glowing core dot
        paint.color = 0xFFFF6F00.toInt()
        c.drawCircle(x, y, s * 0.12f, paint)
    }

    // --- FAST SKELETON: lean skeleton with speed lines ---
    private fun drawFastSkeleton(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFE0E0E0.toInt(), flash, frozen)
        paint.color = col
        // Slim skull
        c.drawOval(x - s * 0.5f, y - s * 0.7f, x + s * 0.5f, y + s * 0.2f, paint)
        // Dark eye sockets
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.3f, s * 0.14f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.3f, s * 0.14f, paint)
        // Red glowing eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.3f, s * 0.07f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.3f, s * 0.07f, paint)
        // Thin body
        paint.color = col
        c.drawRect(x - s * 0.2f, y + s * 0.1f, x + s * 0.2f, y + s * 0.6f, paint)
        // Speed streaks behind
        strokePaint.color = 0x66FFFFFF
        strokePaint.strokeWidth = 2f
        c.drawLine(x - s * 1.0f, y - s * 0.1f, x - s * 0.6f, y - s * 0.1f, strokePaint)
        c.drawLine(x - s * 0.9f, y + s * 0.15f, x - s * 0.5f, y + s * 0.15f, strokePaint)
        c.drawLine(x - s * 1.1f, y + s * 0.4f, x - s * 0.6f, y + s * 0.4f, strokePaint)
    }

    // --- ARMORED GOLEM: big rocky body with armor plates ---
    private fun drawArmoredGolem(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF6D4C41.toInt(), flash, frozen)
        // Large body
        paint.color = col
        path.reset()
        path.moveTo(x, y - s * 0.9f)
        path.lineTo(x + s * 0.8f, y - s * 0.3f)
        path.lineTo(x + s * 0.9f, y + s * 0.5f)
        path.lineTo(x + s * 0.4f, y + s * 0.9f)
        path.lineTo(x - s * 0.4f, y + s * 0.9f)
        path.lineTo(x - s * 0.9f, y + s * 0.5f)
        path.lineTo(x - s * 0.8f, y - s * 0.3f)
        path.close()
        c.drawPath(path, paint)
        // Armor plates (lighter)
        paint.color = color(0xFF8D6E63.toInt(), flash, frozen)
        c.drawRect(x - s * 0.5f, y - s * 0.4f, x + s * 0.5f, y + s * 0.1f, paint)
        c.drawRect(x - s * 0.4f, y + s * 0.2f, x + s * 0.4f, y + s * 0.6f, paint)
        // Glowing eyes
        paint.color = 0xFFFFAB00.toInt()
        c.drawCircle(x - s * 0.25f, y - s * 0.2f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.2f, s * 0.12f, paint)
        // Cracks
        strokePaint.color = 0xFF3E2723.toInt()
        strokePaint.strokeWidth = 2f
        c.drawLine(x - s * 0.3f, y - s * 0.6f, x, y - s * 0.1f, strokePaint)
        c.drawLine(x + s * 0.2f, y - s * 0.5f, x + s * 0.4f, y + s * 0.3f, strokePaint)
    }

    private fun drawDefaultCircle(c: Canvas, x: Float, y: Float, s: Float, baseColor: Int, flash: Boolean, frozen: Boolean) {
        paint.color = color(baseColor, flash, frozen)
        c.drawCircle(x, y, s, paint)
    }

    // ========== BOSSES ==========

    private fun drawBoss(canvas: Canvas, enemy: Enemy, frozen: Boolean) {
        val boss = enemy.bossType ?: return
        val x = enemy.x
        val y = enemy.y
        val s = enemy.size
        val flash = enemy.hitFlash > 0

        // Boss aura ring
        paint.color = boss.color
        paint.alpha = 40
        canvas.drawCircle(x, y, s * 1.4f, paint)
        paint.alpha = 255

        when (boss) {
            BossType.ORC_KING -> drawBossOrcKing(canvas, x, y, s, flash, frozen)
            BossType.LICH_LORD -> drawBossLichLord(canvas, x, y, s, flash, frozen)
            BossType.DEMON_PRINCE -> drawBossDemonPrince(canvas, x, y, s, flash, frozen)
            BossType.DRAGON_QUEEN -> drawBossDragonQueen(canvas, x, y, s, flash, frozen)
            BossType.SHADOW_WRAITH -> drawBossShadowWraith(canvas, x, y, s, flash, frozen)
            BossType.SLIME_KING -> drawBossSlimeKing(canvas, x, y, s, flash, frozen)
            BossType.VAMPIRE_LORD -> drawBossVampireLord(canvas, x, y, s, flash, frozen)
            BossType.SPIDER_QUEEN -> drawBossSpiderQueen(canvas, x, y, s, flash, frozen)
            BossType.FROST_TITAN -> drawBossFrostTitan(canvas, x, y, s, flash, frozen)
            BossType.STONE_GOLEM -> drawBossStoneGolem(canvas, x, y, s, flash, frozen)
        }
    }

    private fun drawBossOrcKing(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF33691E.toInt(), flash, frozen)
        // Huge body
        paint.color = color(0xFF1B5E20.toInt(), flash, frozen)
        c.drawRect(x - s * 0.8f, y - s * 0.1f, x + s * 0.8f, y + s * 0.9f, paint)
        // Head
        paint.color = col
        c.drawCircle(x, y - s * 0.3f, s * 0.7f, paint)
        // Crown
        paint.color = 0xFFFFD700.toInt()
        path.reset()
        path.moveTo(x - s * 0.5f, y - s * 0.7f)
        path.lineTo(x - s * 0.5f, y - s * 1.1f)
        path.lineTo(x - s * 0.25f, y - s * 0.85f)
        path.lineTo(x, y - s * 1.15f)
        path.lineTo(x + s * 0.25f, y - s * 0.85f)
        path.lineTo(x + s * 0.5f, y - s * 1.1f)
        path.lineTo(x + s * 0.5f, y - s * 0.7f)
        path.close()
        c.drawPath(path, paint)
        // Big tusks
        paint.color = 0xFFFFFFCC.toInt()
        path.reset()
        path.moveTo(x - s * 0.4f, y + s * 0.0f)
        path.lineTo(x - s * 0.55f, y - s * 0.55f)
        path.lineTo(x - s * 0.25f, y + s * 0.0f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.4f, y + s * 0.0f)
        path.lineTo(x + s * 0.55f, y - s * 0.55f)
        path.lineTo(x + s * 0.25f, y + s * 0.0f)
        path.close()
        c.drawPath(path, paint)
        // Angry eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.22f, y - s * 0.35f, s * 0.14f, paint)
        c.drawCircle(x + s * 0.22f, y - s * 0.35f, s * 0.14f, paint)
    }

    private fun drawBossLichLord(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF6A1B9A.toInt(), flash, frozen)
        // Robe
        paint.color = color(0xFF4A148C.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.2f)
        path.lineTo(x - s * 0.7f, y + s * 0.9f)
        path.lineTo(x + s * 0.7f, y + s * 0.9f)
        path.lineTo(x + s * 0.4f, y - s * 0.2f)
        path.close()
        c.drawPath(path, paint)
        // Skull head
        paint.color = color(0xFFE0E0E0.toInt(), flash, frozen)
        c.drawCircle(x, y - s * 0.35f, s * 0.55f, paint)
        // Eye sockets with glow
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * 0.2f, y - s * 0.4f, s * 0.16f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.4f, s * 0.16f, paint)
        paint.color = col
        c.drawCircle(x - s * 0.2f, y - s * 0.4f, s * 0.08f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.4f, s * 0.08f, paint)
        // Staff
        strokePaint.color = 0xFF795548.toInt()
        strokePaint.strokeWidth = 4f
        c.drawLine(x + s * 0.7f, y - s * 0.8f, x + s * 0.7f, y + s * 0.8f, strokePaint)
        paint.color = col
        c.drawCircle(x + s * 0.7f, y - s * 0.8f, s * 0.15f, paint)
    }

    private fun drawBossDemonPrince(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFB71C1C.toInt(), flash, frozen)
        paint.color = col
        c.drawCircle(x, y, s * 0.85f, paint)
        // Large horns
        paint.color = color(0xFF212121.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.45f, y - s * 0.55f)
        path.lineTo(x - s * 0.85f, y - s * 1.4f)
        path.lineTo(x - s * 0.1f, y - s * 0.55f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.45f, y - s * 0.55f)
        path.lineTo(x + s * 0.85f, y - s * 1.4f)
        path.lineTo(x + s * 0.1f, y - s * 0.55f)
        path.close()
        c.drawPath(path, paint)
        // Fire eyes
        paint.color = 0xFFFF9100.toInt()
        c.drawCircle(x - s * 0.25f, y - s * 0.15f, s * 0.16f, paint)
        c.drawCircle(x + s * 0.25f, y - s * 0.15f, s * 0.16f, paint)
        // Demonic grin
        strokePaint.color = 0xFFFF6F00.toInt()
        strokePaint.strokeWidth = 3f
        c.drawArc(x - s * 0.4f, y + s * 0.05f, x + s * 0.4f, y + s * 0.55f, 0f, 180f, false, strokePaint)
        // Wings
        paint.color = color(0xFF880E4F.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.5f, y)
        path.lineTo(x - s * 1.3f, y - s * 0.7f)
        path.lineTo(x - s * 0.9f, y + s * 0.4f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.5f, y)
        path.lineTo(x + s * 1.3f, y - s * 0.7f)
        path.lineTo(x + s * 0.9f, y + s * 0.4f)
        path.close()
        c.drawPath(path, paint)
    }

    private fun drawBossDragonQueen(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFFE65100.toInt(), flash, frozen)
        // Massive wings
        paint.color = color(0xFFBF360C.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.2f)
        path.lineTo(x - s * 1.6f, y - s * 1.0f)
        path.lineTo(x - s * 1.3f, y + s * 0.3f)
        path.lineTo(x - s * 0.4f, y + s * 0.2f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.4f, y - s * 0.2f)
        path.lineTo(x + s * 1.6f, y - s * 1.0f)
        path.lineTo(x + s * 1.3f, y + s * 0.3f)
        path.lineTo(x + s * 0.4f, y + s * 0.2f)
        path.close()
        c.drawPath(path, paint)
        // Body
        paint.color = col
        c.drawOval(x - s * 0.7f, y - s * 0.6f, x + s * 0.7f, y + s * 0.7f, paint)
        // Head with crest
        c.drawCircle(x, y - s * 0.45f, s * 0.5f, paint)
        paint.color = 0xFFFFD700.toInt()
        path.reset()
        path.moveTo(x - s * 0.3f, y - s * 0.8f)
        path.lineTo(x, y - s * 1.2f)
        path.lineTo(x + s * 0.3f, y - s * 0.8f)
        path.close()
        c.drawPath(path, paint)
        // Eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.5f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.5f, s * 0.12f, paint)
    }

    private fun drawBossShadowWraith(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF263238.toInt(), flash, frozen)
        // Wispy cloak
        paint.color = col
        paint.alpha = 150
        path.reset()
        path.moveTo(x, y - s * 0.9f)
        path.quadTo(x - s * 1.0f, y - s * 0.2f, x - s * 0.7f, y + s * 0.8f)
        path.quadTo(x - s * 0.3f, y + s * 0.6f, x, y + s * 1.0f)
        path.quadTo(x + s * 0.3f, y + s * 0.6f, x + s * 0.7f, y + s * 0.8f)
        path.quadTo(x + s * 1.0f, y - s * 0.2f, x, y - s * 0.9f)
        path.close()
        c.drawPath(path, paint)
        paint.alpha = 255
        // Hood
        paint.color = color(0xFF37474F.toInt(), flash, frozen)
        c.drawOval(x - s * 0.5f, y - s * 0.9f, x + s * 0.5f, y - s * 0.1f, paint)
        // Glowing eyes
        paint.color = 0xFF80DEEA.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.5f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.5f, s * 0.12f, paint)
        // Scythe
        strokePaint.color = 0xFFBDBDBD.toInt()
        strokePaint.strokeWidth = 3f
        c.drawLine(x - s * 0.6f, y - s * 0.7f, x - s * 0.6f, y + s * 0.7f, strokePaint)
        path.reset()
        path.moveTo(x - s * 0.6f, y - s * 0.7f)
        path.quadTo(x - s * 0.2f, y - s * 0.85f, x - s * 0.1f, y - s * 0.45f)
        strokePaint.strokeWidth = 3f
        c.drawPath(path, strokePaint)
    }

    private fun drawBossSlimeKing(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF00E676.toInt(), flash, frozen)
        // Big blob body
        paint.color = col
        path.reset()
        path.moveTo(x - s * 0.9f, y + s * 0.4f)
        path.quadTo(x - s * 1.0f, y - s * 0.7f, x, y - s * 0.85f)
        path.quadTo(x + s * 1.0f, y - s * 0.7f, x + s * 0.9f, y + s * 0.4f)
        path.quadTo(x, y + s * 0.7f, x - s * 0.9f, y + s * 0.4f)
        path.close()
        c.drawPath(path, paint)
        // Crown
        paint.color = 0xFFFFD700.toInt()
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.6f)
        path.lineTo(x - s * 0.4f, y - s * 0.95f)
        path.lineTo(x - s * 0.15f, y - s * 0.75f)
        path.lineTo(x, y - s * 1.0f)
        path.lineTo(x + s * 0.15f, y - s * 0.75f)
        path.lineTo(x + s * 0.4f, y - s * 0.95f)
        path.lineTo(x + s * 0.4f, y - s * 0.6f)
        path.close()
        c.drawPath(path, paint)
        // Shine
        paint.color = 0x4DFFFFFF
        c.drawCircle(x - s * 0.2f, y - s * 0.35f, s * 0.2f, paint)
        // Big eyes
        paint.color = Color.WHITE
        c.drawCircle(x - s * 0.22f, y - s * 0.15f, s * 0.18f, paint)
        c.drawCircle(x + s * 0.22f, y - s * 0.15f, s * 0.18f, paint)
        paint.color = Color.BLACK
        c.drawCircle(x - s * 0.22f, y - s * 0.1f, s * 0.09f, paint)
        c.drawCircle(x + s * 0.22f, y - s * 0.1f, s * 0.09f, paint)
    }

    private fun drawBossVampireLord(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF880E4F.toInt(), flash, frozen)
        // Cape
        paint.color = color(0xFF4A148C.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.3f)
        path.lineTo(x - s * 0.9f, y + s * 0.9f)
        path.lineTo(x + s * 0.9f, y + s * 0.9f)
        path.lineTo(x + s * 0.4f, y - s * 0.3f)
        path.close()
        c.drawPath(path, paint)
        // Body
        paint.color = color(0xFF212121.toInt(), flash, frozen)
        c.drawRect(x - s * 0.35f, y - s * 0.1f, x + s * 0.35f, y + s * 0.6f, paint)
        // Head (pale)
        paint.color = color(0xFFE0E0E0.toInt(), flash, frozen)
        c.drawCircle(x, y - s * 0.35f, s * 0.5f, paint)
        // Hair
        paint.color = color(0xFF212121.toInt(), flash, frozen)
        c.drawArc(x - s * 0.5f, y - s * 0.85f, x + s * 0.5f, y - s * 0.2f, 180f, 180f, true, paint)
        // Red eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.38f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.38f, s * 0.1f, paint)
        // Fangs
        paint.color = Color.WHITE
        path.reset()
        path.moveTo(x - s * 0.1f, y - s * 0.1f)
        path.lineTo(x - s * 0.07f, y + s * 0.08f)
        path.lineTo(x - s * 0.04f, y - s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.1f, y - s * 0.1f)
        path.lineTo(x + s * 0.07f, y + s * 0.08f)
        path.lineTo(x + s * 0.04f, y - s * 0.1f)
        path.close()
        c.drawPath(path, paint)
    }

    private fun drawBossSpiderQueen(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF3E2723.toInt(), flash, frozen)
        // 8 thick legs
        strokePaint.color = col
        strokePaint.strokeWidth = 4f
        for (i in 0..3) {
            val angle = (-0.7f + i * 0.45f)
            val lx = s * 1.6f * kotlin.math.cos(angle)
            val ly = s * 1.1f * kotlin.math.sin(angle)
            c.drawLine(x, y, x - lx, y + ly, strokePaint)
            c.drawLine(x, y, x + lx, y + ly, strokePaint)
        }
        // Thorax
        paint.color = col
        c.drawOval(x - s * 0.55f, y + s * 0.0f, x + s * 0.55f, y + s * 0.8f, paint)
        // Pattern on thorax
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x, y + s * 0.3f, s * 0.12f, paint)
        // Head
        paint.color = col
        c.drawCircle(x, y - s * 0.25f, s * 0.5f, paint)
        // 6 eyes
        paint.color = 0xFFFF1744.toInt()
        c.drawCircle(x - s * 0.2f, y - s * 0.35f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.2f, y - s * 0.35f, s * 0.1f, paint)
        c.drawCircle(x - s * 0.1f, y - s * 0.22f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.1f, y - s * 0.22f, s * 0.06f, paint)
        c.drawCircle(x - s * 0.3f, y - s * 0.25f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.3f, y - s * 0.25f, s * 0.06f, paint)
        // Crown
        paint.color = 0xFFFFD700.toInt()
        path.reset()
        path.moveTo(x - s * 0.3f, y - s * 0.55f)
        path.lineTo(x - s * 0.2f, y - s * 0.8f)
        path.lineTo(x, y - s * 0.65f)
        path.lineTo(x + s * 0.2f, y - s * 0.8f)
        path.lineTo(x + s * 0.3f, y - s * 0.55f)
        path.close()
        c.drawPath(path, paint)
    }

    private fun drawBossFrostTitan(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF0288D1.toInt(), flash, frozen)
        // Icy body (broad, angular)
        paint.color = col
        path.reset()
        path.moveTo(x - s * 0.8f, y + s * 0.85f)
        path.lineTo(x - s * 0.65f, y - s * 0.3f)
        path.lineTo(x - s * 0.3f, y - s * 0.8f)
        path.lineTo(x + s * 0.3f, y - s * 0.8f)
        path.lineTo(x + s * 0.65f, y - s * 0.3f)
        path.lineTo(x + s * 0.8f, y + s * 0.85f)
        path.close()
        c.drawPath(path, paint)
        // Ice crystals on shoulders
        paint.color = color(0xFF4FC3F7.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.65f, y - s * 0.3f)
        path.lineTo(x - s * 1.1f, y - s * 0.8f)
        path.lineTo(x - s * 0.5f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        path.reset()
        path.moveTo(x + s * 0.65f, y - s * 0.3f)
        path.lineTo(x + s * 1.1f, y - s * 0.8f)
        path.lineTo(x + s * 0.5f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        // Head
        paint.color = col
        c.drawCircle(x, y - s * 0.55f, s * 0.35f, paint)
        // Glowing ice eyes
        paint.color = Color.WHITE
        c.drawCircle(x - s * 0.12f, y - s * 0.58f, s * 0.1f, paint)
        c.drawCircle(x + s * 0.12f, y - s * 0.58f, s * 0.1f, paint)
        paint.color = 0xFF00E5FF.toInt()
        c.drawCircle(x - s * 0.12f, y - s * 0.58f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.12f, y - s * 0.58f, s * 0.06f, paint)
    }

    private fun drawBossStoneGolem(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        val col = color(0xFF5D4037.toInt(), flash, frozen)
        // Body — large blocky shape
        paint.color = col
        c.drawRect(x - s * 0.7f, y - s * 0.3f, x + s * 0.7f, y + s * 0.85f, paint)
        // Shoulders
        c.drawRect(x - s * 0.95f, y - s * 0.2f, x - s * 0.55f, y + s * 0.4f, paint)
        c.drawRect(x + s * 0.55f, y - s * 0.2f, x + s * 0.95f, y + s * 0.4f, paint)
        // Head (smaller block)
        paint.color = color(0xFF795548.toInt(), flash, frozen)
        c.drawRect(x - s * 0.4f, y - s * 0.85f, x + s * 0.4f, y - s * 0.25f, paint)
        // Glowing rune eyes
        paint.color = 0xFFFF6F00.toInt()
        c.drawCircle(x - s * 0.18f, y - s * 0.55f, s * 0.12f, paint)
        c.drawCircle(x + s * 0.18f, y - s * 0.55f, s * 0.12f, paint)
        // Chest rune
        strokePaint.color = 0xFFFF6F00.toInt()
        strokePaint.strokeWidth = 3f
        c.drawLine(x, y - s * 0.1f, x - s * 0.2f, y + s * 0.2f, strokePaint)
        c.drawLine(x, y - s * 0.1f, x + s * 0.2f, y + s * 0.2f, strokePaint)
        c.drawLine(x - s * 0.2f, y + s * 0.2f, x + s * 0.2f, y + s * 0.2f, strokePaint)
        c.drawLine(x, y + s * 0.2f, x, y + s * 0.5f, strokePaint)
        // Cracks
        strokePaint.color = 0xFF3E2723.toInt()
        strokePaint.strokeWidth = 2f
        c.drawLine(x - s * 0.5f, y + s * 0.0f, x - s * 0.3f, y + s * 0.5f, strokePaint)
        c.drawLine(x + s * 0.4f, y - s * 0.1f, x + s * 0.6f, y + s * 0.4f, strokePaint)
    }

    // ========== TOWERS ==========

    fun drawTower(canvas: Canvas, tower: Tower, selected: Boolean) {
        val x = tower.x
        val y = tower.y
        val s = tower.size

        when (tower.type) {
            TowerType.ARROW -> drawTowerArrow(canvas, x, y, s)
            TowerType.MAGIC -> drawTowerMagic(canvas, x, y, s)
            TowerType.CANNON -> drawTowerCannon(canvas, x, y, s)
            TowerType.POISON -> drawTowerPoison(canvas, x, y, s)
            TowerType.TESLA -> drawTowerTesla(canvas, x, y, s)
            TowerType.ICE -> drawTowerIce(canvas, x, y, s)
            TowerType.FLAME -> drawTowerFlame(canvas, x, y, s)
            TowerType.NECRO -> drawTowerNecro(canvas, x, y, s)
            TowerType.BALLISTA -> drawTowerBallista(canvas, x, y, s)
            TowerType.VORTEX -> drawTowerVortex(canvas, x, y, s)
            TowerType.HEALER -> drawTowerHealer(canvas, x, y, s)
        }
    }

    private fun drawTowerArrow(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Wooden base
        paint.color = 0xFF795548.toInt()
        c.drawRect(x - s * 0.35f, y - s * 0.1f, x + s * 0.35f, y + s * 0.5f, paint)
        // Platform top
        paint.color = 0xFF5D4037.toInt()
        c.drawRect(x - s * 0.45f, y - s * 0.2f, x + s * 0.45f, y - s * 0.05f, paint)
        // Bow — gentle wobble
        val bowWobble = (Math.sin(t / 400.0) * 3f).toFloat()
        strokePaint.color = 0xFF8D6E63.toInt()
        strokePaint.strokeWidth = 3f
        c.drawArc(x - s * 0.3f + bowWobble, y - s * 0.7f, x + s * 0.35f + bowWobble, y + s * 0.0f, -120f, 240f, false, strokePaint)
        // Arrow
        paint.color = 0xFFFFEB3B.toInt()
        c.drawRect(x - s * 0.03f, y - s * 0.6f, x + s * 0.03f, y - s * 0.15f, paint)
        // Arrow tip
        path.reset()
        path.moveTo(x, y - s * 0.75f)
        path.lineTo(x - s * 0.08f, y - s * 0.6f)
        path.lineTo(x + s * 0.08f, y - s * 0.6f)
        path.close()
        c.drawPath(path, paint)
    }

    private fun drawTowerMagic(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Crystal base
        paint.color = 0xFF7B1FA2.toInt()
        path.reset()
        path.moveTo(x - s * 0.35f, y + s * 0.5f)
        path.lineTo(x - s * 0.2f, y - s * 0.1f)
        path.lineTo(x + s * 0.2f, y - s * 0.1f)
        path.lineTo(x + s * 0.35f, y + s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        // Crystal
        paint.color = 0xFFCE93D8.toInt()
        path.reset()
        path.moveTo(x, y - s * 0.8f)
        path.lineTo(x - s * 0.25f, y - s * 0.3f)
        path.lineTo(x, y - s * 0.1f)
        path.lineTo(x + s * 0.25f, y - s * 0.3f)
        path.close()
        c.drawPath(path, paint)
        // Inner glow — pulsing
        val glowAlpha = (180 + 75 * Math.sin(t / 500.0)).toInt()
        paint.color = (glowAlpha shl 24) or 0x00E1BEE7
        val glowSize = s * (0.1f + 0.03f * Math.sin(t / 500.0).toFloat())
        c.drawCircle(x, y - s * 0.4f, glowSize, paint)
        // Sparkle — rotating cross
        strokePaint.color = 0xFFFFFFFF.toInt()
        strokePaint.strokeWidth = 1.5f
        val sparkAngle = (t % 2000 / 2000.0 * Math.PI).toFloat()
        val sx = s * 0.15f
        c.drawLine(x - sx * Math.cos(sparkAngle.toDouble()).toFloat(), y - s * 0.55f + sx * Math.sin(sparkAngle.toDouble()).toFloat(),
            x + sx * Math.cos(sparkAngle.toDouble()).toFloat(), y - s * 0.25f - sx * Math.sin(sparkAngle.toDouble()).toFloat(), strokePaint)
        c.drawLine(x + sx * Math.sin(sparkAngle.toDouble()).toFloat(), y - s * 0.55f + sx * Math.cos(sparkAngle.toDouble()).toFloat(),
            x - sx * Math.sin(sparkAngle.toDouble()).toFloat(), y - s * 0.25f - sx * Math.cos(sparkAngle.toDouble()).toFloat(), strokePaint)
    }

    private fun drawTowerCannon(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Base platform
        paint.color = 0xFF424242.toInt()
        c.drawRect(x - s * 0.4f, y + s * 0.1f, x + s * 0.4f, y + s * 0.5f, paint)
        // Barrel — subtle heat shimmer
        val shimmer = (Math.sin(t / 300.0) * 1.5f).toFloat()
        paint.color = 0xFF616161.toInt()
        c.drawRect(x - s * 0.15f + shimmer, y - s * 0.7f, x + s * 0.15f + shimmer, y + s * 0.15f, paint)
        // Barrel tip — smoke wisp
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x + shimmer, y - s * 0.7f, s * 0.18f, paint)
        // Smoke wisps from barrel
        val smokeAlpha = (60 + 40 * Math.sin(t / 600.0)).toInt()
        paint.color = (smokeAlpha shl 24) or 0x00888888
        val smokeY = y - s * 0.85f - (t % 1500 / 1500f) * s * 0.3f
        c.drawCircle(x + shimmer, smokeY, s * 0.08f, paint)
        // Wheel
        strokePaint.color = 0xFF795548.toInt()
        strokePaint.strokeWidth = 3f
        c.drawCircle(x - s * 0.3f, y + s * 0.35f, s * 0.15f, strokePaint)
        c.drawCircle(x + s * 0.3f, y + s * 0.35f, s * 0.15f, strokePaint)
    }

    private fun drawTowerPoison(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Cauldron
        paint.color = 0xFF424242.toInt()
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.1f)
        path.lineTo(x - s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.4f, y - s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Poison liquid — wobbling surface
        val wobble = (Math.sin(t / 350.0) * s * 0.02f).toFloat()
        paint.color = 0xFF66BB6A.toInt()
        c.drawOval(x - s * 0.35f, y - s * 0.2f + wobble, x + s * 0.35f, y + s * 0.05f + wobble, paint)
        // Animated bubbles rising
        paint.color = 0xFF81C784.toInt()
        val b1y = y - s * 0.2f - (t % 1200 / 1200f) * s * 0.4f
        val b2y = y - s * 0.15f - ((t + 400) % 1000 / 1000f) * s * 0.5f
        val b3y = y - s * 0.18f - ((t + 800) % 1400 / 1400f) * s * 0.35f
        val b1a = (255 * (1f - (t % 1200 / 1200f))).toInt()
        val b2a = (255 * (1f - ((t + 400) % 1000 / 1000f))).toInt()
        val b3a = (255 * (1f - ((t + 800) % 1400 / 1400f))).toInt()
        paint.alpha = b1a; c.drawCircle(x - s * 0.12f, b1y, s * 0.07f, paint)
        paint.alpha = b2a; c.drawCircle(x + s * 0.08f, b2y, s * 0.05f, paint)
        paint.alpha = b3a; c.drawCircle(x + s * 0.15f, b3y, s * 0.06f, paint)
        paint.alpha = 255
        // Skull symbol
        paint.color = 0xFFFFFFFF.toInt()
        c.drawCircle(x, y + s * 0.2f, s * 0.1f, paint)
        paint.color = 0xFF424242.toInt()
        c.drawCircle(x - s * 0.04f, y + s * 0.18f, s * 0.03f, paint)
        c.drawCircle(x + s * 0.04f, y + s * 0.18f, s * 0.03f, paint)
    }

    private fun drawTowerTesla(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Coil base
        paint.color = 0xFF37474F.toInt()
        c.drawRect(x - s * 0.3f, y + s * 0.1f, x + s * 0.3f, y + s * 0.5f, paint)
        // Coil pillar
        paint.color = 0xFF546E7A.toInt()
        c.drawRect(x - s * 0.1f, y - s * 0.6f, x + s * 0.1f, y + s * 0.15f, paint)
        // Coil rings
        strokePaint.color = 0xFF29B6F6.toInt()
        strokePaint.strokeWidth = 2.5f
        for (i in 0..3) {
            val ry = y - s * 0.1f - i * s * 0.15f
            c.drawOval(x - s * 0.2f, ry - s * 0.04f, x + s * 0.2f, ry + s * 0.04f, strokePaint)
        }
        // Tesla ball on top — pulsing glow
        val glowSize = s * (0.15f + 0.03f * Math.sin(t / 200.0).toFloat())
        paint.color = 0xFF29B6F6.toInt()
        c.drawCircle(x, y - s * 0.7f, glowSize, paint)
        // Animated electric arcs — random jitter
        strokePaint.color = 0xFFFFEB3B.toInt()
        strokePaint.strokeWidth = 1.5f
        val jitter1 = ((t * 7 % 17) / 17f - 0.5f) * s * 0.2f
        val jitter2 = ((t * 11 % 19) / 19f - 0.5f) * s * 0.2f
        val jitter3 = ((t * 13 % 23) / 23f - 0.5f) * s * 0.15f
        c.drawLine(x - s * 0.3f + jitter1, y - s * 0.8f, x - s * 0.15f, y - s * 0.65f, strokePaint)
        c.drawLine(x + s * 0.3f + jitter2, y - s * 0.8f, x + s * 0.15f, y - s * 0.65f, strokePaint)
        c.drawLine(x + jitter3, y - s * 0.9f, x, y - s * 0.75f, strokePaint)
        // Extra arc from ball to coil
        if (t % 400 < 200) {
            strokePaint.color = 0x99FFFFFF.toInt()
            val midX = x + ((t * 3 % 13) / 13f - 0.5f).toFloat() * s * 0.15f
            c.drawLine(x, y - s * 0.7f, midX, y - s * 0.4f, strokePaint)
        }
    }

    private fun drawTowerIce(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Frozen stone base
        paint.color = 0xFF546E7A.toInt()
        c.drawRect(x - s * 0.35f, y + s * 0.15f, x + s * 0.35f, y + s * 0.5f, paint)
        // Ice crystal — hexagonal shape
        paint.color = 0xFF81D4FA.toInt()
        path.reset()
        path.moveTo(x, y - s * 0.85f)
        path.lineTo(x + s * 0.25f, y - s * 0.5f)
        path.lineTo(x + s * 0.25f, y - s * 0.1f)
        path.lineTo(x, y + s * 0.15f)
        path.lineTo(x - s * 0.25f, y - s * 0.1f)
        path.lineTo(x - s * 0.25f, y - s * 0.5f)
        path.close()
        c.drawPath(path, paint)
        // Crystal shine
        paint.color = 0xFFE1F5FE.toInt()
        path.reset()
        path.moveTo(x - s * 0.1f, y - s * 0.7f)
        path.lineTo(x + s * 0.05f, y - s * 0.5f)
        path.lineTo(x - s * 0.15f, y - s * 0.35f)
        path.close()
        c.drawPath(path, paint)
        // Frost particles orbiting crystal
        paint.color = 0xFFB3E5FC.toInt()
        for (i in 0..3) {
            val angle = i * Math.PI / 2 + t / 2000.0 * Math.PI * 2
            val r = s * 0.38f
            val px = x + (Math.cos(angle) * r).toFloat()
            val py = y - s * 0.35f + (Math.sin(angle) * r * 0.5f).toFloat()
            c.drawCircle(px, py, s * 0.04f, paint)
        }
        // Snowflake cross inside — slowly rotating
        strokePaint.color = 0xFFFFFFFF.toInt()
        strokePaint.strokeWidth = 1.5f
        val rot = (t % 6000 / 6000.0 * Math.PI * 2).toFloat()
        val cx = x; val cy = y - s * 0.35f
        for (i in 0..2) {
            val a = rot + i * Math.PI.toFloat() / 3
            c.drawLine(cx - s * 0.18f * Math.cos(a.toDouble()).toFloat(), cy - s * 0.18f * Math.sin(a.toDouble()).toFloat(),
                cx + s * 0.18f * Math.cos(a.toDouble()).toFloat(), cy + s * 0.18f * Math.sin(a.toDouble()).toFloat(), strokePaint)
        }
    }

    private fun drawTowerFlame(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Stone brazier base
        paint.color = 0xFF5D4037.toInt()
        path.reset()
        path.moveTo(x - s * 0.4f, y + s * 0.1f)
        path.lineTo(x - s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.4f, y + s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Fire bowl
        paint.color = 0xFF424242.toInt()
        c.drawOval(x - s * 0.35f, y - s * 0.05f, x + s * 0.35f, y + s * 0.15f, paint)
        // Flame — outer orange with flicker
        val flicker = (Math.sin(t / 120.0) * s * 0.05f).toFloat()
        val flicker2 = (Math.cos(t / 90.0) * s * 0.03f).toFloat()
        paint.color = 0xFFFF5722.toInt()
        path.reset()
        path.moveTo(x - s * 0.25f, y)
        path.quadTo(x - s * 0.3f + flicker2, y - s * 0.5f, x + flicker, y - s * 0.85f - Math.abs(flicker))
        path.quadTo(x + s * 0.3f - flicker2, y - s * 0.5f, x + s * 0.25f, y)
        path.close()
        c.drawPath(path, paint)
        // Flame — inner yellow
        paint.color = 0xFFFFEB3B.toInt()
        path.reset()
        path.moveTo(x - s * 0.12f, y)
        path.quadTo(x - s * 0.15f - flicker, y - s * 0.35f, x - flicker2, y - s * 0.6f - Math.abs(flicker2))
        path.quadTo(x + s * 0.15f + flicker, y - s * 0.35f, x + s * 0.12f, y)
        path.close()
        c.drawPath(path, paint)
        // Rising ember particles
        paint.color = 0xFFFF8A65.toInt()
        val e1y = y - s * 0.5f - (t % 800 / 800f) * s * 0.5f
        val e2y = y - s * 0.65f - ((t + 300) % 1000 / 1000f) * s * 0.4f
        paint.alpha = (255 * (1f - (t % 800 / 800f))).toInt()
        c.drawCircle(x - s * 0.2f + flicker, e1y, s * 0.04f, paint)
        paint.alpha = (255 * (1f - ((t + 300) % 1000 / 1000f))).toInt()
        c.drawCircle(x + s * 0.15f - flicker2, e2y, s * 0.035f, paint)
        paint.alpha = 255
    }

    private fun drawTowerNecro(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Dark pedestal
        paint.color = 0xFF212121.toInt()
        c.drawRect(x - s * 0.35f, y + s * 0.15f, x + s * 0.35f, y + s * 0.5f, paint)
        // Tombstone shape
        paint.color = 0xFF424242.toInt()
        path.reset()
        path.moveTo(x - s * 0.25f, y + s * 0.15f)
        path.lineTo(x - s * 0.25f, y - s * 0.4f)
        path.arcTo(x - s * 0.25f, y - s * 0.7f, x + s * 0.25f, y - s * 0.1f, 180f, 180f, false)
        path.lineTo(x + s * 0.25f, y + s * 0.15f)
        path.close()
        c.drawPath(path, paint)
        // Skull symbol on tombstone — pulsing
        val skullGlow = (200 + 55 * Math.sin(t / 600.0)).toInt()
        paint.color = (skullGlow shl 24) or 0x00CE93D8
        c.drawCircle(x, y - s * 0.3f, s * 0.12f, paint)
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * 0.05f, y - s * 0.33f, s * 0.035f, paint)
        c.drawCircle(x + s * 0.05f, y - s * 0.33f, s * 0.035f, paint)
        // Floating soul wisps — orbiting
        paint.color = 0xFF9C27B0.toInt()
        for (i in 0..2) {
            val angle = i * Math.PI * 2 / 3 + t / 1500.0 * Math.PI * 2
            val r = s * (0.35f + 0.05f * Math.sin(t / 400.0 + i).toFloat())
            val wy = y - s * 0.5f + (Math.sin(angle + Math.PI / 2) * s * 0.15f).toFloat()
            val wx = x + (Math.cos(angle) * r).toFloat()
            paint.alpha = (180 + 75 * Math.sin(t / 300.0 + i * 2)).toInt()
            c.drawCircle(wx, wy, s * (0.05f + 0.01f * i), paint)
        }
        paint.alpha = 255
    }

    private fun drawTowerBallista(c: Canvas, x: Float, y: Float, s: Float) {
        val t = System.currentTimeMillis()
        // Wooden frame base
        paint.color = 0xFF5D4037.toInt()
        c.drawRect(x - s * 0.4f, y + s * 0.2f, x + s * 0.4f, y + s * 0.5f, paint)
        // Support legs
        strokePaint.color = 0xFF795548.toInt()
        strokePaint.strokeWidth = 3f
        c.drawLine(x - s * 0.35f, y + s * 0.5f, x - s * 0.25f, y - s * 0.1f, strokePaint)
        c.drawLine(x + s * 0.35f, y + s * 0.5f, x + s * 0.25f, y - s * 0.1f, strokePaint)
        // Main beam / rail
        paint.color = 0xFF8D6E63.toInt()
        c.drawRect(x - s * 0.08f, y - s * 0.75f, x + s * 0.08f, y + s * 0.2f, paint)
        // Crossbow arms
        strokePaint.color = 0xFF3E2723.toInt()
        strokePaint.strokeWidth = 4f
        c.drawLine(x - s * 0.4f, y - s * 0.35f, x + s * 0.4f, y - s * 0.35f, strokePaint)
        // Bowstring — vibrating
        strokePaint.color = 0xFFBCAAA4.toInt()
        strokePaint.strokeWidth = 1.5f
        val vibrate = (Math.sin(t / 60.0) * s * 0.02f * Math.max(0.0, 1.0 - (t % 2000) / 500.0)).toFloat()
        c.drawLine(x - s * 0.4f, y - s * 0.35f, x + vibrate, y - s * 0.2f, strokePaint)
        c.drawLine(x + s * 0.4f, y - s * 0.35f, x + vibrate, y - s * 0.2f, strokePaint)
        // Large bolt
        paint.color = 0xFFFFD54F.toInt()
        c.drawRect(x - s * 0.04f, y - s * 0.8f, x + s * 0.04f, y - s * 0.2f, paint)
        // Bolt tip — glinting
        path.reset()
        path.moveTo(x, y - s * 0.95f)
        path.lineTo(x - s * 0.1f, y - s * 0.8f)
        path.lineTo(x + s * 0.1f, y - s * 0.8f)
        path.close()
        val glintAlpha = (200 + 55 * Math.sin(t / 400.0)).toInt()
        paint.color = (glintAlpha shl 24) or 0x00BDBDBD
        c.drawPath(path, paint)
    }

    private fun drawTowerVortex(c: Canvas, x: Float, y: Float, s: Float) {
        // Arcane base circle
        paint.color = 0xFF311B92.toInt()
        c.drawCircle(x, y + s * 0.2f, s * 0.35f, paint)
        // Swirl rings (3 concentric)
        strokePaint.color = 0xFF7E57C2.toInt()
        strokePaint.strokeWidth = 2.5f
        c.drawCircle(x, y - s * 0.2f, s * 0.15f, strokePaint)
        strokePaint.color = 0xFFB39DDB.toInt()
        strokePaint.strokeWidth = 2f
        c.drawCircle(x, y - s * 0.2f, s * 0.3f, strokePaint)
        strokePaint.color = 0xFFD1C4E9.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawCircle(x, y - s * 0.2f, s * 0.45f, strokePaint)
        // Central orb
        paint.color = 0xFF7E57C2.toInt()
        c.drawCircle(x, y - s * 0.2f, s * 0.1f, paint)
        paint.color = 0xFFEDE7F6.toInt()
        c.drawCircle(x, y - s * 0.23f, s * 0.04f, paint)
        // Spiral arms (drawn as small dots along spirals)
        paint.color = 0xFFB39DDB.toInt()
        for (i in 0..5) {
            val angle = i * Math.PI / 3 + System.currentTimeMillis() % 3000 / 3000.0 * Math.PI * 2
            val r = s * 0.2f + i * s * 0.04f
            c.drawCircle(x + (Math.cos(angle) * r).toFloat(), y - s * 0.2f + (Math.sin(angle) * r).toFloat(), s * 0.035f, paint)
        }
    }

    // ========== BASE ==========

    // ========== NEW ENEMIES ==========

    private fun drawBerserker(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean, rage: Float) {
        // Bulky red muscular body — glows brighter as rage increases
        val rageAlpha = ((rage - 1f) / 1.5f).coerceIn(0f, 1f)
        val bodyColor = color(0xFFD32F2F.toInt(), flash, frozen)
        paint.color = bodyColor
        // Torso — wide trapezoid
        path.reset()
        path.moveTo(x - s * 0.5f, y - s * 0.1f)
        path.lineTo(x + s * 0.5f, y - s * 0.1f)
        path.lineTo(x + s * 0.4f, y + s * 0.6f)
        path.lineTo(x - s * 0.4f, y + s * 0.6f)
        path.close()
        c.drawPath(path, paint)
        // Arms — thick rectangles
        c.drawRect(x - s * 0.7f, y - s * 0.05f, x - s * 0.5f, y + s * 0.45f, paint)
        c.drawRect(x + s * 0.5f, y - s * 0.05f, x + s * 0.7f, y + s * 0.45f, paint)
        // Head
        paint.color = color(0xFFE57373.toInt(), flash, frozen)
        c.drawCircle(x, y - s * 0.35f, s * 0.25f, paint)
        // Angry eyes — red glow scales with rage
        paint.color = if (frozen) 0xFF81D4FA.toInt() else android.graphics.Color.argb(
            (160 + 95 * rageAlpha).toInt(), 255, 50, 50)
        c.drawCircle(x - s * 0.1f, y - s * 0.38f, s * 0.06f, paint)
        c.drawCircle(x + s * 0.1f, y - s * 0.38f, s * 0.06f, paint)
        // Rage aura glow
        if (rageAlpha > 0.1f) {
            paint.color = android.graphics.Color.argb((40 * rageAlpha).toInt(), 255, 50, 50)
            c.drawCircle(x, y, s * (0.7f + rageAlpha * 0.3f), paint)
        }
    }

    private fun drawCommander(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean) {
        // Gold-crowned officer — upright body with crown and cape
        val bodyColor = color(0xFFFF6F00.toInt(), flash, frozen)
        paint.color = bodyColor
        // Body — rectangular
        c.drawRect(x - s * 0.3f, y - s * 0.15f, x + s * 0.3f, y + s * 0.6f, paint)
        // Cape behind
        paint.color = color(0xFFBF360C.toInt(), flash, frozen)
        path.reset()
        path.moveTo(x - s * 0.35f, y - s * 0.1f)
        path.lineTo(x - s * 0.45f, y + s * 0.65f)
        path.lineTo(x + s * 0.45f, y + s * 0.65f)
        path.lineTo(x + s * 0.35f, y - s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Head
        paint.color = color(0xFFFFB74D.toInt(), flash, frozen)
        c.drawCircle(x, y - s * 0.35f, s * 0.22f, paint)
        // Crown — 3 gold triangles
        paint.color = color(0xFFFFD700.toInt(), flash, frozen)
        for (i in -1..1) {
            path.reset()
            val cx = x + i * s * 0.15f
            path.moveTo(cx - s * 0.06f, y - s * 0.5f)
            path.lineTo(cx, y - s * 0.7f)
            path.lineTo(cx + s * 0.06f, y - s * 0.5f)
            path.close()
            c.drawPath(path, paint)
        }
        c.drawRect(x - s * 0.22f, y - s * 0.52f, x + s * 0.22f, y - s * 0.47f, paint)
        // Eyes
        paint.color = android.graphics.Color.WHITE
        c.drawCircle(x - s * 0.08f, y - s * 0.37f, s * 0.05f, paint)
        c.drawCircle(x + s * 0.08f, y - s * 0.37f, s * 0.05f, paint)
        paint.color = color(0xFFE65100.toInt(), flash, frozen)
        c.drawCircle(x - s * 0.08f, y - s * 0.37f, s * 0.025f, paint)
        c.drawCircle(x + s * 0.08f, y - s * 0.37f, s * 0.025f, paint)
        // Aura ring
        strokePaint.color = color(0xFFFFD700.toInt(), flash, frozen)
        strokePaint.strokeWidth = 1.5f
        c.drawCircle(x, y, s * 0.65f, strokePaint)
    }

    private fun drawShapeshifter(c: Canvas, x: Float, y: Float, s: Float, flash: Boolean, frozen: Boolean, phase: Int) {
        // Morphing amorphous blob — color/shape changes with phase
        val phaseColors = intArrayOf(0xFF7C4DFF.toInt(), 0xFFFF4081.toInt(), 0xFF00E676.toInt(), 0xFFFFD740.toInt())
        val baseColor = color(phaseColors[phase % 4], flash, frozen)
        paint.color = baseColor
        // Main blob — drawn as overlapping circles to look amorphous
        val t = (System.currentTimeMillis() % 2000) / 2000f * Math.PI.toFloat() * 2
        c.drawCircle(x + kotlin.math.sin(t) * s * 0.08f, y, s * 0.38f, paint)
        c.drawCircle(x - s * 0.15f, y + kotlin.math.cos(t) * s * 0.06f, s * 0.25f, paint)
        c.drawCircle(x + s * 0.15f, y - kotlin.math.sin(t * 1.3f) * s * 0.05f, s * 0.25f, paint)
        // Eyes that shift position with phase
        paint.color = android.graphics.Color.WHITE
        val eyeOffset = (phase % 2) * 0.06f
        c.drawCircle(x - s * (0.1f + eyeOffset), y - s * 0.1f, s * 0.07f, paint)
        c.drawCircle(x + s * (0.1f + eyeOffset), y - s * 0.1f, s * 0.07f, paint)
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x - s * (0.1f + eyeOffset), y - s * 0.1f, s * 0.035f, paint)
        c.drawCircle(x + s * (0.1f + eyeOffset), y - s * 0.1f, s * 0.035f, paint)
        // Phase ring
        strokePaint.color = baseColor
        strokePaint.strokeWidth = 1.5f
        c.drawCircle(x, y, s * 0.5f, strokePaint)
    }

    // ========== HEALER TOWER ==========

    private fun drawTowerHealer(c: Canvas, x: Float, y: Float, s: Float) {
        // Green base pedestal
        paint.color = 0xFF2E7D32.toInt()
        c.drawCircle(x, y + s * 0.2f, s * 0.38f, paint)
        // White cross body
        paint.color = 0xFFFFFFFF.toInt()
        c.drawRect(x - s * 0.08f, y - s * 0.45f, x + s * 0.08f, y + s * 0.1f, paint)
        c.drawRect(x - s * 0.25f, y - s * 0.25f, x + s * 0.25f, y - s * 0.09f, paint)
        // Green heart at center
        paint.color = 0xFF66BB6A.toInt()
        c.drawCircle(x, y - s * 0.17f, s * 0.1f, paint)
        // Pulsing heal aura
        val pulse = (System.currentTimeMillis() % 1500) / 1500f
        val auraAlpha = ((1f - pulse) * 40).toInt().coerceIn(0, 255)
        paint.color = android.graphics.Color.argb(auraAlpha, 102, 187, 106)
        c.drawCircle(x, y, s * (0.4f + pulse * 0.3f), paint)
        // Sparkle dots rotating around
        paint.color = 0xFF81C784.toInt()
        for (i in 0..3) {
            val angle = i * Math.PI / 2 + System.currentTimeMillis() % 2000 / 2000.0 * Math.PI * 2
            val r = s * 0.35f
            c.drawCircle(x + (Math.cos(angle) * r).toFloat(), y - s * 0.1f + (Math.sin(angle) * r).toFloat(), s * 0.04f, paint)
        }
    }

    fun drawBase(canvas: Canvas, x: Float, y: Float, hpRatio: Float) {
        // Glow ring under base
        val glowColor = if (hpRatio > 0.6f) 0x224CAF50 else if (hpRatio > 0.3f) 0x22FFB300 else 0x22F44336
        paint.color = glowColor
        canvas.drawCircle(x, y, 68f, paint)
        paint.color = glowColor and 0x11FFFFFF.toInt()
        canvas.drawCircle(x, y, 80f, paint)
        // Foundation ring
        paint.color = 0xFF424242.toInt()
        canvas.drawCircle(x, y, 55f, paint)
        // Castle wall
        val wallColor = if (hpRatio > 0.6f) 0xFF4CAF50.toInt() else if (hpRatio > 0.3f) 0xFFFFB300.toInt() else 0xFFF44336.toInt()
        paint.color = wallColor
        canvas.drawCircle(x, y, 48f, paint)
        // Battlements (4 blocks on top)
        for (i in 0..3) {
            val angle = i * Math.PI / 2
            val bx = x + (42f * kotlin.math.cos(angle)).toFloat()
            val by = y + (42f * kotlin.math.sin(angle)).toFloat()
            canvas.drawRect(bx - 8f, by - 8f, bx + 8f, by + 8f, paint)
        }
        // Inner keep
        paint.color = 0xFF2E7D32.toInt()
        canvas.drawCircle(x, y, 25f, paint)
        // Window slits
        paint.color = 0xFFFFD54F.toInt()
        canvas.drawRect(x - 3f, y - 12f, x + 3f, y - 4f, paint)
        canvas.drawRect(x - 15f, y - 3f, x - 9f, y + 3f, paint)
        canvas.drawRect(x + 9f, y - 3f, x + 15f, y + 3f, paint)
        // Door
        paint.color = 0xFF795548.toInt()
        canvas.drawRoundRect(x - 9f, y + 8f, x + 9f, y + 28f, 4f, 4f, paint)
        paint.color = 0xFF5D4037.toInt()
        canvas.drawRect(x - 1f, y + 12f, x + 1f, y + 28f, paint)
        // Flag on top
        strokePaint.color = 0xFF795548.toInt()
        strokePaint.strokeWidth = 2f
        canvas.drawLine(x, y - 25f, x, y - 50f, strokePaint)
        paint.color = 0xFFFFD700.toInt()
        path.reset()
        path.moveTo(x, y - 50f)
        path.lineTo(x + 15f, y - 43f)
        path.lineTo(x, y - 36f)
        path.close()
        canvas.drawPath(path, paint)
    }
}
