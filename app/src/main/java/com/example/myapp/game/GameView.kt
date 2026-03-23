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
    private var running = false
    private val engine = GameEngine(context)

    var onGoldChanged: ((Int) -> Unit)? = null
    var onWaveChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int, Int) -> Unit)? = null
    var onStatsChanged: (() -> Unit)? = null

    // Paints
    private val bgPaint = Paint().apply { color = 0xFF1B2838.toInt() }
    private val gridPaint = Paint().apply { color = 0xFF243447.toInt(); strokeWidth = 1f }
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
        running = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.init(width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        gameThread?.join()
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
                    drawGame(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            // Notify UI thread
            post {
                onGoldChanged?.invoke(engine.gold)
                onWaveChanged?.invoke(engine.wave)
                onStatsChanged?.invoke()
                if (engine.gameOver) {
                    onGameOver?.invoke(engine.score, engine.wave)
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

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Grid
        val gridSize = 60f
        var gx = 0f
        while (gx < width) {
            canvas.drawLine(gx, 0f, gx, height.toFloat(), gridPaint)
            gx += gridSize
        }
        var gy = 0f
        while (gy < height) {
            canvas.drawLine(0f, gy, width.toFloat(), gy, gridPaint)
            gy += gridSize
        }

        // Freeze overlay
        if (engine.freezeTimer > 0) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), freezeOverlayPaint)
        }

        // Base
        val baseHpRatio = engine.baseHp / engine.maxBaseHp
        val bPaint = if (baseHpRatio > 0.3f) basePaint else baseDamagedPaint
        canvas.drawCircle(engine.baseX, engine.baseY, 50f, bPaint)
        canvas.drawText("\uD83C\uDFF0", engine.baseX, engine.baseY + 16f, emojiPaint)

        // Base HP bar
        val barW = 80f
        val barH = 8f
        val barX = engine.baseX - barW / 2
        val barY = engine.baseY + 55f
        canvas.drawRect(barX, barY, barX + barW, barY + barH, hpBarBgPaint)
        val hpPaint = if (baseHpRatio > 0.3f) hpBarPaint else hpBarDamagedPaint
        canvas.drawRect(barX, barY, barX + barW * baseHpRatio, barY + barH, hpPaint)

        // Towers & ranges
        for (tower in engine.towers) {
            if (tower == selectedTower) {
                canvas.drawCircle(tower.x, tower.y, tower.range, towerRangePaint)
            }
            canvas.drawText(tower.type.emoji, tower.x, tower.y + 14f, smallEmojiPaint)
            if (tower.level > 1) {
                val lvlPaint = Paint(goldTextPaint).apply { textSize = 18f }
                canvas.drawText("Lv${tower.level}", tower.x, tower.y - 25f, lvlPaint)
            }
        }

        // Enemies
        for (enemy in engine.enemies) {
            val ePaint = Paint().apply {
                color = when {
                    enemy.hitFlash > 0 -> Color.WHITE
                    engine.freezeTimer > 0 -> 0xFF81D4FA.toInt()
                    else -> enemy.type.color
                }
                isAntiAlias = true
            }
            canvas.drawCircle(enemy.x, enemy.y, enemy.size, ePaint)
            canvas.drawText(enemy.type.emoji, enemy.x, enemy.y + 14f, smallEmojiPaint)

            // HP bar
            val ehpRatio = enemy.hp / enemy.maxHp
            val eBarW = enemy.size * 2
            val eBarX = enemy.x - eBarW / 2
            val eBarY = enemy.y - enemy.size - 10f
            canvas.drawRect(eBarX, eBarY, eBarX + eBarW, eBarY + 5f, hpBarBgPaint)
            canvas.drawRect(eBarX, eBarY, eBarX + eBarW * ehpRatio, eBarY + 5f, hpBarPaint)

            // Boss: large HP bar at top of screen
            if (enemy.type == EnemyType.BOSS) {
                val bossBarW = width * 0.7f
                val bossBarX = (width - bossBarW) / 2
                val bossBarY = 100f
                canvas.drawRect(bossBarX, bossBarY, bossBarX + bossBarW, bossBarY + 14f, hpBarBgPaint)
                val bossHpPaint = Paint().apply { color = 0xFFE91E63.toInt() }
                canvas.drawRect(bossBarX, bossBarY, bossBarX + bossBarW * ehpRatio, bossBarY + 14f, bossHpPaint)
                val bossTxt = Paint(waveTextPaint).apply { textSize = 24f; color = 0xFFE91E63.toInt() }
                canvas.drawText("\u2620\uFE0F BOSS  ${enemy.hp.toInt()}/${enemy.maxHp.toInt()}", width / 2f, bossBarY - 8f, bossTxt)
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

        // Player
        canvas.drawCircle(engine.player.x, engine.player.y, engine.player.size, playerPaint)
        canvas.drawText("\uD83E\uDDDD", engine.player.x, engine.player.y + 16f, emojiPaint)
        canvas.drawCircle(engine.player.x, engine.player.y, engine.player.attackRange, playerRangePaint)

        // Player HP bar
        val phRatio = engine.player.hp / engine.player.maxHp
        val pBarW = 60f
        val pBarX = engine.player.x - pBarW / 2
        val pBarY = engine.player.y - engine.player.size - 15f
        canvas.drawRect(pBarX, pBarY, pBarX + pBarW, pBarY + 6f, hpBarBgPaint)
        canvas.drawRect(pBarX, pBarY, pBarX + pBarW * phRatio, pBarY + 6f, Paint().apply { color = 0xFF42A5F5.toInt() })

        // Floating texts
        for (ft in engine.floatingTexts) {
            floatPaint.color = ft.color
            floatPaint.textSize = ft.size
            floatPaint.alpha = (255 * (ft.life / ft.maxLife).coerceIn(0f, 1f)).toInt()
            canvas.drawText(ft.text, ft.x, ft.y, floatPaint)
        }

        // Wave info at top
        if (!engine.waveInProgress && engine.wave > 0 && !engine.gameOver) {
            val countdown = engine.waveTimer.toInt() + 1
            canvas.drawText("Next wave in ${countdown}s", width / 2f, 80f, waveTextPaint)
        } else if (engine.waveInProgress) {
            canvas.drawText("Wave ${engine.wave}  \u2694\uFE0F  ${engine.enemies.size} enemies", width / 2f, 80f, waveTextPaint)
        }

        // Wave banner overlay
        if (engine.showWaveBanner) {
            val bannerPaint = Paint().apply { color = 0xCC000000.toInt() }
            canvas.drawRect(0f, height * 0.35f, width.toFloat(), height * 0.55f, bannerPaint)
            val wp = Paint(waveTextPaint).apply { textSize = 56f; color = 0xFFFFD700.toInt() }
            val isBoss = engine.wave % 5 == 0
            val waveLabel = if (isBoss) "\u2620\uFE0F BOSS WAVE ${engine.wave} \u2620\uFE0F" else "\u2694\uFE0F WAVE ${engine.wave} \u2694\uFE0F"
            canvas.drawText(waveLabel, width / 2f, height * 0.47f, wp)
        }

        // Combo display (top right)
        if (engine.comboCount >= 3) {
            comboPaint.textSize = 28f + engine.comboCount.coerceAtMost(20) * 1.5f
            canvas.drawText("${engine.comboCount}x COMBO", width - 20f, 170f, comboPaint)
            val multPaint = Paint(comboPaint).apply { textSize = 22f; color = 0xFFFFEB3B.toInt() }
            canvas.drawText("x${String.format("%.1f", engine.comboMultiplier)} gold", width - 20f, 198f, multPaint)
        }

        // High score (top left-ish)
        if (engine.highScore > 0) {
            val hsPaint = Paint(goldTextPaint).apply { textSize = 20f; textAlign = Paint.Align.LEFT }
            canvas.drawText("\u2B50 Best: ${engine.highScore}", 10f, 130f, hsPaint)
        }

        // Achievement banner
        val ach = engine.newAchievement
        if (ach != null && engine.achievementBannerTimer > 0) {
            val abY = height * 0.25f
            val abPaint = Paint().apply { color = 0xDD1B2838.toInt() }
            canvas.drawRect(width * 0.1f, abY, width * 0.9f, abY + 70f, abPaint)
            val borderPaint = Paint().apply {
                color = 0xFFFFD700.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f
            }
            canvas.drawRect(width * 0.1f, abY, width * 0.9f, abY + 70f, borderPaint)
            canvas.drawText("${ach.emoji} Achievement Unlocked!", width / 2f, abY + 28f, achievePaint)
            val descPaint = Paint(achievePaint).apply { textSize = 22f; color = Color.WHITE }
            canvas.drawText("${ach.title} — ${ach.description}", width / 2f, abY + 55f, descPaint)
        }

        // Placement mode indicator
        if (placementMode != null) {
            val placeTextPaint = Paint(goldTextPaint).apply { textSize = 32f }
            canvas.drawText("Tap to place ${placementMode!!.emoji} tower", width / 2f, 160f, placeTextPaint)
        }

        canvas.restore() // End screen shake

        // Game over overlay (drawn outside shake)
        if (engine.gameOver) {
            val overlay = Paint().apply { color = 0xAA000000.toInt() }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlay)

            val goPaint = Paint().apply {
                color = 0xFFF44336.toInt(); textSize = 64f; isAntiAlias = true
                textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("GAME OVER", width / 2f, height / 2f - 80f, goPaint)

            val scorePaint = Paint(waveTextPaint).apply { textSize = 36f }
            canvas.drawText("Wave: ${engine.wave}  |  Score: ${engine.score}", width / 2f, height / 2f - 10f, scorePaint)
            canvas.drawText("Kills: ${engine.totalKills}  |  Best Combo: ${engine.bestCombo}x", width / 2f, height / 2f + 35f, scorePaint)

            if (engine.score >= engine.highScore) {
                val newBestPaint = Paint(goldTextPaint).apply { textSize = 32f }
                canvas.drawText("\u2B50 NEW HIGH SCORE! \u2B50", width / 2f, height / 2f + 80f, newBestPaint)
            } else {
                val hsPaint = Paint(goldTextPaint).apply { textSize = 26f }
                canvas.drawText("High Score: ${engine.highScore}  |  Best Wave: ${engine.highWave}", width / 2f, height / 2f + 80f, hsPaint)
            }

            canvas.drawText("Tap to restart", width / 2f, height / 2f + 130f, goldTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tx = event.x
            val ty = event.y

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
            return true
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        running = false
        gameThread?.join()
    }

    fun resume() {
        if (!running) {
            running = true
            gameThread = Thread(this)
            gameThread?.start()
        }
    }

    fun getSelectedTower(): Tower? = selectedTower
}
