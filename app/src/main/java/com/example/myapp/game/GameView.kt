package com.example.myapp.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    @Volatile private var running = false
    private val engine = GameEngine(context)

    var onGoldChanged: ((Int) -> Unit)? = null
    var onWaveChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int, Int) -> Unit)? = null
    var onDiamondsChanged: ((Int) -> Unit)? = null

    // Paints
    private val bgPaint = Paint().apply { color = 0xFF4A90D9.toInt() } // bright sky blue
    private val gridPaint = Paint().apply { color = 0xFF5A9FE0.toInt(); strokeWidth = 1f }
    // Static terrain colors (no day/night)
    private val skyColor = 0xFF4A90D9.toInt()
    private val grassColor = 0xFF3A7D3A.toInt()
    private val grassLightColor = 0xFF4CAF50.toInt()
    private val pathColor = 0xFF8D6E63.toInt()
    private val pathEdgeColor = 0xFF6D4C41.toInt()
    private val trunkColor = 0xFF795548.toInt()
    private val rockColor = 0xFF757575.toInt()
    private val rockHighlightColor = 0xFF9E9E9E.toInt()
    // Terrain paints (pre-allocated)
    private val terrainPaint = Paint().apply { isAntiAlias = true }
    private val pathPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val shadowPaint = Paint().apply { isAntiAlias = true; color = 0x33000000 }
    private val textOutlinePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 4f; color = 0xCC000000.toInt(); strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    // Cached terrain decorations (regenerated on surface size change)
    private data class Decoration(val x: Float, val y: Float, val type: Int, val scale: Float, val seed: Int)
    private var decorations: List<Decoration> = emptyList()
    private var pathCache: List<Path> = emptyList()
    private var terrainDirty = true
    // Diamond display paint
    private val diamondPaint = Paint().apply {
        color = 0xFF00E5FF.toInt(); textSize = 22f; isAntiAlias = true; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD
    }
    // Wave preview paints
    private val previewBgPaint = Paint().apply { color = 0xCC1B2838.toInt() }
    private val previewTextPaint = Paint().apply {
        color = Color.WHITE; textSize = 18f; isAntiAlias = true; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD
    }
    private val previewLabelPaint = Paint().apply {
        color = 0xFFBDBDBD.toInt(); textSize = 16f; isAntiAlias = true; textAlign = Paint.Align.LEFT
    }
    private val basePaint = Paint().apply { color = 0xFF4CAF50.toInt() }
    private val baseDamagedPaint = Paint().apply { color = 0xFFF44336.toInt() }
    private val hpBarBgPaint = Paint().apply { color = 0xFF333333.toInt() }
    private val hpBarPaint = Paint().apply { color = 0xFF4CAF50.toInt() }
    private val hpBarDamagedPaint = Paint().apply { color = 0xFFF44336.toInt() }
    private val playerPaint = Paint().apply { color = 0xFF42A5F5.toInt() }
    private val playerRangePaint = Paint().apply {
        color = 0x2242A5F5; style = Paint.Style.STROKE; strokeWidth = 2f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE; textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER
    }
    private val emojiPaint = Paint().apply {
        textSize = 48f; isAntiAlias = true; textAlign = Paint.Align.CENTER
    }
    private val smallEmojiPaint = Paint().apply {
        textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER
    }
    private val towerRangePaint = Paint().apply {
        color = 0x15FFFFFF; style = Paint.Style.STROKE; strokeWidth = 1f
    }
    private val projPaint = Paint().apply { isAntiAlias = true }
    private val goldTextPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 28f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val waveTextPaint = Paint().apply {
        color = Color.WHITE; textSize = 40f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val particlePaint = Paint().apply { isAntiAlias = true }
    private val floatPaint = Paint().apply {
        isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val comboPaint = Paint().apply {
        color = 0xFFFF9800.toInt(); textSize = 32f; isAntiAlias = true; textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT_BOLD
    }
    private val achievePaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 28f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val freezeOverlayPaint = Paint().apply { color = 0x1529B6F6 }
    // Pre-allocated reusable paints to avoid GC pressure in render loop
    private val bossHpPaint = Paint().apply { isAntiAlias = true }
    private val bossNamePaint = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }
    private val lvlPaint = Paint().apply { color = 0xFFFFD700.toInt(); textSize = 18f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
    private val playerHpBarPaint = Paint().apply { color = 0xFF42A5F5.toInt() }
    private val overlayPaint = Paint().apply { color = 0xAA000000.toInt() }
    private val gameOverPaint = Paint().apply {
        color = 0xFFF44336.toInt(); textSize = 64f; isAntiAlias = true
        textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val scoreDisplayPaint = Paint().apply { color = Color.WHITE; textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }

    // Tower placement mode
    var placementMode: TowerType? = null
    private var selectedTower: Tower? = null

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun getEngine(): GameEngine = engine

    override fun surfaceCreated(holder: SurfaceHolder) {
        engine.init(width.toFloat(), height.toFloat())
        generateTerrain()
        running = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.init(width.toFloat(), height.toFloat())
        generateTerrain()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            gameThread?.join(2000)
        } catch (_: InterruptedException) { }
        gameThread = null
        // Clear callbacks to prevent activity memory leak
        onGoldChanged = null
        onWaveChanged = null
        onGameOver = null
        onDiamondsChanged = null
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
            lastTime = now

            engine.update(dt)

            val canvas = holder.lockCanvas()
            if (canvas != null) {
                try {
                    synchronized(engine.lock) { drawGame(canvas) }
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Notify UI thread
            val goldSnap: Int
            val waveSnap: Int
            val diamondSnap: Int
            val isGameOver: Boolean
            val scoreSnap: Int
            synchronized(engine.lock) {
                goldSnap = engine.gold
                waveSnap = engine.wave
                diamondSnap = engine.skillTree.diamonds
                isGameOver = engine.gameOver
                scoreSnap = engine.score
            }
            post {
                onGoldChanged?.invoke(goldSnap)
                onWaveChanged?.invoke(waveSnap)
                onDiamondsChanged?.invoke(diamondSnap)
                if (isGameOver) {
                    onGameOver?.invoke(scoreSnap, waveSnap)
                }
            }

            // Cap at ~60 fps
            val frameTime = (System.nanoTime() - now) / 1_000_000
            if (frameTime < 16) {
                Thread.sleep(16 - frameTime)
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        // Screen shake
        canvas.save()
        if (engine.shakeTimer > 0) {
            val sx = ((Math.random() - 0.5) * engine.shakeIntensity * 2).toFloat()
            val sy = ((Math.random() - 0.5) * engine.shakeIntensity * 2).toFloat()
            canvas.translate(sx, sy)
        }

        // Sky background
        bgPaint.color = skyColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // A few fluffy clouds
        paint.color = 0x55FFFFFF
        canvas.drawOval(width * 0.15f, height * 0.03f, width * 0.35f, height * 0.08f, paint)
        canvas.drawOval(width * 0.22f, height * 0.02f, width * 0.32f, height * 0.065f, paint)
        canvas.drawOval(width * 0.6f, height * 0.05f, width * 0.82f, height * 0.10f, paint)
        canvas.drawOval(width * 0.65f, height * 0.04f, width * 0.78f, height * 0.085f, paint)

        // Green terrain ground
        val groundTop = height * 0.12f
        terrainPaint.shader = LinearGradient(
            0f, groundTop, 0f, height.toFloat(),
            grassLightColor, grassColor, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, groundTop, width.toFloat(), height.toFloat(), terrainPaint)
        terrainPaint.shader = null

        // Subtle grid overlay on grass
        gridPaint.color = 0xFF5A9FE0.toInt()
        gridPaint.alpha = 25
        val gridSize = 60f
        var gx = 0f
        while (gx < width) {
            canvas.drawLine(gx, groundTop, gx, height.toFloat(), gridPaint)
            gx += gridSize
        }
        var gy = groundTop
        while (gy < height) {
            canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint)
            gy += gridSize
        }
        gridPaint.alpha = 255

        // Dirt paths from spawn points to base
        for (p in pathCache) {
            pathPaint.style = Paint.Style.STROKE
            pathPaint.color = pathEdgeColor
            pathPaint.strokeWidth = 42f
            canvas.drawPath(p, pathPaint)
            pathPaint.color = pathColor
            pathPaint.strokeWidth = 32f
            canvas.drawPath(p, pathPaint)
        }

        // Grass tufts
        val turfSeed = 99
        val turfRng = java.util.Random(turfSeed.toLong())
        terrainPaint.color = grassLightColor
        terrainPaint.alpha = 100
        repeat(60) {
            val tx = turfRng.nextFloat() * width
            val ty = groundTop + turfRng.nextFloat() * (height - groundTop)
            val ts = 3f + turfRng.nextFloat() * 5f
            canvas.drawLine(tx, ty, tx - ts * 0.5f, ty - ts, terrainPaint)
            canvas.drawLine(tx, ty, tx + ts * 0.3f, ty - ts * 0.8f, terrainPaint)
        }
        terrainPaint.alpha = 255

        // Decorations (trees, rocks, bushes)
        drawDecorations(canvas)

        // Horizon fog
        terrainPaint.shader = LinearGradient(
            0f, groundTop - 20f, 0f, groundTop + 40f,
            skyColor, (skyColor and 0x00FFFFFF), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, groundTop - 20f, width.toFloat(), groundTop + 40f, terrainPaint)
        terrainPaint.shader = null

        // Freeze overlay
        if (engine.freezeTimer > 0) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), freezeOverlayPaint)
        }

        // === SHADOWS (drawn before entities for depth) ===
        // Base shadow
        shadowPaint.alpha = 40
        canvas.drawOval(
            engine.baseX - 55f, engine.baseY + 35f,
            engine.baseX + 55f, engine.baseY + 55f, shadowPaint
        )
        // Tower shadows
        for (tower in engine.towers) {
            shadowPaint.alpha = 35
            canvas.drawOval(
                tower.x - tower.size * 0.6f, tower.y + tower.size * 0.3f,
                tower.x + tower.size * 0.6f, tower.y + tower.size * 0.5f, shadowPaint
            )
        }
        // Enemy shadows
        for (enemy in engine.enemies) {
            shadowPaint.alpha = 30
            val es = enemy.size * 0.8f
            canvas.drawOval(
                enemy.x - es, enemy.y + enemy.size * 0.5f,
                enemy.x + es, enemy.y + enemy.size * 0.7f, shadowPaint
            )
        }
        // Player shadow
        shadowPaint.alpha = 35
        canvas.drawOval(
            engine.player.x - engine.player.size * 0.5f,
            engine.player.y + engine.player.size * 0.4f,
            engine.player.x + engine.player.size * 0.5f,
            engine.player.y + engine.player.size * 0.6f,
            shadowPaint
        )

        // Base
        val baseHpRatio = engine.baseHp / engine.maxBaseHp
        EntityRenderer.drawBase(canvas, engine.baseX, engine.baseY, baseHpRatio)

        // Base HP bar
        val barW = 80f
        val barH = 8f
        val barX = engine.baseX - barW / 2
        val barY = engine.baseY + 55f
        canvas.drawRect(barX, barY, barX + barW, barY + barH, hpBarBgPaint)
        val hpPaint = if (baseHpRatio > 0.3f) hpBarPaint else hpBarDamagedPaint
        canvas.drawRect(barX, barY, barX + barW * baseHpRatio, barY + barH, hpPaint)

        // Towers & ranges (with perspective)
        for (tower in engine.towers) {
            if (tower == selectedTower) {
                canvas.drawCircle(tower.x, tower.y, tower.range, towerRangePaint)
            }
            val tScale = depthFactor(tower.y)
            canvas.save()
            canvas.scale(tScale, tScale, tower.x, tower.y)
            EntityRenderer.drawTower(canvas, tower, tower == selectedTower)
            canvas.restore()
            if (tower.level > 1) {
                canvas.drawText("Lv${tower.level}", tower.x, tower.y - 25f * tScale, lvlPaint)
            }
        }

        // Enemies (with perspective scale — further up = slightly smaller)
        for (enemy in engine.enemies) {
            val depthScale = depthFactor(enemy.y)
            canvas.save()
            canvas.scale(depthScale, depthScale, enemy.x, enemy.y)
            EntityRenderer.drawEnemy(canvas, enemy, engine.freezeTimer > 0)
            canvas.restore()

            // HP bar
            val ehpRatio = enemy.hp / enemy.maxHp
            val eBarW = enemy.size * 2
            val eBarX = enemy.x - eBarW / 2
            val eBarY = enemy.y - enemy.size - 10f
            canvas.drawRect(eBarX, eBarY, eBarX + eBarW, eBarY + 5f, hpBarBgPaint)
            canvas.drawRect(eBarX, eBarY, eBarX + eBarW * ehpRatio, eBarY + 5f, hpBarPaint)

            // Boss: large HP bar at top of screen with name
            if (enemy.type == EnemyType.BOSS) {
                val bossBarW = width * 0.7f
                val bossBarX = (width - bossBarW) / 2
                val bossBarY = 100f
                canvas.drawRect(bossBarX, bossBarY, bossBarX + bossBarW, bossBarY + 14f, hpBarBgPaint)
                val bossColor = enemy.bossType?.color ?: 0xFFE91E63.toInt()
                bossHpPaint.color = bossColor
                canvas.drawRect(bossBarX, bossBarY, bossBarX + bossBarW * ehpRatio, bossBarY + 14f, bossHpPaint)
                val bossName = enemy.bossType?.let { "${it.emoji} ${it.displayName}" } ?: "\u2620\uFE0F BOSS"
                bossNamePaint.textSize = 24f; bossNamePaint.color = bossColor
                canvas.drawText("$bossName  ${enemy.hp.toInt()}/${enemy.maxHp.toInt()}", width / 2f, bossBarY - 8f, bossNamePaint)
            }
        }

        // Projectiles
        for (proj in engine.projectiles) {
            projPaint.color = proj.color
            canvas.drawCircle(proj.x, proj.y, proj.size, projPaint)
        }

        // Particles
        for (p in engine.particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (255 * (p.life / p.maxLife).coerceIn(0f, 1f)).toInt()
            canvas.drawCircle(p.x, p.y, p.size * (p.life / p.maxLife).coerceIn(0.3f, 1f), particlePaint)
        }

        // Player (with perspective)
        val phRatioForDraw = engine.player.hp / engine.player.maxHp
        val pScale = depthFactor(engine.player.y)
        canvas.save()
        canvas.scale(pScale, pScale, engine.player.x, engine.player.y)
        EntityRenderer.drawPlayer(canvas, engine.player.x, engine.player.y, engine.player.size, phRatioForDraw)
        canvas.restore()
        canvas.drawCircle(engine.player.x, engine.player.y, engine.player.attackRange, playerRangePaint)

        // Player HP bar
        val phRatio = engine.player.hp / engine.player.maxHp
        val pBarW = 60f
        val pBarX = engine.player.x - pBarW / 2
        val pBarY = engine.player.y - engine.player.size - 15f
        canvas.drawRect(pBarX, pBarY, pBarX + pBarW, pBarY + 6f, hpBarBgPaint)
        canvas.drawRect(pBarX, pBarY, pBarX + pBarW * phRatio, pBarY + 6f, playerHpBarPaint)

        // Floating texts
        for (ft in engine.floatingTexts) {
            floatPaint.color = ft.color
            floatPaint.textSize = ft.size
            floatPaint.alpha = (255 * (ft.life / ft.maxLife).coerceIn(0f, 1f)).toInt()
            canvas.drawText(ft.text, ft.x, ft.y, floatPaint)
        }

        // Wave info at top (with outline for readability)
        val endlessTag = if (engine.isEndlessMode) "\u267E\uFE0F " else ""
        if (!engine.waveInProgress && engine.wave > 0 && !engine.gameOver) {
            val countdown = engine.waveTimer.toInt() + 1
            val wText = "${endlessTag}Next wave in ${countdown}s"
            drawOutlinedText(canvas, wText, width / 2f, 80f, waveTextPaint)
        } else if (engine.waveInProgress) {
            val wText = "${endlessTag}Wave ${engine.wave}  \u2694\uFE0F  ${engine.enemies.size} enemies"
            drawOutlinedText(canvas, wText, width / 2f, 80f, waveTextPaint)
        }

        // Wave banner overlay
        if (engine.showWaveBanner) {
            val bannerPaint = Paint().apply { color = 0xCC000000.toInt() }
            canvas.drawRect(0f, height * 0.35f, width.toFloat(), height * 0.55f, bannerPaint)
            val wp = Paint(waveTextPaint).apply { textSize = 56f; color = 0xFFFFD700.toInt() }
            val isBoss = engine.wave % 5 == 0
            val bossLabel = engine.currentBoss?.let { "${it.emoji} ${it.displayName}" }
            val waveLabel = if (isBoss && bossLabel != null) "$bossLabel" else "\u2694\uFE0F WAVE ${engine.wave} \u2694\uFE0F"
            drawOutlinedText(canvas, waveLabel, width / 2f, height * 0.47f, wp)
            if (isBoss) {
                val subPaint = Paint(waveTextPaint).apply { textSize = 28f; color = 0xFFBDBDBD.toInt() }
                drawOutlinedText(canvas, "Wave ${engine.wave}", width / 2f, height * 0.52f, subPaint)
            }
        }

        // Combo display (top right)
        if (engine.comboCount >= 3) {
            comboPaint.textSize = 28f + engine.comboCount.coerceAtMost(20) * 1.5f
            drawOutlinedText(canvas, "${engine.comboCount}x COMBO", width - 20f, 170f, comboPaint)
            val multPaint = Paint(comboPaint).apply { textSize = 22f; color = 0xFFFFEB3B.toInt() }
            drawOutlinedText(canvas, "x${String.format("%.1f", engine.comboMultiplier)} gold", width - 20f, 198f, multPaint)
        }

        // High score (top left-ish)
        if (engine.highScore > 0) {
            val hsPaint = Paint(goldTextPaint).apply { textSize = 22f; textAlign = Paint.Align.LEFT }
            drawOutlinedText(canvas, "\u2B50 Best: ${engine.highScore}", 10f, 130f, hsPaint)
        }

        // Diamond counter (below high score)
        drawOutlinedText(canvas, "\uD83D\uDC8E ${engine.skillTree.diamonds}", 10f, 155f, diamondPaint)

        // Wave preview panel (between waves, bottom-left area above controls)
        val preview = engine.nextWavePreview
        if (preview != null && !engine.waveInProgress && !engine.gameOver) {
            val pvX = 10f
            val pvY = height * 0.68f
            val pvW = width * 0.55f
            val pvH = if (preview.isBoss) 70f else (40f + preview.enemies.size * 22f).coerceAtMost(120f)
            canvas.drawRoundRect(pvX, pvY, pvX + pvW, pvY + pvH, 12f, 12f, previewBgPaint)
            previewLabelPaint.color = 0xFFBDBDBD.toInt()
            canvas.drawText("\uD83D\uDD2E Next Wave:", pvX + 10f, pvY + 20f, previewLabelPaint)
            if (preview.isBoss && preview.bossType != null) {
                previewTextPaint.color = preview.bossType.color
                canvas.drawText("${preview.bossType.emoji} ${preview.bossType.displayName} + minions!", pvX + 10f, pvY + 48f, previewTextPaint)
            } else {
                var ty = pvY + 40f
                for ((type, count) in preview.enemies) {
                    if (ty > pvY + pvH - 5f) break
                    previewTextPaint.color = type.color
                    canvas.drawText("${type.emoji} ${type.name} x$count", pvX + 10f, ty, previewTextPaint)
                    ty += 22f
                }
            }
        }

        // Achievement banner
        val ach = engine.newAchievement
        if (ach != null && engine.achievementBannerTimer > 0) {
            val abY = height * 0.25f
            val abPaint = Paint().apply { color = 0xDD1B2838.toInt() }
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, abPaint)
            val borderPaint = Paint().apply {
                color = 0xFFFFD700.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
            }
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, borderPaint)
            drawOutlinedText(canvas, "${ach.emoji} Achievement Unlocked!", width / 2f, abY + 28f, achievePaint)
            val descPaint = Paint(achievePaint).apply { textSize = 22f; color = Color.WHITE }
            drawOutlinedText(canvas, "${ach.title} — ${ach.description}", width / 2f, abY + 55f, descPaint)
        }

        // Placement mode indicator
        if (placementMode != null) {
            val placeTextPaint = Paint(goldTextPaint).apply { textSize = 32f }
            drawOutlinedText(canvas, "Tap to place ${placementMode!!.emoji} tower", width / 2f, 160f, placeTextPaint)
        }

        canvas.restore() // End screen shake

        // Game over overlay (drawn outside shake)
        if (engine.gameOver) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            if (engine.isEndlessMode) {
                drawOutlinedText(canvas, "ENDLESS OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            } else {
                drawOutlinedText(canvas, "GAME OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            }

            drawOutlinedText(canvas, "Wave: ${engine.wave}  |  Score: ${engine.score}", width / 2f, height / 2f - 10f, scoreDisplayPaint)
            drawOutlinedText(canvas, "Kills: ${engine.totalKills}  |  Best Combo: ${engine.bestCombo}x", width / 2f, height / 2f + 35f, scoreDisplayPaint)

            // Diamonds earned this run
            if (engine.diamondsEarnedThisRun > 0) {
                val dPaint = Paint(scoreDisplayPaint).apply { color = 0xFF00E5FF.toInt() }
                drawOutlinedText(canvas, "\uD83D\uDC8E +${engine.diamondsEarnedThisRun} Diamonds earned!", width / 2f, height / 2f + 70f, dPaint)
            }

            if (engine.isEndlessMode) {
                val record = engine.endlessHighWave
                if (engine.wave >= record) {
                    val newBestPaint = Paint(goldTextPaint).apply { textSize = 32f }
                    drawOutlinedText(canvas, "\u2B50 NEW ENDLESS RECORD! \u2B50", width / 2f, height / 2f + 110f, newBestPaint)
                } else {
                    val hsPaint = Paint(goldTextPaint).apply { textSize = 26f }
                    drawOutlinedText(canvas, "Endless Record: Wave $record", width / 2f, height / 2f + 110f, hsPaint)
                }
            } else {
                if (engine.score >= engine.highScore) {
                    val newBestPaint = Paint(goldTextPaint).apply { textSize = 32f }
                    drawOutlinedText(canvas, "\u2B50 NEW HIGH SCORE! \u2B50", width / 2f, height / 2f + 110f, newBestPaint)
                } else {
                    val hsPaint = Paint(goldTextPaint).apply { textSize = 26f }
                    drawOutlinedText(canvas, "High Score: ${engine.highScore}  |  Best Wave: ${engine.highWave}", width / 2f, height / 2f + 110f, hsPaint)
                }
            }

            drawOutlinedText(canvas, "Tap to restart", width / 2f, height / 2f + 160f, goldTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tx = event.x
            val ty = event.y

            synchronized(engine.lock) {
                if (engine.gameOver) {
                    engine.restart()
                    return true
                }

                // Tower placement mode
                if (placementMode != null) {
                    if (engine.placeTower(tx, ty, placementMode!!)) {
                        post { onGoldChanged?.invoke(engine.gold) }
                    }
                    placementMode = null
                    return true
                }

                // Check if tapped on a tower (to select it)
                val tapped = engine.towers.find { it.distanceTo(tx, ty) < it.size + 20f }
                if (tapped != null) {
                    selectedTower = tapped
                    return true
                }
                selectedTower = null

                // Otherwise move player
                engine.player.moveTo(tx, ty)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        running = false
        try {
            gameThread?.join(2000)
        } catch (_: InterruptedException) { }
        gameThread = null
    }

    fun resume() {
        if (!running) {
            running = true
            gameThread = Thread(this)
            gameThread?.start()
        }
    }

    fun getSelectedTower(): Tower? = selectedTower

    /** Smoothly interpolate between two colors */
    private fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val a = ((c1 shr 24 and 0xFF) * (1 - t) + (c2 shr 24 and 0xFF) * t).toInt()
        val r = ((c1 shr 16 and 0xFF) * (1 - t) + (c2 shr 16 and 0xFF) * t).toInt()
        val g = ((c1 shr 8 and 0xFF) * (1 - t) + (c2 shr 8 and 0xFF) * t).toInt()
        val b = ((c1 and 0xFF) * (1 - t) + (c2 and 0xFF) * t).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** Draw text with dark outline for readability on any background */
    private fun drawOutlinedText(canvas: Canvas, text: String, x: Float, y: Float, fillPaint: Paint) {
        textOutlinePaint.textSize = fillPaint.textSize
        textOutlinePaint.textAlign = fillPaint.textAlign
        textOutlinePaint.typeface = fillPaint.typeface
        canvas.drawText(text, x, y, textOutlinePaint)
        canvas.drawText(text, x, y, fillPaint)
    }

    /** Perspective depth: objects near bottom (closer) are slightly bigger */
    private fun depthFactor(y: Float): Float {
        val t = (y / height.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
        return 0.8f + t * 0.25f  // range: 0.8 (top) to 1.05 (bottom)
    }

    /** Generate terrain paths & decorations when surface size is known */
    private fun generateTerrain() {
        val w = width.toFloat()
        val h = height.toFloat()
        val bx = engine.baseX
        val by = engine.baseY

        // Build curved paths from each spawn point to the base
        val spawns = listOf(
            Pair(w * 0.1f, -40f),
            Pair(w * 0.3f, -40f),
            Pair(w * 0.5f, -40f),
            Pair(w * 0.7f, -40f),
            Pair(w * 0.9f, -40f),
            Pair(-40f, h * 0.3f),
            Pair(w + 40f, h * 0.3f)
        )
        val paths = mutableListOf<Path>()
        for ((sx, sy) in spawns) {
            val p = Path()
            p.moveTo(sx, sy)
            // Curved control point offset for organic feel
            val cx = (sx + bx) / 2f + (sx - bx) * 0.15f
            val cy = (sy + by) / 2f
            p.quadTo(cx, cy, bx, by)
            paths.add(p)
        }
        pathCache = paths

        // Generate decorations avoiding paths (use path distance check)
        val decos = mutableListOf<Decoration>()
        val rng = java.util.Random(77L)
        val groundTop = h * 0.12f
        repeat(45) { i ->
            val dx = rng.nextFloat() * w
            val dy = groundTop + rng.nextFloat() * (h - groundTop - 80f)
            // Skip decorations near base
            val dbx = dx - bx; val dby = dy - by
            if (dbx * dbx + dby * dby < 90f * 90f) return@repeat
            // Skip decorations too close to paths (rough check)
            var onPath = false
            for ((sx, sy) in spawns) {
                val t = ((dx - sx) * (bx - sx) + (dy - sy) * (by - sy)) /
                        ((bx - sx) * (bx - sx) + (by - sy) * (by - sy) + 0.01f)
                val ct = t.coerceIn(0f, 1f)
                val px = sx + ct * (bx - sx)
                val py = sy + ct * (by - sy)
                val pdx = dx - px; val pdy = dy - py
                if (pdx * pdx + pdy * pdy < 50f * 50f) { onPath = true; break }
            }
            if (onPath) return@repeat
            val type = rng.nextInt(4) // 0=tree, 1=rock, 2=bush, 3=flower
            val scale = 0.7f + rng.nextFloat() * 0.6f
            decos.add(Decoration(dx, dy, type, scale, rng.nextInt()))
        }
        decorations = decos
        terrainDirty = false
    }

    /** Draw terrain decorations — trees, rocks, bushes, flowers */
    private fun drawDecorations(canvas: Canvas) {
        for (d in decorations) {
            val x = d.x; val y = d.y; val s = d.scale
            when (d.type) {
                0 -> { // Tree
                    terrainPaint.color = trunkColor
                    canvas.drawRect(x - 3f * s, y - 8f * s, x + 3f * s, y + 12f * s, terrainPaint)
                    shadowPaint.alpha = 25
                    canvas.drawOval(x - 10f * s, y + 8f * s, x + 10f * s, y + 14f * s, shadowPaint)
                    terrainPaint.color = grassColor
                    canvas.drawCircle(x, y - 8f * s, 12f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x - 3f * s, y - 12f * s, 8f * s, terrainPaint)
                    canvas.drawCircle(x + 4f * s, y - 6f * s, 7f * s, terrainPaint)
                }
                1 -> { // Rock
                    shadowPaint.alpha = 20
                    canvas.drawOval(x - 8f * s, y + 3f * s, x + 8f * s, y + 7f * s, shadowPaint)
                    terrainPaint.color = rockColor
                    val rp = Path()
                    rp.moveTo(x - 7f * s, y + 2f * s)
                    rp.lineTo(x - 5f * s, y - 6f * s)
                    rp.lineTo(x + 2f * s, y - 8f * s)
                    rp.lineTo(x + 8f * s, y - 3f * s)
                    rp.lineTo(x + 6f * s, y + 3f * s)
                    rp.close()
                    canvas.drawPath(rp, terrainPaint)
                    terrainPaint.color = rockHighlightColor
                    canvas.drawCircle(x - 2f * s, y - 4f * s, 2f * s, terrainPaint)
                }
                2 -> { // Bush
                    shadowPaint.alpha = 18
                    canvas.drawOval(x - 8f * s, y + 2f * s, x + 8f * s, y + 6f * s, shadowPaint)
                    terrainPaint.color = grassColor
                    canvas.drawCircle(x - 4f * s, y, 6f * s, terrainPaint)
                    canvas.drawCircle(x + 3f * s, y - 1f * s, 5f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x, y - 3f * s, 5f * s, terrainPaint)
                }
                3 -> { // Flowers
                    terrainPaint.color = grassLightColor
                    canvas.drawLine(x, y, x, y - 6f * s, terrainPaint)
                    val fc = when (d.seed % 4) {
                        0 -> 0xFFFF6B6B.toInt()
                        1 -> 0xFFFFD93D.toInt()
                        2 -> 0xFFC084FC.toInt()
                        else -> 0xFF6BCB77.toInt()
                    }
                    terrainPaint.color = fc
                    canvas.drawCircle(x, y - 7f * s, 3f * s, terrainPaint)
                    terrainPaint.color = 0xFFFFFFCC.toInt()
                    canvas.drawCircle(x, y - 7f * s, 1.5f * s, terrainPaint)
                }
            }
        }
    }

    // Reusable paint for star drawing
    private val paint = Paint().apply { isAntiAlias = true }
}
