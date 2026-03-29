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
            else -> drawDefaultCircle(canvas, x, y, s, enemy.displayColor, flash, frozen)
        }
    }

    private fun color(baseColor: Int, flash: Boolean, frozen: Boolean): Int = when {
        flash -> Color.WHITE
        frozen -> 0xFF81D4FA.toInt()
        else -> baseColor
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
        }
    }

    private fun drawTowerArrow(c: Canvas, x: Float, y: Float, s: Float) {
        // Wooden base
        paint.color = 0xFF795548.toInt()
        c.drawRect(x - s * 0.35f, y - s * 0.1f, x + s * 0.35f, y + s * 0.5f, paint)
        // Platform top
        paint.color = 0xFF5D4037.toInt()
        c.drawRect(x - s * 0.45f, y - s * 0.2f, x + s * 0.45f, y - s * 0.05f, paint)
        // Bow
        strokePaint.color = 0xFF8D6E63.toInt()
        strokePaint.strokeWidth = 3f
        c.drawArc(x - s * 0.3f, y - s * 0.7f, x + s * 0.35f, y + s * 0.0f, -120f, 240f, false, strokePaint)
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
        // Inner glow
        paint.color = 0xFFE1BEE7.toInt()
        c.drawCircle(x, y - s * 0.4f, s * 0.1f, paint)
        // Sparkle
        strokePaint.color = 0xFFFFFFFF.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawLine(x - s * 0.15f, y - s * 0.55f, x + s * 0.15f, y - s * 0.25f, strokePaint)
        c.drawLine(x + s * 0.15f, y - s * 0.55f, x - s * 0.15f, y - s * 0.25f, strokePaint)
    }

    private fun drawTowerCannon(c: Canvas, x: Float, y: Float, s: Float) {
        // Base platform
        paint.color = 0xFF424242.toInt()
        c.drawRect(x - s * 0.4f, y + s * 0.1f, x + s * 0.4f, y + s * 0.5f, paint)
        // Barrel
        paint.color = 0xFF616161.toInt()
        c.drawRect(x - s * 0.15f, y - s * 0.7f, x + s * 0.15f, y + s * 0.15f, paint)
        // Barrel tip
        paint.color = 0xFF212121.toInt()
        c.drawCircle(x, y - s * 0.7f, s * 0.18f, paint)
        // Wheel
        strokePaint.color = 0xFF795548.toInt()
        strokePaint.strokeWidth = 3f
        c.drawCircle(x - s * 0.3f, y + s * 0.35f, s * 0.15f, strokePaint)
        c.drawCircle(x + s * 0.3f, y + s * 0.35f, s * 0.15f, strokePaint)
    }

    private fun drawTowerPoison(c: Canvas, x: Float, y: Float, s: Float) {
        // Cauldron
        paint.color = 0xFF424242.toInt()
        path.reset()
        path.moveTo(x - s * 0.4f, y - s * 0.1f)
        path.lineTo(x - s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.3f, y + s * 0.5f)
        path.lineTo(x + s * 0.4f, y - s * 0.1f)
        path.close()
        c.drawPath(path, paint)
        // Poison liquid
        paint.color = 0xFF66BB6A.toInt()
        c.drawOval(x - s * 0.35f, y - s * 0.2f, x + s * 0.35f, y + s * 0.05f, paint)
        // Bubbles
        paint.color = 0xFF81C784.toInt()
        c.drawCircle(x - s * 0.12f, y - s * 0.3f, s * 0.07f, paint)
        c.drawCircle(x + s * 0.08f, y - s * 0.4f, s * 0.05f, paint)
        c.drawCircle(x + s * 0.15f, y - s * 0.25f, s * 0.06f, paint)
        // Skull symbol
        paint.color = 0xFFFFFFFF.toInt()
        c.drawCircle(x, y + s * 0.2f, s * 0.1f, paint)
        paint.color = 0xFF424242.toInt()
        c.drawCircle(x - s * 0.04f, y + s * 0.18f, s * 0.03f, paint)
        c.drawCircle(x + s * 0.04f, y + s * 0.18f, s * 0.03f, paint)
    }

    private fun drawTowerTesla(c: Canvas, x: Float, y: Float, s: Float) {
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
        // Tesla ball on top
        paint.color = 0xFF29B6F6.toInt()
        c.drawCircle(x, y - s * 0.7f, s * 0.15f, paint)
        // Electric arcs
        strokePaint.color = 0xFFFFEB3B.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawLine(x - s * 0.3f, y - s * 0.8f, x - s * 0.15f, y - s * 0.65f, strokePaint)
        c.drawLine(x + s * 0.3f, y - s * 0.8f, x + s * 0.15f, y - s * 0.65f, strokePaint)
        c.drawLine(x, y - s * 0.9f, x, y - s * 0.75f, strokePaint)
    }

    private fun drawTowerIce(c: Canvas, x: Float, y: Float, s: Float) {
        // Frozen stone base
        paint.color = 0xFF546E7A.toInt()
        c.drawRect(x - s * 0.35f, y + s * 0.15f, x + s * 0.35f, y + s * 0.5f, paint)
        // Ice crystal — hexagonal shape
        paint.color = 0xFF81D4FA.toInt()
        path.reset()
        path.moveTo(x, y - s * 0.85f)              // top point
        path.lineTo(x + s * 0.25f, y - s * 0.5f)
        path.lineTo(x + s * 0.25f, y - s * 0.1f)
        path.lineTo(x, y + s * 0.15f)               // bottom point
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
        // Frost particles around crystal
        paint.color = 0xFFB3E5FC.toInt()
        c.drawCircle(x - s * 0.35f, y - s * 0.45f, s * 0.05f, paint)
        c.drawCircle(x + s * 0.35f, y - s * 0.55f, s * 0.04f, paint)
        c.drawCircle(x - s * 0.3f, y - s * 0.7f, s * 0.035f, paint)
        c.drawCircle(x + s * 0.28f, y - s * 0.2f, s * 0.04f, paint)
        // Snowflake cross inside
        strokePaint.color = 0xFFFFFFFF.toInt()
        strokePaint.strokeWidth = 1.5f
        c.drawLine(x, y - s * 0.65f, x, y - s * 0.05f, strokePaint)
        c.drawLine(x - s * 0.18f, y - s * 0.5f, x + s * 0.18f, y - s * 0.2f, strokePaint)
        c.drawLine(x + s * 0.18f, y - s * 0.5f, x - s * 0.18f, y - s * 0.2f, strokePaint)
    }

    // ========== BASE ==========

    fun drawBase(canvas: Canvas, x: Float, y: Float, hpRatio: Float) {
        // Foundation ring
        paint.color = 0xFF424242.toInt()
        canvas.drawCircle(x, y, 55f, paint)
        // Castle wall
        val wallColor = if (hpRatio > 0.3f) 0xFF4CAF50.toInt() else 0xFFF44336.toInt()
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
        // Door
        paint.color = 0xFF795548.toInt()
        canvas.drawRect(x - 8f, y + 10f, x + 8f, y + 28f, paint)
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
