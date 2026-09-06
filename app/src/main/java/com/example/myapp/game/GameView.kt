package com.example.myapp.game

import android.content.Context
import android.graphics.*
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.myapp.AndroidGameAudio
import com.example.myapp.GameStrings
import com.example.myapp.MusicManager
import com.example.myapp.SoundManager

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    @Volatile private var running = false
    private val engine = GameEngine(
        AndroidGamePreferences(context.getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)),
        AndroidGameAudio()
    )
    init {
        GameEngineHolder.engine = engine
        GameStrings.init(context)
    }

    var onGoldChanged: ((Int) -> Unit)? = null
    var onWaveChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int, Int) -> Unit)? = null
    var onDiamondsChanged: ((Int) -> Unit)? = null
    var onTowerSelected: ((Tower?) -> Unit)? = null
    var onPlacementModeChanged: ((Boolean) -> Unit)? = null
    var onCampaignVictory: (() -> Unit)? = null
    var onMilestoneBuff: (() -> Unit)? = null
    var onMerchantShop: (() -> Unit)? = null
    var onWaveStateChanged: ((inProgress: Boolean, waveTimer: Float) -> Unit)? = null
    @Volatile private var gameOverFired = false
    @Volatile private var milestoneDialogShown = false
    @Volatile private var merchantDialogShown = false
    private var lastFpsTime = System.nanoTime()
    private var frameCount = 0
    private var displayFps = 0

    // Paints
    // Theme configuration for the active biome
    private var currentTheme = MapTheme.forType(MapType.CLASSIC)
    private var grassColor = currentTheme.grassColor
    private var grassLightColor = currentTheme.grassLightColor
    private var pathColor = currentTheme.pathColor
    private var pathEdgeColor = currentTheme.pathEdgeColor
    private var trunkColor = currentTheme.trunkColor
    private var rockColor = currentTheme.rockColor
    private var rockHighlightColor = currentTheme.rockHighlightColor

    /** Update terrain palette based on map theme */
    private fun updateThemeColors() {
        currentTheme = MapTheme.forType(engine.mapType)
        grassColor = currentTheme.grassColor
        grassLightColor = currentTheme.grassLightColor
        pathColor = currentTheme.pathColor
        pathEdgeColor = currentTheme.pathEdgeColor
        trunkColor = currentTheme.trunkColor
        rockColor = currentTheme.rockColor
        rockHighlightColor = currentTheme.rockHighlightColor
    }
    // Terrain paints (pre-allocated)
    private val terrainPaint = Paint().apply { isAntiAlias = true }
    private val pathPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val shadowPaint = Paint().apply { isAntiAlias = true; color = 0x33000000 }
    private val textOutlinePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 4f; color = 0xCC000000.toInt(); strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND }
    // Cached terrain decorations (regenerated on surface size change)
    private data class Decoration(val x: Float, val y: Float, val type: Int, val scale: Float, val seed: Int)
    private var decorations: List<Decoration> = emptyList()
    private var pathCache: List<Path> = emptyList()
    private var waterPhase: Float = 0f
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
    private val hpBarBgPaint = Paint().apply { color = 0xFF333333.toInt() }
    private val hpBarPaint = Paint().apply { color = 0xFF4CAF50.toInt() }
    private val hpBarDamagedPaint = Paint().apply { color = 0xFFF44336.toInt() }
    private val playerRangePaint = Paint().apply {
        color = 0x2242A5F5; style = Paint.Style.STROKE; strokeWidth = 2f
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE; textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER
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

    // Zero-allocation shockwave visual pool
    private class ShockwaveRing {
        var active: Boolean = false
        var x: Float = 0f
        var y: Float = 0f
        var currentRadius: Float = 0f
        var maxRadius: Float = 140f
        var color: Int = Color.WHITE
        var life: Float = 0f
        var maxLife: Float = 0.35f
        var strokeWidth: Float = 4f
    }
    private val shockwavePool = Array(16) { ShockwaveRing() }
    private val shockwavePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    fun triggerShockwave(x: Float, y: Float, color: Int = 0xFF00E5FF.toInt(), maxRadius: Float = 140f, duration: Float = 0.35f, strokeWidth: Float = 5f) {
        for (sw in shockwavePool) {
            if (!sw.active) {
                sw.active = true
                sw.x = x
                sw.y = y
                sw.currentRadius = 8f
                sw.maxRadius = maxRadius
                sw.color = color
                sw.life = duration
                sw.maxLife = duration
                sw.strokeWidth = strokeWidth
                break
            }
        }
    }

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
    // Pre-allocated paints for wave banner, game over, combos, achievements (avoid GC in render loop)
    private val bannerBgPaint = Paint().apply { color = 0xCC000000.toInt() }
    private val waveBannerPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 90f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val waveBannerSubPaint = Paint().apply {
        color = 0xFFEEEEEE.toInt(); textSize = 44f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val achBgPaint = Paint().apply { color = 0xDD1B2838.toInt() }
    private val achBorderPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true
    }
    private val achDescPaint = Paint().apply {
        color = Color.WHITE; textSize = 22f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val comboMultPaint = Paint().apply {
        color = 0xFFFFEB3B.toInt(); textSize = 22f; isAntiAlias = true; textAlign = Paint.Align.RIGHT; typeface = Typeface.DEFAULT_BOLD
    }
    private val goNewBestPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 32f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val goHighScorePaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 26f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val goDiamondPaint = Paint().apply {
        color = 0xFF00E5FF.toInt(); textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val placeTextPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 32f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val victoryPaint = Paint().apply {
        color = 0xFF4CAF50.toInt(); textSize = 64f; isAntiAlias = true
        textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val hsLeftPaint = Paint().apply {
        color = 0xFFFFD700.toInt(); textSize = 22f; isAntiAlias = true; textAlign = Paint.Align.LEFT; typeface = Typeface.DEFAULT_BOLD
    }

    // Tower placement mode
    var placementMode: TowerType? = null
    var blockadePlacementMode: Boolean = false
    var trapPlacementMode: TrapType? = null
    private var selectedTower: Tower? = null
    // Long-press tower stats
    private var longPressDownTime: Long = 0L
    private var longPressDownX: Float = 0f
    private var longPressDownY: Float = 0f
    private var towerStatsPopup: Tower? = null
    private var placementTouchX = -1f
    private var placementTouchY = -1f
    // Victory celebration
    private data class Confetti(var x: Float, var y: Float, var vx: Float, var vy: Float,
                                var life: Float, val color: Int, val size: Float, var rotation: Float, val spin: Float)
    private val confettiParticles = mutableListOf<Confetti>()
    private var victoryAnimTimer: Float = 0f
    private var victoryScoreDisplay: Int = 0
    private var victoryTriggered: Boolean = false
    // Path arrow drawing
    private val arrowPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val arrowPath = Path()

    // Battery saver / adaptive performance
    private var batterySaverOn = false
    private var lastBatteryCheck = 0L
    private val frameMs: Long get() = if (batterySaverOn) 33L else 16L  // 30 vs 60 fps

    init {
        holder.addCallback(this)
        isFocusable = true
        SpriteManager.initialize()
    }

    fun getEngine(): GameEngine = engine

    override fun surfaceCreated(holder: SurfaceHolder) {
        SpriteManager.initialize()
        engine.init(width.toFloat(), height.toFloat())
        updateThemeColors()
        generateTerrain()
        startGameThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Only update screen dimensions—init() already ran in surfaceCreated
        engine.screenW = width.toFloat()
        engine.screenH = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameThread()
        // Clear callbacks to prevent activity memory leak
        onGoldChanged = null
        onWaveChanged = null
        onGameOver = null
        onDiamondsChanged = null
        onTowerSelected = null
        onCampaignVictory = null
        onMilestoneBuff = null
        onMerchantShop = null
        onWaveStateChanged = null
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
            lastTime = now

            val gameDt = dt * engine.gameSpeed
            engine.update(gameDt)
            waterPhase = (waterPhase + dt * 1.8f) % 1000f

            // Drain visual shockwaves from engine (zero GC allocations)
            synchronized(engine.lock) {
                if (engine.pendingShockwaves.isNotEmpty()) {
                    for (i in 0 until engine.pendingShockwaves.size) {
                        val sw = engine.pendingShockwaves[i]
                        triggerShockwave(sw.x, sw.y, sw.color, sw.maxRadius)
                    }
                    engine.pendingShockwaves.clear()
                }
            }

            // Synchronize adaptive music intensity and weather ambience
            MusicManager.updateGameState(
                engine.currentWeather,
                engine.waveInProgress,
                engine.currentBoss != null || engine.enemies.any { it.isBoss }
            )

            // Victory celebration confetti
            if (engine.campaignVictory || engine.gameOver) {
                if (!victoryTriggered && engine.campaignVictory) {
                    victoryTriggered = true
                    victoryAnimTimer = 0f
                    victoryScoreDisplay = 0
                    // Spawn initial burst of confetti
                    val w = holder.surfaceFrame.width().toFloat()
                    repeat(80) {
                        confettiParticles.add(Confetti(
                            x = (Math.random() * w).toFloat(),
                            y = -20f - (Math.random() * 200).toFloat(),
                            vx = ((Math.random() - 0.5) * 120).toFloat(),
                            vy = (100 + Math.random() * 200).toFloat(),
                            life = 3f + (Math.random() * 2).toFloat(),
                            color = listOf(0xFFFF1744.toInt(), 0xFFFFD700.toInt(), 0xFF00E676.toInt(),
                                0xFF2979FF.toInt(), 0xFFFF9100.toInt(), 0xFFE040FB.toInt(),
                                0xFF00BCD4.toInt(), 0xFFFFEB3B.toInt()).random(),
                            size = 6f + (Math.random() * 6).toFloat(),
                            rotation = (Math.random() * 360).toFloat(),
                            spin = ((Math.random() - 0.5) * 400).toFloat()
                        ))
                    }
                }
                victoryAnimTimer += dt
                // Animated score counter
                if (victoryScoreDisplay < engine.score) {
                    victoryScoreDisplay = (victoryScoreDisplay + (engine.score * dt * 2).toInt().coerceAtLeast(1)).coerceAtMost(engine.score)
                }
                // Ongoing confetti trickle
                if (engine.campaignVictory && confettiParticles.size < 120 && Math.random() < dt * 15) {
                    val w = holder.surfaceFrame.width().toFloat()
                    confettiParticles.add(Confetti(
                        x = (Math.random() * w).toFloat(), y = -10f,
                        vx = ((Math.random() - 0.5) * 80).toFloat(),
                        vy = (80 + Math.random() * 150).toFloat(),
                        life = 3f + (Math.random() * 2).toFloat(),
                        color = listOf(0xFFFF1744.toInt(), 0xFFFFD700.toInt(), 0xFF00E676.toInt(),
                            0xFF2979FF.toInt(), 0xFFFF9100.toInt(), 0xFFE040FB.toInt()).random(),
                        size = 5f + (Math.random() * 5).toFloat(),
                        rotation = (Math.random() * 360).toFloat(),
                        spin = ((Math.random() - 0.5) * 300).toFloat()
                    ))
                }
            } else {
                if (victoryTriggered) {
                    victoryTriggered = false
                    confettiParticles.clear()
                }
            }
            // Update confetti
            confettiParticles.forEach { c ->
                c.x += c.vx * dt
                c.y += c.vy * dt
                c.vy += 40f * dt  // gravity
                c.vx *= 0.99f     // air drag
                c.rotation += c.spin * dt
                c.life -= dt
            }
            confettiParticles.removeAll { it.life <= 0 || it.y > (holder.surfaceFrame.height() + 50) }

            // Update active shockwaves (zero GC allocations)
            for (sw in shockwavePool) {
                if (sw.active) {
                    sw.life -= dt
                    if (sw.life <= 0f) sw.active = false
                }
            }

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
            val isVictory: Boolean
            val isMilestonePending: Boolean
            val isMerchantPending: Boolean
            val waveInProgressSnap: Boolean
            val waveTimerSnap: Float
            synchronized(engine.lock) {
                goldSnap = engine.gold
                waveSnap = engine.wave
                diamondSnap = engine.skillTree.diamonds
                isGameOver = engine.gameOver
                scoreSnap = engine.score
                isVictory = engine.campaignVictory
                isMilestonePending = engine.endlessMilestonePending
                isMerchantPending = engine.merchantShopPending
                waveInProgressSnap = engine.waveInProgress
                waveTimerSnap = engine.waveTimer
            }
            post {
                onGoldChanged?.invoke(goldSnap)
                onWaveChanged?.invoke(waveSnap)
                onDiamondsChanged?.invoke(diamondSnap)
                onWaveStateChanged?.invoke(waveInProgressSnap, waveTimerSnap)
                // Haptic feedback
                val haptic = engine.hapticPending
                if (haptic > 0) {
                    engine.hapticPending = 0
                    if (haptic == 2) {
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    } else {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
                if (isGameOver && !gameOverFired) {
                    gameOverFired = true
                    onGameOver?.invoke(scoreSnap, waveSnap)
                }
                if (isMilestonePending && !milestoneDialogShown) {
                    milestoneDialogShown = true
                    onMilestoneBuff?.invoke()
                }
                if (!isMilestonePending) {
                    milestoneDialogShown = false
                }
                if (isMerchantPending && !merchantDialogShown) {
                    merchantDialogShown = true
                    onMerchantShop?.invoke()
                }
                if (!isMerchantPending) {
                    merchantDialogShown = false
                }
                // Victory overlay is drawn; player taps to continue (handled in onTouchEvent)
            }

            // Battery saver check (every 30s)
            val nowMs = System.currentTimeMillis()
            if (nowMs - lastBatteryCheck > 30_000) {
                lastBatteryCheck = nowMs
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
                batterySaverOn = level <= 15
            }

            // Cap at ~60 fps (or 30 in battery saver)
            val frameTime = (System.nanoTime() - now) / 1_000_000
            if (frameTime < frameMs) {
                Thread.sleep(frameMs - frameTime)
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        // FPS counter
        frameCount++
        val fpsNow = System.nanoTime()
        if (fpsNow - lastFpsTime >= 1_000_000_000L) {
            displayFps = frameCount
            frameCount = 0
            lastFpsTime = fpsNow
        }

        // Screen shake
        canvas.save()
        if (engine.shakeTimer > 0 && com.example.myapp.SoundManager.isShakeEnabled(context)) {
            val sx = ((Math.random() - 0.5) * engine.shakeIntensity * 2).toFloat()
            val sy = ((Math.random() - 0.5) * engine.shakeIntensity * 2).toFloat()
            canvas.translate(sx, sy)
        }

        // === SKY ===
        val night = engine.isNight
        val weather = engine.currentWeather
        val skyTop = when (weather) {
            WeatherEvent.BLOOD_MOON -> 0xFF3B0B14.toInt()
            WeatherEvent.THUNDERSTORM -> 0xFF0D1B2A.toInt()
            WeatherEvent.SOLAR_ECLIPSE -> 0xFF1A1A2E.toInt()
            else -> if (night) currentTheme.skyTopNight else currentTheme.skyTopDay
        }
        val skyBot = when (weather) {
            WeatherEvent.BLOOD_MOON -> 0xFF8A1C28.toInt()
            WeatherEvent.THUNDERSTORM -> 0xFF27384E.toInt()
            WeatherEvent.SOLAR_ECLIPSE -> 0xFF3D2C54.toInt()
            else -> if (night) currentTheme.skyBotNight else currentTheme.skyBotDay
        }
        terrainPaint.shader = LinearGradient(
            0f, 0f, 0f, height * 0.14f,
            skyTop, skyBot, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), terrainPaint)
        terrainPaint.shader = null

        // Stars at night, solar eclipse, or in enchanted twilight nebula
        if (night || weather == WeatherEvent.SOLAR_ECLIPSE || currentTheme.mapType == MapType.ENCHANTED) {
            paint.color = 0xCCFFFFFF.toInt()
            val starRng = java.util.Random(42L)
            val starCount = if (currentTheme.mapType == MapType.ENCHANTED && !night) 45 else 30
            repeat(starCount) {
                val sx = starRng.nextFloat() * width
                val sy = starRng.nextFloat() * height * 0.12f
                val sr = 1f + starRng.nextFloat() * 1.5f
                paint.color = if (currentTheme.mapType == MapType.ENCHANTED && starRng.nextBoolean()) 0xEE80D8FF.toInt() else 0xCCFFFFFF.toInt()
                canvas.drawCircle(sx, sy, sr, paint)
            }
        }

        // Celestial Orbs (Blood Moon / Solar Eclipse)
        val groundTop0 = height * 0.12f
        if (weather == WeatherEvent.BLOOD_MOON) {
            val moonX = width * 0.78f
            val moonY = groundTop0 * 0.42f
            paint.color = 0x44FF1744.toInt()
            canvas.drawCircle(moonX, moonY, 32f, paint)
            paint.color = 0x77D50000.toInt()
            canvas.drawCircle(moonX, moonY, 24f, paint)
            paint.color = 0xFFB71C1C.toInt()
            canvas.drawCircle(moonX, moonY, 18f, paint)
            paint.color = 0xFFFF5252.toInt()
            canvas.drawCircle(moonX - 3f, moonY - 3f, 13f, paint)
            paint.color = 0xFF7F0000.toInt()
            canvas.drawCircle(moonX - 6f, moonY - 6f, 11f, paint)
        } else if (weather == WeatherEvent.SOLAR_ECLIPSE) {
            val sunX = width * 0.78f
            val sunY = groundTop0 * 0.42f
            paint.color = 0x55FFD700.toInt()
            canvas.drawCircle(sunX, sunY, 36f, paint)
            paint.color = 0x77FFAB00.toInt()
            canvas.drawCircle(sunX, sunY, 26f, paint)
            paint.color = 0xFF121218.toInt()
            canvas.drawCircle(sunX, sunY, 18f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            paint.color = 0xFFFFE57F.toInt()
            canvas.drawCircle(sunX, sunY, 18f, paint)
            paint.style = Paint.Style.FILL
        }

        // Distant mountains silhouette
        terrainPaint.color = if (night) currentTheme.mountainColorNight else currentTheme.mountainColorDay
        terrainPaint.alpha = if (night) 60 else 40
        val mtPath = Path()
        mtPath.moveTo(-10f, groundTop0 + 15f)
        mtPath.lineTo(width * 0.08f, groundTop0 - 30f)
        mtPath.lineTo(width * 0.18f, groundTop0 + 5f)
        mtPath.lineTo(width * 0.30f, groundTop0 - 50f)
        mtPath.lineTo(width * 0.45f, groundTop0 + 10f)
        mtPath.lineTo(width * 0.55f, groundTop0 - 35f)
        mtPath.lineTo(width * 0.68f, groundTop0 + 8f)
        mtPath.lineTo(width * 0.80f, groundTop0 - 45f)
        mtPath.lineTo(width * 0.92f, groundTop0 + 5f)
        mtPath.lineTo(width + 10f, groundTop0 - 20f)
        mtPath.lineTo(width + 10f, groundTop0 + 30f)
        mtPath.lineTo(-10f, groundTop0 + 30f)
        mtPath.close()
        canvas.drawPath(mtPath, terrainPaint)

        // Mountain peaks / highlights
        if (currentTheme.hasSnowCaps) {
            terrainPaint.color = 0xFFFFFFFF.toInt()
            terrainPaint.alpha = if (night) 15 else 25
            canvas.drawCircle(width * 0.30f, groundTop0 - 46f, 10f, terrainPaint)
            canvas.drawCircle(width * 0.80f, groundTop0 - 41f, 9f, terrainPaint)
            canvas.drawCircle(width * 0.55f, groundTop0 - 31f, 7f, terrainPaint)
            terrainPaint.alpha = 255
        } else currentTheme.mountainHighlightColor?.let { hlColor ->
            terrainPaint.color = hlColor
            terrainPaint.alpha = if (night) 50 else 70
            canvas.drawCircle(width * 0.30f, groundTop0 - 46f, 7f, terrainPaint)
            canvas.drawCircle(width * 0.80f, groundTop0 - 41f, 6f, terrainPaint)
            canvas.drawCircle(width * 0.55f, groundTop0 - 31f, 5f, terrainPaint)
            terrainPaint.alpha = 255
        }

        if (night) {
            // Moon glow
            paint.color = 0x18B0BEC5
            canvas.drawCircle(width * 0.82f, height * 0.04f, 80f, paint)
            paint.color = 0x30CFD8DC
            canvas.drawCircle(width * 0.82f, height * 0.04f, 45f, paint)
            paint.color = 0xDDECEFF1.toInt()
            canvas.drawCircle(width * 0.82f, height * 0.04f, 22f, paint)
            // Moon crater shadow
            paint.color = 0x22455A64
            canvas.drawCircle(width * 0.82f - 5f, height * 0.04f - 3f, 6f, paint)
            canvas.drawCircle(width * 0.82f + 7f, height * 0.04f + 5f, 4f, paint)
        } else {
            // Sun glow with theme color
            val glowColor = currentTheme.sunGlowColor
            paint.color = (glowColor and 0x00FFFFFF) or 0x18000000
            canvas.drawCircle(width * 0.82f, height * 0.04f, 80f, paint)
            paint.color = (glowColor and 0x00FFFFFF) or 0x30000000
            canvas.drawCircle(width * 0.82f, height * 0.04f, 45f, paint)
            paint.color = currentTheme.sunColor
            canvas.drawCircle(width * 0.82f, height * 0.04f, 22f, paint)
        }

        // Fluffy clouds (dimmer at night)
        paint.color = if (night) 0x33FFFFFF else 0x66FFFFFF
        canvas.drawOval(width * 0.12f, height * 0.025f, width * 0.30f, height * 0.065f, paint)
        canvas.drawOval(width * 0.18f, height * 0.015f, width * 0.34f, height * 0.055f, paint)
        paint.color = if (night) 0x22FFFFFF else 0x44FFFFFF
        canvas.drawOval(width * 0.55f, height * 0.04f, width * 0.78f, height * 0.085f, paint)
        canvas.drawOval(width * 0.60f, height * 0.03f, width * 0.75f, height * 0.075f, paint)
        canvas.drawOval(width * 0.38f, height * 0.06f, width * 0.52f, height * 0.095f, paint)

        // === TERRAIN (theme-aware) ===
        val groundTop = height * 0.12f
        val terrTop = if (night) currentTheme.groundTopNight else currentTheme.groundTopDay
        val terrBot = if (night) currentTheme.groundBotNight else currentTheme.groundBotDay
        terrainPaint.shader = LinearGradient(
            0f, groundTop, 0f, height.toFloat(),
            terrTop, terrBot, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, groundTop, width.toFloat(), height.toFloat(), terrainPaint)
        terrainPaint.shader = null

        // Rolling hill contours for depth
        val hillRng = java.util.Random(55L)
        repeat(14) {
            val hx = hillRng.nextFloat() * width
            val hy = groundTop + 20f + hillRng.nextFloat() * (height - groundTop - 60f)
            val hw = 80f + hillRng.nextFloat() * 150f
            val hh = 25f + hillRng.nextFloat() * 40f
            terrainPaint.color = if (hillRng.nextBoolean()) currentTheme.hillColor1 else currentTheme.hillColor2
            terrainPaint.alpha = 30 + hillRng.nextInt(25)
            canvas.drawOval(hx - hw, hy - hh / 2, hx + hw, hy + hh / 2, terrainPaint)
        }
        terrainPaint.alpha = 255

        // Castle hill mound
        terrainPaint.color = currentTheme.castleMoundColor1
        terrainPaint.alpha = 80
        canvas.drawOval(
            engine.baseX - 130f, engine.baseY - 25f,
            engine.baseX + 130f, engine.baseY + 55f, terrainPaint
        )
        terrainPaint.color = currentTheme.castleMoundColor2
        terrainPaint.alpha = 50
        canvas.drawOval(
            engine.baseX - 95f, engine.baseY - 12f,
            engine.baseX + 95f, engine.baseY + 35f, terrainPaint
        )
        terrainPaint.alpha = 255

        // === DIRT PATHS WITH LAYERED DETAIL ===
        // Per-path tint offsets for visual distinction at overlaps
        val pathTints = intArrayOf(0, 15, -15, 25, -25, 10, -10, 20)
        for ((idx, p) in pathCache.withIndex()) {
            val tint = pathTints[idx % pathTints.size]
            pathPaint.style = Paint.Style.STROKE
            // Grass border around path
            pathPaint.color = currentTheme.pathBorderColor
            pathPaint.strokeWidth = 58f
            canvas.drawPath(p, pathPaint)
            // Dark dirt edge
            pathPaint.color = adjustBrightness(currentTheme.pathEdgeColor, tint)
            pathPaint.strokeWidth = 48f
            canvas.drawPath(p, pathPaint)
            // Main dirt fill
            pathPaint.color = adjustBrightness(currentTheme.pathColor, tint)
            pathPaint.strokeWidth = 36f
            canvas.drawPath(p, pathPaint)
            // Center lane lines — dashed pattern per path index
            val centerColor = currentTheme.pathCenterColor
            pathPaint.color = adjustBrightness(centerColor, tint)
            pathPaint.strokeWidth = 14f
            pathPaint.alpha = 70
            canvas.drawPath(p, pathPaint)
            pathPaint.alpha = 255
        }

        // === PATH WEAR — darken paths based on wave progress ===
        if (engine.wave > 1) {
            val wearAlpha = (engine.wave * 2).coerceAtMost(40)
            for (p in pathCache) {
                pathPaint.style = Paint.Style.STROKE
                pathPaint.color = 0x000000 or (wearAlpha shl 24)
                pathPaint.strokeWidth = 32f
                canvas.drawPath(p, pathPaint)
            }
            // Scuff marks on well-worn paths
            if (engine.wave >= 10) {
                val scuffRng = java.util.Random(engine.wave.toLong())
                for (gamePath in engine.paths) {
                    val wps = gamePath.waypoints
                    for (i in 0 until wps.size - 1) {
                        val ax = wps[i].x; val ay = wps[i].y
                        val bpx = wps[i + 1].x; val bpy = wps[i + 1].y
                        val scuffCount = (engine.wave / 5).coerceAtMost(8)
                        repeat(scuffCount) {
                            val t = scuffRng.nextFloat()
                            val sx = ax + t * (bpx - ax) + (scuffRng.nextFloat() - 0.5f) * 18f
                            val sy = ay + t * (bpy - ay) + (scuffRng.nextFloat() - 0.5f) * 18f
                            paint.color = 0x22000000
                            canvas.drawCircle(sx, sy, 3f + scuffRng.nextFloat() * 4f, paint)
                        }
                    }
                }
            }
        }

        // Textured cobblestones and flagstones along paths
        val pebbleRng = java.util.Random(42L)
        for (gamePath in engine.paths) {
            val wps = gamePath.waypoints
            for (i in 0 until wps.size - 1) {
                val ax = wps[i].x; val ay = wps[i].y
                val bpx = wps[i + 1].x; val bpy = wps[i + 1].y
                val segLen = Math.sqrt(((bpx - ax) * (bpx - ax) + (bpy - ay) * (bpy - ay)).toDouble()).toFloat()
                val pebbleCount = (segLen / 20f).toInt()
                repeat(pebbleCount) {
                    val t = pebbleRng.nextFloat()
                    val px = ax + t * (bpx - ax) + (pebbleRng.nextFloat() - 0.5f) * 16f
                    val py = ay + t * (bpy - ay) + (pebbleRng.nextFloat() - 0.5f) * 16f
                    val pw = 3f + pebbleRng.nextFloat() * 3.5f
                    val ph = 2f + pebbleRng.nextFloat() * 2.5f
                    // Cobblestone shadow
                    terrainPaint.color = 0xFF212121.toInt()
                    terrainPaint.alpha = 80
                    canvas.drawRoundRect(px - pw + 1f, py - ph + 1f, px + pw + 1f, py + ph + 1f, 2f, 2f, terrainPaint)
                    // Cobblestone body
                    terrainPaint.color = if (pebbleRng.nextBoolean()) currentTheme.cobblestone1 else currentTheme.cobblestone2
                    terrainPaint.alpha = 190
                    canvas.drawRoundRect(px - pw, py - ph, px + pw, py + ph, 2f, 2f, terrainPaint)
                    // Highlight top edge
                    terrainPaint.color = currentTheme.cobblestoneHighlight
                    terrainPaint.alpha = 90
                    canvas.drawLine(px - pw + 1f, py - ph + 1f, px + pw - 1f, py - ph + 1f, terrainPaint)
                }
            }
        }
        terrainPaint.alpha = 255

        // === PATH HIGHLIGHTING (placement mode) ===
        if (placementMode != null) {
            // Red highlight: can't place towers on paths
            for (p in pathCache) {
                pathPaint.style = Paint.Style.STROKE
                pathPaint.color = 0x66FF1744.toInt()
                pathPaint.strokeWidth = 52f
                canvas.drawPath(p, pathPaint)
                pathPaint.color = 0x44FF0000.toInt()
                pathPaint.strokeWidth = 36f
                canvas.drawPath(p, pathPaint)
            }
        } else if (trapPlacementMode != null) {
            // Green highlight: can only place traps near paths
            for (p in pathCache) {
                pathPaint.style = Paint.Style.STROKE
                pathPaint.color = 0x6600E676.toInt()
                pathPaint.strokeWidth = 52f
                canvas.drawPath(p, pathPaint)
                pathPaint.color = 0x4400C853.toInt()
                pathPaint.strokeWidth = 36f
                canvas.drawPath(p, pathPaint)
            }
        }

        // === PATH DIRECTION ARROWS (between waves) ===
        if (!engine.waveInProgress && !engine.gameOver && !engine.campaignVictory && engine.wave > 0) {
            drawPathArrows(canvas)
        }

        // Grass blade tufts across the field
        val turfRng = java.util.Random(99L)
        repeat(90) {
            val tx = turfRng.nextFloat() * width
            val ty = groundTop + turfRng.nextFloat() * (height - groundTop)
            val ts = 3f + turfRng.nextFloat() * 6f
            terrainPaint.color = if (turfRng.nextBoolean()) grassLightColor else grassColor
            terrainPaint.alpha = 70 + turfRng.nextInt(60)
            canvas.drawLine(tx, ty, tx - ts * 0.5f, ty - ts, terrainPaint)
            canvas.drawLine(tx, ty, tx + ts * 0.3f, ty - ts * 0.8f, terrainPaint)
            if (turfRng.nextFloat() < 0.3f) {
                canvas.drawLine(tx, ty, tx + ts * 0.6f, ty - ts * 0.6f, terrainPaint)
            }
        }
        terrainPaint.alpha = 255

        // Distant treeline along horizon
        val treeSeed = java.util.Random(33L)
        val gtop = height * 0.12f
        repeat(30) {
            val tx = treeSeed.nextFloat() * width
            val ts = 6f + treeSeed.nextFloat() * 10f
            terrainPaint.color = currentTheme.treelineColor1
            terrainPaint.alpha = 35 + treeSeed.nextInt(25)
            canvas.drawCircle(tx, gtop + 10f, ts, terrainPaint)
            terrainPaint.color = currentTheme.treelineColor2
            canvas.drawCircle(tx - ts * 0.3f, gtop + 6f, ts * 0.7f, terrainPaint)
        }
        terrainPaint.alpha = 255

        // === CROSSROADS STONE PLAZA ===
        if (currentTheme.hasCrossroadsSquare) {
            val (cx, cy) = MapPathGenerator.getCrossroadsCenter(width.toFloat(), height.toFloat())
            paint.color = 0x88263238.toInt()
            canvas.drawCircle(cx, cy, 65f, paint)
            paint.color = 0xAA455A64.toInt()
            canvas.drawCircle(cx, cy, 52f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = 0x8890A4AE.toInt()
            canvas.drawCircle(cx, cy, 38f, paint)
            canvas.drawCircle(cx, cy, 18f, paint)
            paint.style = Paint.Style.FILL
            paint.alpha = 255
        }

        // Decorations (trees, rocks, bushes, flowers)
        drawDecorations(canvas)

        // === VOLCANO CRATER VISUAL ===
        if (engine.mapType == MapType.VOLCANO) {
            val vcx = engine.volcanoCenterX
            val vcy = engine.volcanoCenterY
            val vTime = System.currentTimeMillis() / 1000.0
            val erupting = engine.volcanoErupting
            // Volcano cone (dark rock)
            terrainPaint.color = 0xFF37474F.toInt()
            val conePath = Path()
            conePath.moveTo(vcx, vcy - 90f)
            conePath.lineTo(vcx - 110f, vcy + 60f)
            conePath.lineTo(vcx + 110f, vcy + 60f)
            conePath.close()
            canvas.drawPath(conePath, terrainPaint)
            // Darker top
            terrainPaint.color = 0xFF263238.toInt()
            val topPath = Path()
            topPath.moveTo(vcx, vcy - 90f)
            topPath.lineTo(vcx - 55f, vcy - 25f)
            topPath.lineTo(vcx + 55f, vcy - 25f)
            topPath.close()
            canvas.drawPath(topPath, terrainPaint)
            // Crater opening
            terrainPaint.color = 0xFF1A1A1A.toInt()
            canvas.drawOval(vcx - 30f, vcy - 75f, vcx + 30f, vcy - 55f, terrainPaint)
            // Lava glow in crater
            val gAlpha = if (erupting) 200 else (100 + (50 * Math.sin(vTime * 2.0)).toInt())
            paint.color = 0xFFFF3D00.toInt()
            paint.alpha = gAlpha.coerceIn(0, 255)
            canvas.drawOval(vcx - 22f, vcy - 72f, vcx + 22f, vcy - 58f, paint)
            paint.color = 0xFFFFAB00.toInt()
            paint.alpha = (gAlpha * 0.6f).toInt().coerceIn(0, 255)
            canvas.drawOval(vcx - 14f, vcy - 69f, vcx + 14f, vcy - 61f, paint)
            // Lava drips on sides
            for (i in 0..3) {
                val drx = vcx + (-35f + i * 22f)
                val dry = vcy - 50f + (15f * Math.sin(vTime + i * 1.5).toFloat())
                paint.color = 0xCCFF6D00.toInt()
                canvas.drawCircle(drx, dry, 4f + 2f * Math.sin(vTime * 1.5 + i).toFloat(), paint)
            }
            // Eruption visual: intense glow + red overlay
            if (erupting) {
                paint.color = 0x22FF1744.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.color = 0x44FF6D00.toInt()
                canvas.drawCircle(vcx, vcy - 65f, 120f, paint)
                paint.color = 0x33FF3D00.toInt()
                canvas.drawCircle(vcx, vcy - 65f, 180f, paint)
            }
            paint.alpha = 255
        }

        // === TRAP RENDERING ===
        for (trap in engine.traps) {
            if (trap.isSpent()) continue
            val tx = trap.x; val ty = trap.y; val ts = trap.size
            when (trap.type) {
                TrapType.SPIKE -> {
                    // Metal spikes on ground
                    terrainPaint.color = 0xFF757575.toInt()
                    canvas.drawCircle(tx, ty, ts * 0.7f, terrainPaint)
                    terrainPaint.color = 0xFFBDBDBD.toInt()
                    for (a in 0 until 6) {
                        val ang = a * 60.0 * Math.PI / 180.0
                        val sx = tx + (Math.cos(ang) * ts * 0.55f).toFloat()
                        val sy = ty + (Math.sin(ang) * ts * 0.55f).toFloat()
                        canvas.drawCircle(sx, sy, 2.5f, terrainPaint)
                    }
                    terrainPaint.color = 0xFF424242.toInt()
                    canvas.drawCircle(tx, ty, ts * 0.3f, terrainPaint)
                }
                TrapType.TAR -> {
                    // Brown tar puddle
                    terrainPaint.color = 0xFF3E2723.toInt()
                    canvas.drawOval(tx - ts * 0.8f, ty - ts * 0.5f, tx + ts * 0.8f, ty + ts * 0.5f, terrainPaint)
                    terrainPaint.color = 0xFF4E342E.toInt()
                    canvas.drawOval(tx - ts * 0.5f, ty - ts * 0.3f, tx + ts * 0.5f, ty + ts * 0.3f, terrainPaint)
                    // Tar bubbles
                    val tTime = System.currentTimeMillis() / 1000.0
                    terrainPaint.color = 0xFF5D4037.toInt()
                    for (b in 0..2){
                        val bx = tx + (-6f + b * 6f) + (Math.sin(tTime + b) * 2f).toFloat()
                        val by = ty + (-3f + b * 2f)
                        canvas.drawCircle(bx, by, 2f + Math.sin(tTime * 2 + b).toFloat(), terrainPaint)
                    }
                }
                TrapType.MINE -> {
                    // Circular mine device
                    terrainPaint.color = 0xFF616161.toInt()
                    canvas.drawCircle(tx, ty, ts * 0.6f, terrainPaint)
                    terrainPaint.color = 0xFF424242.toInt()
                    canvas.drawCircle(tx, ty, ts * 0.4f, terrainPaint)
                    // Red blinking light
                    val blink = (System.currentTimeMillis() / 500) % 2 == 0L
                    terrainPaint.color = if (blink) 0xFFFF1744.toInt() else 0xFF880000.toInt()
                    canvas.drawCircle(tx, ty, 3f, terrainPaint)
                }
            }
            // Uses indicator
            if (trap.type != TrapType.MINE) {
                paint.color = 0xDDFFFFFF.toInt()
                paint.textSize = 12f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("${trap.uses}", tx, ty + ts + 10f, paint)
                paint.textAlign = Paint.Align.CENTER
            }
        }

        // === FIRE PATCHES (Napalm Conflagration) ===
        for (fp in engine.activeFirePatches) {
            val progress = (fp.duration / 3.0f).coerceIn(0f, 1f)
            paint.color = ((0x55 * progress).toInt() shl 24) or 0x00FF3D00
            canvas.drawCircle(fp.x, fp.y, fp.radius, paint)
            paint.color = ((0x77 * progress).toInt() shl 24) or 0x00FFAB00
            canvas.drawCircle(fp.x, fp.y, fp.radius * 0.55f, paint)
        }

        // === SUPPLY DROP RENDERING ===
        for (drop in engine.supplyDrops) {
            val dx = drop.x; val dy = drop.y; val ds = drop.size
            // Pulsing glow
            val pulse = (System.currentTimeMillis() % 1000) / 1000f
            val glowAlpha = (80 + 40 * kotlin.math.sin(pulse * Math.PI * 2).toFloat()).toInt()
            paint.color = android.graphics.Color.argb(glowAlpha, 255, 215, 0)
            canvas.drawCircle(dx, dy, ds * 1.2f, paint)
            // Box
            paint.color = 0xFF795548.toInt()
            canvas.drawRect(dx - ds * 0.5f, dy - ds * 0.4f, dx + ds * 0.5f, dy + ds * 0.4f, paint)
            // Gold cross on box
            paint.color = 0xFFFFD700.toInt()
            canvas.drawRect(dx - ds * 0.08f, dy - ds * 0.3f, dx + ds * 0.08f, dy + ds * 0.3f, paint)
            canvas.drawRect(dx - ds * 0.3f, dy - ds * 0.08f, dx + ds * 0.3f, dy + ds * 0.08f, paint)
            // Amount text
            paint.color = 0xFFFFD700.toInt()
            paint.textSize = 14f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("+${drop.goldAmount}g", dx, dy - ds - 4f, paint)
            // Lifetime bar
            val lifeRatio = (drop.lifetime / 8f).coerceIn(0f, 1f)
            paint.color = 0xFF388E3C.toInt()
            canvas.drawRect(dx - ds * 0.5f, dy + ds * 0.5f, dx - ds * 0.5f + ds * lifeRatio, dy + ds * 0.5f + 3f, paint)
        }

        // Soft horizon transition
        val horizonColor = skyBot
        terrainPaint.shader = LinearGradient(
            0f, groundTop - 8f, 0f, groundTop + 25f,
            horizonColor, horizonColor and 0x00FFFFFF, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, groundTop - 8f, width.toFloat(), groundTop + 25f, terrainPaint)
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
        // Pre-compute synergy counts for visual glow
        val synergyCounts = mutableMapOf<Int, Int>()
        for (i in engine.towers.indices) {
            val t = engine.towers[i]
            var count = 0
            for (j in engine.towers.indices) {
                if (i != j && engine.towers[j].type == t.type && t.distanceTo(engine.towers[j].x, engine.towers[j].y) < t.range * 1.2f) {
                    count++
                }
            }
            synergyCounts[i] = count.coerceAtMost(3)
        }
        // Draw blockades
        for (blockade in engine.blockades) {
            if (blockade.isDead()) continue
            // Brown wooden blockade body
            paint.color = 0xFF8D6E63.toInt()
            canvas.drawRoundRect(
                blockade.x - blockade.size, blockade.y - blockade.size * 0.6f,
                blockade.x + blockade.size, blockade.y + blockade.size * 0.6f,
                6f, 6f, paint
            )
            // Cross-beam lines
            paint.color = 0xFF6D4C41.toInt()
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(blockade.x - blockade.size + 4f, blockade.y - blockade.size * 0.5f,
                blockade.x + blockade.size - 4f, blockade.y + blockade.size * 0.5f, paint)
            canvas.drawLine(blockade.x - blockade.size + 4f, blockade.y + blockade.size * 0.5f,
                blockade.x + blockade.size - 4f, blockade.y - blockade.size * 0.5f, paint)
            paint.style = Paint.Style.FILL
            // HP bar
            val hpRatio = blockade.hp / blockade.maxHp
            val barW = blockade.size * 1.6f
            val barY = blockade.y - blockade.size * 0.6f - 8f
            paint.color = 0xFF333333.toInt()
            canvas.drawRoundRect(blockade.x - barW / 2, barY - 4f, blockade.x + barW / 2, barY + 4f, 2f, 2f, paint)
            paint.color = if (hpRatio > 0.5f) 0xFF66BB6A.toInt() else if (hpRatio > 0.25f) 0xFFFFB300.toInt() else 0xFFFF5252.toInt()
            canvas.drawRoundRect(blockade.x - barW / 2, barY - 4f, blockade.x - barW / 2 + barW * hpRatio, barY + 4f, 2f, 2f, paint)
        }

        for ((idx, tower) in engine.towers.withIndex()) {
            val synergyCount = synergyCounts[idx] ?: 0
            // Synergy glow ring
            if (synergyCount > 0) {
                val synergyColor = when (tower.type) {
                    TowerType.ARROW -> 0x4CAF50
                    TowerType.MAGIC -> 0xAB47BC
                    TowerType.CANNON -> 0xFF7043
                    TowerType.POISON -> 0x66BB6A
                    TowerType.TESLA -> 0x29B6F6
                    TowerType.ICE -> 0x81D4FA
                    TowerType.FLAME -> 0xFF5722
                    TowerType.NECRO -> 0x9C27B0
                    TowerType.BALLISTA -> 0xFFD54F
                    TowerType.VORTEX -> 0x7E57C2
                    TowerType.HEALER -> 0x66BB6A
                }
                val glowAlpha = (0x15 * synergyCount).coerceAtMost(0x44)
                paint.color = (glowAlpha shl 24) or synergyColor
                canvas.drawCircle(tower.x, tower.y, tower.size + 12f + synergyCount * 3f, paint)
            }
            if (tower == selectedTower) {
                // Smooth sinusoidal pulse for selection ring
                val nowTime = System.currentTimeMillis()
                val pulse = (Math.sin(nowTime / 180.0) * 0.15f + 0.85f).toFloat()

                // Outer range fill and glowing stroke
                paint.color = 0x2000E5FF
                canvas.drawCircle(tower.x, tower.y, tower.range, paint)
                paint.color = 0x8800E5FF.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f
                canvas.drawCircle(tower.x, tower.y, tower.range, paint)

                // Pulsing highlight ring right around tower base
                paint.color = 0x5500E5FF
                canvas.drawCircle(tower.x, tower.y, (tower.size + 10f) * pulse, paint)
                paint.color = 0xEEFFD700.toInt() // gold border on selected unit
                paint.strokeWidth = 3f
                canvas.drawCircle(tower.x, tower.y, (tower.size + 8f) * pulse, paint)
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 1f
            }
            val tScale = depthFactor(tower.y)
            // Recoil animation: quick scale bounce on fire
            val recoilScale = if (tower.recoilTimer > 0) {
                val t = (tower.recoilTimer / 0.15f).coerceIn(0f, 1f)
                1f + 0.12f * t  // expand 12% then shrink back
            } else 1f
            canvas.save()
            canvas.scale(tScale * recoilScale, tScale * recoilScale, tower.x, tower.y)
            EntityRenderer.drawTower(canvas, tower, tower == selectedTower)
            canvas.restore()
            // Muzzle flash on fire
            if (tower.recoilTimer > 0.08f) {
                paint.color = 0x55FFFFFF
                canvas.drawCircle(tower.x, tower.y - tower.size * 0.4f, tower.size * 0.5f, paint)
            }
            // Thorns jam indicator — red flash on jammed towers
            if (tower.thornJamTimer > 0) {
                paint.color = 0x44FF5252
                canvas.drawCircle(tower.x, tower.y, tower.size + 6f, paint)
            }
            // Tower stun effect — dizzy orbiting stars
            if (tower.stunTimer > 0f) {
                val stunAngle = (System.currentTimeMillis() * 0.006) % (Math.PI * 2)
                val starDist = tower.size * 0.65f
                paint.color = 0xFFFFD700.toInt()
                paint.style = Paint.Style.FILL
                for (i in 0 until 3) {
                    val a = stunAngle + i * (Math.PI * 2.0 / 3.0)
                    val sx = tower.x + (Math.cos(a) * starDist).toFloat()
                    val sy = tower.y - tower.size * 0.7f + (Math.sin(a) * (starDist * 0.4f)).toFloat()
                    canvas.drawCircle(sx, sy, 3.5f, paint)
                }
                lvlPaint.textSize = 10f
                lvlPaint.color = 0xFFFFD54F.toInt()
                canvas.drawText("💫 STUNNED", tower.x, tower.y - tower.size - 4f, lvlPaint)
            }
            // Ice tower aura ring
            if (tower.type == TowerType.ICE) {
                paint.color = 0x2281D4FA
                canvas.drawCircle(tower.x, tower.y, tower.range, paint)
                paint.color = 0x5581D4FA
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(tower.x, tower.y, tower.range, paint)
                paint.style = Paint.Style.FILL
            }
            if (tower.level > 1) {
                // Level badge with background
                val lvText = "Lv${tower.level}"
                val ly = tower.y - 25f * tScale
                paint.color = 0xCC000000.toInt()
                canvas.drawRoundRect(tower.x - 16f, ly - 14f, tower.x + 16f, ly + 4f, 4f, 4f, paint)
                canvas.drawText(lvText, tower.x, ly, lvlPaint)
            }
            // Targeting mode label on selected tower
            if (tower == selectedTower && tower.targetingMode != TargetingMode.CLOSE) {
                val modeText = "\uD83C\uDFAF ${tower.targetingMode.label}"
                val my = tower.y + tower.size + 18f
                paint.color = 0xCC000000.toInt()
                canvas.drawRoundRect(tower.x - 30f, my - 12f, tower.x + 30f, my + 6f, 4f, 4f, paint)
                canvas.drawText(modeText, tower.x, my + 2f, lvlPaint)
            }
            // Mastery star badge
            val mLvl = engine.masteryLevel(tower.type)
            if (mLvl > 0) {
                val starColor = when (mLvl) {
                    1 -> 0xFFCD7F32.toInt()  // bronze
                    2 -> 0xFFC0C0C0.toInt()  // silver
                    3 -> 0xFFFFD700.toInt()  // gold
                    4 -> 0xFF00BCD4.toInt()  // diamond
                    else -> 0xFFE040FB.toInt() // master purple
                }
                val sy = tower.y + tower.size + 4f
                paint.color = starColor
                canvas.drawText("★".repeat(mLvl.coerceAtMost(3)), tower.x, sy, lvlPaint)
            }
        }

        // Enemies (with perspective scale — further up = slightly smaller)
        for (enemy in engine.enemies) {
            val depthScale = depthFactor(enemy.y)
            // Death animation: shrink + fade
            val deathAlpha: Int
            val deathScale: Float
            if (enemy.deathProcessed && enemy.deathAnimTimer > 0f) {
                val t = (enemy.deathAnimTimer / 0.4f).coerceIn(0f, 1f)
                deathScale = t * 0.7f + 0.3f  // shrink from 100% to 30%
                deathAlpha = (255 * t).toInt()
            } else {
                deathScale = 1f
                deathAlpha = 255
            }
            // Skip drawing if fully faded
            if (deathAlpha <= 0) continue

            // Boss Attack Telegraph Danger Zone (drawn underneath boss)
            if (enemy.isTelegraphing) {
                val radius = enemy.telegraphRadius
                val progress = (1f - (enemy.telegraphTimer / enemy.telegraphDuration).coerceIn(0f, 1f))
                val pulse = ((Math.sin(System.currentTimeMillis() * 0.012) + 1.0) / 2.0).toFloat()

                // Hazard zone translucent fill
                paint.color = 0x22FF1744.toInt()
                paint.style = Paint.Style.FILL
                canvas.drawCircle(enemy.x, enemy.y, radius, paint)

                // Charging radial sweep / inner expanding circle
                paint.color = 0x33FF5252.toInt()
                canvas.drawCircle(enemy.x, enemy.y, radius * progress, paint)

                // Danger ring border with pulse
                paint.color = 0xFFFF1744.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f + pulse * 2f
                canvas.drawCircle(enemy.x, enemy.y, radius, paint)
                paint.style = Paint.Style.FILL
            }

            canvas.save()
            canvas.scale(depthScale * deathScale, depthScale * deathScale, enemy.x, enemy.y)
            if (deathAlpha < 255) {
                val layerPaint = Paint().apply { alpha = deathAlpha }
                canvas.saveLayerAlpha(enemy.x - enemy.size * 2, enemy.y - enemy.size * 2,
                    enemy.x + enemy.size * 2, enemy.y + enemy.size * 2, deathAlpha)
            }
            EntityRenderer.drawEnemy(canvas, enemy, engine.freezeTimer > 0)
            if (deathAlpha < 255) canvas.restore()
            canvas.restore()

            // Skip decorations for dying enemies
            if (enemy.deathProcessed) continue

            // Elite crown above enemy
            if (enemy.isElite) {
                val crownY = enemy.y - enemy.size - 18f
                paint.color = 0xFFFFD700.toInt()
                val cp = Path()
                cp.moveTo(enemy.x - 8f, crownY + 6f)
                cp.lineTo(enemy.x - 10f, crownY - 4f)
                cp.lineTo(enemy.x - 4f, crownY)
                cp.lineTo(enemy.x, crownY - 6f)
                cp.lineTo(enemy.x + 4f, crownY)
                cp.lineTo(enemy.x + 10f, crownY - 4f)
                cp.lineTo(enemy.x + 8f, crownY + 6f)
                cp.close()
                canvas.drawPath(cp, paint)
                // Elite glow ring (ability-colored)
                val glowColor = if (enemy.eliteAbility != com.example.myapp.game.EliteAbility.NONE)
                    (enemy.eliteAbility.color and 0x00FFFFFF) or 0x33000000 else 0x33FFD700
                paint.color = glowColor
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 6f, paint)
                // Elite ability label below enemy
                if (enemy.eliteAbility != com.example.myapp.game.EliteAbility.NONE) {
                    lvlPaint.color = enemy.eliteAbility.color
                    canvas.drawText(enemy.eliteAbility.label, enemy.x, enemy.y + enemy.size + 16f, lvlPaint)
                }
            }

            // Elemental status indicators (zero GC allocation, reusing paint)
            if (enemy.burnTimer > 0) {
                paint.color = 0x44FF5722
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 3f, paint)
            }
            if (enemy.poisonTimer > 0) {
                paint.color = 0x4476FF03
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 4f, paint)
            }
            if (enemy.shockTimer > 0) {
                paint.color = 0x4429B6F6
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 5f, paint)
            }
            if (enemy.superconductTimer > 0) {
                paint.color = 0x5500E5FF
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 6f, paint)
            }
            if (enemy.arcaneMarkTimer > 0) {
                paint.color = 0x44AB47BC
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 4f, paint)
            }
            if (enemy.stunTimer > 0) {
                paint.color = 0x55FFEB3B
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 5f, paint)
            }
            if (enemy.solarBurnTimer > 0) {
                paint.color = 0x55FFD700
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 5f, paint)
            }
            if (enemy.soulburnTimer > 0) {
                paint.color = 0x559C27B0
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 6f, paint)
            }
            if (enemy.conduitTimer > 0) {
                paint.color = 0x55E040FB
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 5f, paint)
            }
            if (enemy.silenceTimer > 0) {
                paint.color = 0x5500E5FF
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 4f, paint)
            }
            if (enemy.brittleTimer > 0) {
                paint.color = 0x5580DEEA
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 4f, paint)
            }
            if (enemy.enfeebleTimer > 0) {
                paint.color = 0x557C4DFF
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 5f, paint)
            }
            if (enemy.astralDecayTimer > 0) {
                paint.color = 0x55BA68C8
                canvas.drawCircle(enemy.x, enemy.y, enemy.size + 6f, paint)
            }

            // HP bar — rounded with color gradient
            val ehpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
            val eBarW = enemy.size * 2.2f
            val eBarH = 6f
            val eBarX = enemy.x - eBarW / 2
            val eBarY = enemy.y - enemy.size - 12f
            // Background
            paint.color = 0x88000000.toInt()
            canvas.drawRoundRect(eBarX - 1f, eBarY - 1f, eBarX + eBarW + 1f, eBarY + eBarH + 1f, 3f, 3f, paint)
            canvas.drawRoundRect(eBarX, eBarY, eBarX + eBarW, eBarY + eBarH, 3f, 3f, hpBarBgPaint)
            // Color based on health
            val barColor = when {
                ehpRatio > 0.6f -> 0xFF4CAF50.toInt()
                ehpRatio > 0.3f -> 0xFFFF9800.toInt()
                else -> 0xFFF44336.toInt()
            }
            paint.color = barColor
            canvas.drawRoundRect(eBarX, eBarY, eBarX + eBarW * ehpRatio, eBarY + eBarH, 3f, 3f, paint)

            // Boss: large HP bar at top of screen with name and ability indicator
            if (enemy.type == EnemyType.BOSS) {
                val bossBarW = width * 0.75f
                val bossBarH = 18f
                val bossBarX = (width - bossBarW) / 2
                val bossBarY = 105f
                // Background panel
                paint.color = 0xCC1B2838.toInt()
                canvas.drawRoundRect(bossBarX - 8f, bossBarY - 30f, bossBarX + bossBarW + 8f, bossBarY + bossBarH + 22f, 10f, 10f, paint)
                // Bar background
                canvas.drawRoundRect(bossBarX, bossBarY, bossBarX + bossBarW, bossBarY + bossBarH, 4f, 4f, hpBarBgPaint)
                // Bar fill
                val bossColor = enemy.bossType?.color ?: 0xFFE91E63.toInt()
                bossHpPaint.color = bossColor
                canvas.drawRoundRect(bossBarX, bossBarY, bossBarX + bossBarW * ehpRatio, bossBarY + bossBarH, 4f, 4f, bossHpPaint)
                // Border
                paint.color = bossColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRoundRect(bossBarX, bossBarY, bossBarX + bossBarW, bossBarY + bossBarH, 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Phase divider notches (25%, 50%, 75%)
                paint.color = 0x88FFFFFF.toInt()
                paint.strokeWidth = 2f
                val notch25 = bossBarX + bossBarW * 0.25f
                val notch50 = bossBarX + bossBarW * 0.50f
                val notch75 = bossBarX + bossBarW * 0.75f
                canvas.drawLine(notch25, bossBarY, notch25, bossBarY + bossBarH, paint)
                canvas.drawLine(notch50, bossBarY, notch50, bossBarY + bossBarH, paint)
                canvas.drawLine(notch75, bossBarY, notch75, bossBarY + bossBarH, paint)

                // Boss name
                val bossName = enemy.bossType?.let { "${it.emoji} ${it.displayName}" } ?: "\u2620\uFE0F BOSS"
                bossNamePaint.textSize = 22f; bossNamePaint.color = Color.WHITE
                drawOutlinedText(canvas, bossName, width / 2f, bossBarY - 10f, bossNamePaint)
                // HP text on bar
                val hpText = "${enemy.hp.toInt()} / ${enemy.maxHp.toInt()}"
                bossNamePaint.textSize = 13f; bossNamePaint.color = Color.WHITE
                canvas.drawText(hpText, width / 2f, bossBarY + bossBarH - 3f, bossNamePaint)

                // Ability / Telegraph indicator
                if (enemy.isTelegraphing) {
                    val pulse = ((Math.sin(System.currentTimeMillis() * 0.015) + 1.0) / 2.0).toFloat()
                    val chargeAlpha = (180 + (pulse * 75)).toInt()
                    val chargeRatio = (1f - (enemy.telegraphTimer / enemy.telegraphDuration).coerceIn(0f, 1f))
                    // Telegraph charge progress line
                    paint.color = 0xFFFF1744.toInt()
                    paint.alpha = chargeAlpha
                    canvas.drawRoundRect(bossBarX, bossBarY + bossBarH + 2f, bossBarX + bossBarW * chargeRatio, bossBarY + bossBarH + 6f, 2f, 2f, paint)
                    paint.alpha = 255

                    val abName = enemy.pendingAbility?.name?.replace('_', ' ') ?: "SPECIAL"
                    bossNamePaint.textSize = 12f
                    bossNamePaint.color = 0xFFFF5252.toInt()
                    canvas.drawText(GameStrings.bossChargingFmt(abName, enemy.telegraphTimer), width / 2f, bossBarY + bossBarH + 18f, bossNamePaint)
                } else {
                    val abilityLabel = enemy.bossType?.ability?.name?.replace('_', ' ') ?: ""
                    if (abilityLabel.isNotEmpty()) {
                        val phaseLabel = "Phase ${enemy.currentPhase}/4"
                        bossNamePaint.textSize = 12f; bossNamePaint.color = 0xFFBDBDBD.toInt()
                        canvas.drawText("$phaseLabel  •  \u26A0\uFE0F $abilityLabel", width / 2f, bossBarY + bossBarH + 15f, bossNamePaint)
                    }
                }
            }
        }

        // Projectiles with trail glow
        for (proj in engine.projectiles) {
            // Trail glow
            projPaint.color = proj.color
            projPaint.alpha = 60
            canvas.drawCircle(proj.x, proj.y, proj.size * 2.5f, projPaint)
            projPaint.alpha = 255
            canvas.drawCircle(proj.x, proj.y, proj.size, projPaint)
            // Bright center
            projPaint.color = 0xCCFFFFFF.toInt()
            canvas.drawCircle(proj.x, proj.y, proj.size * 0.4f, projPaint)
        }

        // Particles
        for (p in engine.particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (255 * (p.life / p.maxLife).coerceIn(0f, 1f)).toInt()
            canvas.drawCircle(p.x, p.y, p.size * (p.life / p.maxLife).coerceIn(0.3f, 1f), particlePaint)
        }

        // Shockwaves (zero GC allocations)
        for (sw in shockwavePool) {
            if (sw.active) {
                val prog = 1f - (sw.life / sw.maxLife).coerceIn(0f, 1f)
                val r = sw.currentRadius + (sw.maxRadius - sw.currentRadius) * prog
                val alpha = ((1f - prog * prog) * 230).toInt().coerceIn(0, 255)
                shockwavePaint.color = sw.color
                shockwavePaint.alpha = alpha
                shockwavePaint.strokeWidth = sw.strokeWidth * (1f - prog * 0.4f)
                canvas.drawCircle(sw.x, sw.y, r, shockwavePaint)
            }
        }

        // Player (with perspective)
        val phRatioForDraw = engine.player.hp / engine.player.maxHp
        val pScale = depthFactor(engine.player.y)
        canvas.save()
        canvas.scale(pScale, pScale, engine.player.x, engine.player.y)
        EntityRenderer.drawPlayer(canvas, engine.player.x, engine.player.y, engine.player.size, phRatioForDraw)
        canvas.restore()
        // Player range circle — subtle
        paint.color = 0x1542A5F5
        canvas.drawCircle(engine.player.x, engine.player.y, engine.player.attackRange, paint)
        paint.color = 0x3342A5F5
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawCircle(engine.player.x, engine.player.y, engine.player.attackRange, paint)
        paint.style = Paint.Style.FILL

        // Player HP bar removed — focus on base health only

        // Floating texts (with outline for readability)
        for (ft in engine.floatingTexts) {
            val alpha = (255 * (ft.life / ft.maxLife).coerceIn(0f, 1f)).toInt()
            floatPaint.color = ft.color
            floatPaint.textSize = ft.size
            floatPaint.alpha = alpha
            textOutlinePaint.textSize = ft.size
            textOutlinePaint.textAlign = floatPaint.textAlign
            textOutlinePaint.typeface = floatPaint.typeface
            textOutlinePaint.alpha = alpha
            canvas.drawText(ft.text, ft.x, ft.y, textOutlinePaint)
            canvas.drawText(ft.text, ft.x, ft.y, floatPaint)
        }

        // Wave info at top (with outline for readability)
        val endlessTag = if (engine.isEndlessMode) "\u267E\uFE0F " else ""
        val rushTag = if (engine.isBossRush) "\uD83D\uDC80 " else ""
        val randTag = if (engine.isRandomizerMode) "\uD83C\uDFB2 " else ""
        if (!engine.waveInProgress && engine.wave > 0 && !engine.gameOver) {
            val countdown = engine.waveTimer.toInt() + 1
            val wText = "${rushTag}${endlessTag}${randTag}Next wave in ${countdown}s"
            drawOutlinedText(canvas, wText, width / 2f, 80f, waveTextPaint)
            if (engine.isBossRush) {
                drawOutlinedText(canvas, "Boss ${engine.bossRushWave} defeated", width / 2f, 50f, previewTextPaint)
            }
            // Between-wave gameplay tip
            val tips = arrayOf(
                "💡 Same-type towers near each other get synergy bonuses!",
                "💡 Save gold between waves — you earn 5% interest!",
                "💡 Match tower damage types to enemy weaknesses.",
                "💡 Use Ice towers to slow enemies for other towers.",
                "💡 Upgrade towers for +40% damage, +15 range per level.",
                "💡 Use the left virtual joystick to steer your hero smoothly!",
                "💡 Sell unused towers to recover 60% of their cost.",
                "💡 Night waves give enemies +20% HP — prepare extra!",
                "💡 Boss waves arrive every 5th wave — save abilities!",
                "💡 Tap a tower to change targeting: Close/First/Last/Strong."
            )
            val tipIdx = engine.wave % tips.size
            previewTextPaint.color = 0xFFB0BEC5.toInt()
            val savedAlign = previewTextPaint.textAlign
            previewTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(tips[tipIdx], width / 2f, height - 40f, previewTextPaint)
            previewTextPaint.textAlign = savedAlign
        } else if (engine.waveInProgress) {
            val modTag = if (engine.currentWaveModifier != WaveModifier.NONE) " ${engine.currentWaveModifier.emoji}" else ""
            val bossName = if (engine.isBossRush && engine.currentBoss != null) " \u2014 ${engine.currentBoss!!.displayName}" else ""
            val wText = "${rushTag}${endlessTag}${randTag}Wave ${engine.wave}$modTag$bossName  \u2694\uFE0F  ${engine.enemies.size} enemies"
            drawOutlinedText(canvas, wText, width / 2f, 80f, waveTextPaint)

            // Wave progress bar — thin bar under wave text
            if (engine.totalEnemiesThisWave > 0) {
                val progW = width * 0.5f
                val progH = 6f
                val progX = (width - progW) / 2f
                val progY = 90f
                val progress = (engine.enemiesSpawnedThisWave.toFloat() / engine.totalEnemiesThisWave).coerceIn(0f, 1f)
                paint.color = 0x66000000
                canvas.drawRoundRect(progX, progY, progX + progW, progY + progH, 3f, 3f, paint)
                paint.color = 0xFF4CAF50.toInt()
                canvas.drawRoundRect(progX, progY, progX + progW * progress, progY + progH, 3f, 3f, paint)
            }

            // Wave modifier indicator
            if (engine.currentWaveModifier != WaveModifier.NONE) {
                val mod = engine.currentWaveModifier
                paint.color = 0xCC1B2838.toInt()
                val modW = 200f
                canvas.drawRoundRect(width / 2f - modW / 2f, 97f, width / 2f + modW / 2f, 118f, 6f, 6f, paint)
                previewTextPaint.color = 0xFFFFAB00.toInt()
                val savedAlign = previewTextPaint.textAlign
                previewTextPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("${mod.emoji} ${mod.displayName}", width / 2f, 114f, previewTextPaint)
                previewTextPaint.textAlign = savedAlign
            }

            // Weather indicator in top bar
            if (engine.currentWeather != WeatherEvent.CLEAR) {
                val we = engine.currentWeather
                paint.color = 0xCC111827.toInt()
                val weW = 220f
                val weY = if (engine.currentWaveModifier != WaveModifier.NONE) 122f else 97f
                canvas.drawRoundRect(width / 2f - weW / 2f, weY, width / 2f + weW / 2f, weY + 21f, 6f, 6f, paint)
                val wColor = when (we) {
                    WeatherEvent.BLOOD_MOON -> 0xFFFF5252.toInt()
                    WeatherEvent.THUNDERSTORM -> 0xFF80D8FF.toInt()
                    WeatherEvent.SOLAR_ECLIPSE -> 0xFFFFE57F.toInt()
                    else -> Color.WHITE
                }
                previewTextPaint.color = wColor
                val savedAlign = previewTextPaint.textAlign
                previewTextPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("${we.emoji} ${GameStrings.weatherName(we)}", width / 2f, weY + 17f, previewTextPaint)
                previewTextPaint.textAlign = savedAlign
            }
        }

        // Wave banner overlay
        if (engine.showWaveBanner) {
            canvas.drawRect(0f, height * 0.28f, width.toFloat(), height * 0.68f, bannerBgPaint)
            val isBoss = engine.wave % engine.bossInterval == 0
            val bossLabel = engine.currentBoss?.let { "${it.emoji} ${it.displayName}" }
            val waveLabel = if (isBoss && bossLabel != null) "$bossLabel" else "\u2694\uFE0F WAVE ${engine.wave} \u2694\uFE0F"
            drawOutlinedText(canvas, waveLabel, width / 2f, height * 0.40f, waveBannerPaint)
            // Enemy count line
            val preview = engine.nextWavePreview
            val totalEnemies = engine.totalEnemiesThisWave
            if (isBoss) {
                drawOutlinedText(canvas, "⚠️ Boss Wave", width / 2f, height * 0.47f, waveBannerSubPaint)
            }
            if (totalEnemies > 0) {
                drawOutlinedText(canvas, "$totalEnemies enemies incoming", width / 2f, height * 0.54f, waveBannerSubPaint)
            }
            // Enemy type breakdown
            if (preview != null && preview.enemies.isNotEmpty()) {
                val typeLinePaint = Paint().apply {
                    color = 0xFFCCCCCC.toInt(); textSize = 36f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT
                }
                val line = preview.enemies.entries.joinToString("  ") { (type, count) -> "${type.emoji}×$count" }
                drawOutlinedText(canvas, line, width / 2f, height * 0.61f, typeLinePaint)
            }
        }

        // Weather banner overlay
        if (engine.showWeatherBanner && engine.currentWeather != WeatherEvent.CLEAR) {
            val event = engine.currentWeather
            val bannerNotice = GameStrings.weatherBanner(event)
            val bannerW = (width * 0.90f).coerceAtMost(720f)
            val bannerH = 100f
            val bannerLeft = (width - bannerW) / 2f
            val bannerTop = height * 0.23f

            val bannerCardPaint = Paint().apply {
                color = 0xEE0F172A.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val bannerBorderColor = when (event) {
                WeatherEvent.BLOOD_MOON -> 0xFFFF1744.toInt()
                WeatherEvent.THUNDERSTORM -> 0xFF00E5FF.toInt()
                WeatherEvent.SOLAR_ECLIPSE -> 0xFFFFD700.toInt()
                else -> 0xFF9E9E9E.toInt()
            }
            val bannerBorderPaint = Paint().apply {
                color = bannerBorderColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            val bannerTitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val bannerDescPaint = Paint().apply {
                color = 0xFFECEFF1.toInt()
                textSize = 17f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val rect = RectF(bannerLeft, bannerTop, bannerLeft + bannerW, bannerTop + bannerH)
            canvas.drawRoundRect(rect, 16f, 16f, bannerCardPaint)
            canvas.drawRoundRect(rect, 16f, 16f, bannerBorderPaint)
            canvas.drawText("${event.emoji} ${GameStrings.weatherName(event).uppercase()}", width / 2f, bannerTop + 38f, bannerTitlePaint)
            canvas.drawText(bannerNotice, width / 2f, bannerTop + 76f, bannerDescPaint)
        }

        // Combo display (top right)
        if (engine.comboCount >= 3) {
            comboPaint.textSize = 28f + engine.comboCount.coerceAtMost(20) * 1.5f
            drawOutlinedText(canvas, "${engine.comboCount}x COMBO", width - 20f, 170f, comboPaint)
            comboMultPaint.textSize = 22f
            drawOutlinedText(canvas, "x${String.format("%.1f", engine.comboMultiplier)} gold", width - 20f, 198f, comboMultPaint)
        }

        // High score (top left-ish)
        if (engine.highScore > 0) {
            drawOutlinedText(canvas, "\u2B50 Best: ${engine.highScore}", 10f, 130f, hsLeftPaint)
        }

        // Diamond counter (below high score)
        drawOutlinedText(canvas, "\uD83D\uDC8E ${engine.skillTree.diamonds}", 10f, 155f, diamondPaint)

        // Day/Night indicator
        if (engine.isNight) {
            drawOutlinedText(canvas, "\uD83C\uDF19 Night — enemies +20% HP", 10f, 178f, previewLabelPaint)
        }

        // Wave preview panel — ENHANCED DANGER PREVIEW
        val preview = engine.nextWavePreview
        if (preview != null && !engine.waveInProgress && !engine.gameOver) {
            val pvX = 10f
            val pvY = height * 0.62f
            val pvW = width * 0.82f

            // --- Threat analysis ---
            val playerTowerTypes = engine.towers.map { it.type }.toSet()
            val enemyTypes = preview.enemies.keys
            val warnings = mutableListOf<Pair<String, Int>>()
            when (preview.modifier) {
                WaveModifier.FAST, WaveModifier.BOSS_RALLY ->
                    if (TowerType.ICE !in playerTowerTypes && TowerType.VORTEX !in playerTowerTypes)
                        warnings.add("⚠️ Fast enemies — no slow towers!" to 0xFFFF3D3D.toInt())
                WaveModifier.ARMORED ->
                    if (TowerType.CANNON !in playerTowerTypes && TowerType.BALLISTA !in playerTowerTypes)
                        warnings.add("⚠️ Armored — no armor-piercing!" to 0xFFFF9100.toInt())
                WaveModifier.SWARM ->
                    if (TowerType.CANNON !in playerTowerTypes && TowerType.TESLA !in playerTowerTypes && TowerType.FLAME !in playerTowerTypes)
                        warnings.add("⚠️ Swarm — no AoE damage!" to 0xFFFF9100.toInt())
                WaveModifier.REGEN ->
                    if (TowerType.POISON !in playerTowerTypes && TowerType.FLAME !in playerTowerTypes)
                        warnings.add("⚠️ Regen enemies — DoT towers needed!" to 0xFFFFAB00.toInt())
                else -> {}
            }
            if (EnemyType.ARMORED_GOLEM in enemyTypes &&
                TowerType.CANNON !in playerTowerTypes && TowerType.BALLISTA !in playerTowerTypes &&
                warnings.none { "armor" in it.first.lowercase() })
                warnings.add("⚠️ Armored Golem — needs heavy firepower!" to 0xFFFF9100.toInt())
            if ((EnemyType.FAST_SKELETON in enemyTypes || EnemyType.BAT in enemyTypes) &&
                TowerType.ICE !in playerTowerTypes && TowerType.VORTEX !in playerTowerTypes &&
                warnings.none { "fast" in it.first.lowercase() || "slow" in it.first.lowercase() })
                warnings.add("⚠️ Fast units — no slow towers!" to 0xFFFF3D3D.toInt())
            if (EnemyType.NECROMANCER in enemyTypes &&
                warnings.none { "necromancer" in it.first.lowercase() })
                warnings.add("⚠️ Necromancer — target with Snipers!" to 0xFFBA68C8.toInt())
            if (EnemyType.GHOST in enemyTypes &&
                TowerType.MAGIC !in playerTowerTypes && TowerType.TESLA !in playerTowerTypes &&
                warnings.none { "ghost" in it.first.lowercase() })
                warnings.add("⚠️ Ghosts — physical resist, use Magic/Tesla!" to 0xFF80DEEA.toInt())
            if (EnemyType.TREANT in enemyTypes &&
                TowerType.FLAME !in playerTowerTypes &&
                warnings.none { "treant" in it.first.lowercase() })
                warnings.add("⚠️ Treants — use Flame to stop regen!" to 0xFF81C784.toInt())
            if (EnemyType.MAGMA_CRAB in enemyTypes &&
                TowerType.ICE !in playerTowerTypes &&
                warnings.none { "magma" in it.first.lowercase() })
                warnings.add("⚠️ Magma Crabs — use Ice to shatter shell!" to 0xFFFF3D00.toInt())
            val hpRatio = engine.baseHp / engine.maxBaseHp
            if (hpRatio < 0.35f)
                warnings.add(0, "🔴 Base CRITICAL — defend now!" to 0xFFFF1744.toInt())
            else if (hpRatio < 0.55f)
                warnings.add(0, "🟡 Base damaged — repair if possible" to 0xFFFFAB00.toInt())
            if (preview.isBoss)
                warnings.add(0, "💀 BOSS incoming — focus fire!" to 0xFFE91E63.toInt())

            val borderColor = when {
                warnings.any { it.second == 0xFFFF1744.toInt() || it.second == 0xFFFF3D3D.toInt() } -> 0xFFFF3D3D.toInt()
                warnings.any { it.second == 0xFFE91E63.toInt() } -> 0xFFE91E63.toInt()
                warnings.isNotEmpty() -> 0xFFFF9100.toInt()
                else -> 0xFF4CAF50.toInt()
            }
            val hasModifier = preview.modifier != WaveModifier.NONE
            val modExtra = if (hasModifier) 36f else 0f
            val warnExtra = warnings.size * 28f
            val contentLines = if (preview.isBoss) 2 else preview.enemies.size
            val pvH = (84f + modExtra + contentLines * 28f + warnExtra).coerceAtMost(300f).coerceAtLeast(100f)

            // Panel background
            canvas.drawRoundRect(pvX, pvY, pvX + pvW, pvY + pvH, 14f, 14f, previewBgPaint)
            // Colored left-border threat stripe
            paint.color = borderColor; paint.style = Paint.Style.FILL
            canvas.drawRoundRect(pvX, pvY, pvX + 7f, pvY + pvH, 14f, 14f, paint)
            // Outer border
            paint.color = borderColor; paint.style = Paint.Style.STROKE; paint.strokeWidth = 2f
            canvas.drawRoundRect(pvX, pvY, pvX + pvW, pvY + pvH, 14f, 14f, paint)
            paint.style = Paint.Style.FILL; paint.strokeWidth = 1f

            // Header
            val headerY = pvY + 26f
            previewLabelPaint.color = 0xFFFFD54F.toInt(); previewLabelPaint.textSize = 20f
            canvas.drawText("⚔️ Wave ${engine.wave + 1} Preview", pvX + 16f, headerY, previewLabelPaint)
            if (!preview.isBoss) {
                val totalCount = preview.enemies.values.sum()
                previewLabelPaint.color = 0xFFBDBDBD.toInt(); previewLabelPaint.textSize = 15f
                previewLabelPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("$totalCount enemies", pvX + pvW - 14f, headerY, previewLabelPaint)
                previewLabelPaint.textAlign = Paint.Align.LEFT
            }
            paint.color = 0x55FFFFFF
            canvas.drawRect(pvX + 10f, headerY + 8f, pvX + pvW - 10f, headerY + 9f, paint)
            var contentY = headerY + 30f

            // Modifier tag
            if (hasModifier) {
                paint.color = 0x55FFAB00
                canvas.drawRoundRect(pvX + 8f, contentY - 18f, pvX + pvW - 8f, contentY + 12f, 8f, 8f, paint)
                previewTextPaint.color = 0xFFFFAB00.toInt(); previewTextPaint.textSize = 16f
                canvas.drawText("${preview.modifier.emoji} ${preview.modifier.displayName}: ${preview.modifier.description}", pvX + 16f, contentY + 4f, previewTextPaint)
                contentY += 36f
            }

            // Enemy list / boss info
            if (preview.isBoss && preview.bossType != null) {
                val bt = preview.bossType!!
                previewTextPaint.color = bt.color; previewTextPaint.textSize = 18f
                canvas.drawText("${bt.emoji} ${bt.displayName}", pvX + 16f, contentY + 4f, previewTextPaint)
                previewTextPaint.textSize = 15f; previewTextPaint.color = 0xFFBDBDBD.toInt()
                canvas.drawText("  ⚡ ${bt.ability.name.replace('_', ' ')} + minions", pvX + 16f, contentY + 26f, previewTextPaint)
                contentY += 52f
            } else {
                previewTextPaint.textSize = 17f
                for ((type, count) in preview.enemies) {
                    if (contentY > pvY + pvH - warnExtra - 16f) break
                    val isThreat = (type == EnemyType.ARMORED_GOLEM &&
                                   TowerType.CANNON !in playerTowerTypes && TowerType.BALLISTA !in playerTowerTypes) ||
                                   ((type == EnemyType.FAST_SKELETON || type == EnemyType.BAT) &&
                                   TowerType.ICE !in playerTowerTypes && TowerType.VORTEX !in playerTowerTypes)
                    previewTextPaint.color = if (isThreat) 0xFFFF7043.toInt() else type.color
                    canvas.drawText("${type.emoji} ${type.name.replace('_', ' ')} ×$count${if (isThreat) " ⚠️" else ""}",
                        pvX + 22f, contentY, previewTextPaint)
                    contentY += 28f
                }
            }

            // Warning lines
            if (warnings.isNotEmpty()) {
                paint.color = 0x44FFFFFF
                canvas.drawRect(pvX + 10f, contentY + 2f, pvX + pvW - 10f, contentY + 3f, paint)
                contentY += 14f
                for ((msg, col) in warnings) {
                    if (contentY > pvY + pvH - 8f) break
                    paint.color = (col and 0x00FFFFFF) or 0x33000000
                    canvas.drawRoundRect(pvX + 8f, contentY - 18f, pvX + pvW - 8f, contentY + 8f, 6f, 6f, paint)
                    previewLabelPaint.color = col; previewLabelPaint.textSize = 16f
                    canvas.drawText(msg, pvX + 14f, contentY, previewLabelPaint)
                    contentY += 28f
                }
            }
        }

        // Achievement banner
        val ach = engine.newAchievement
        if (ach != null && engine.achievementBannerTimer > 0) {
            val abY = height * 0.25f
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, achBgPaint)
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, achBorderPaint)
            drawOutlinedText(canvas, "${ach.emoji} ${GameStrings.achievementUnlocked}", width / 2f, abY + 28f, achievePaint)
            drawOutlinedText(canvas, "${ach.title} — ${ach.description}", width / 2f, abY + 55f, achDescPaint)
        }

        // Placement mode indicator
        if (placementMode != null) {
            drawOutlinedText(canvas, GameStrings.placeTower(placementMode!!.emoji, placementMode!!.name), width / 2f, 160f, placeTextPaint)
            // Range ring: green if valid spot, red if blocked
            if (placementTouchX >= 0) {
                val r = placementMode!!.baseRange
                val canPlace = engine.canPlaceTower(placementTouchX, placementTouchY, placementMode!!)
                val ringColor = if (canPlace) 0xEE00E676.toInt() else 0xEEFF1744.toInt()
                val fillColor = if (canPlace) 0x2200E676.toInt() else 0x24FF1744.toInt()

                paint.color = fillColor
                paint.style = Paint.Style.FILL
                canvas.drawCircle(placementTouchX, placementTouchY, r, paint)

                paint.color = ringColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawCircle(placementTouchX, placementTouchY, r, paint)

                // Cross-hair at center
                paint.strokeWidth = 2.5f
                canvas.drawLine(placementTouchX - 16f, placementTouchY, placementTouchX + 16f, placementTouchY, paint)
                canvas.drawLine(placementTouchX, placementTouchY - 16f, placementTouchX, placementTouchY + 16f, paint)
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 1f
            }
        } else if (blockadePlacementMode) {
            drawOutlinedText(canvas, GameStrings.placeBlockade, width / 2f, 160f, placeTextPaint)
        } else if (trapPlacementMode != null) {
            val tName = when (trapPlacementMode!!) {
                TrapType.SPIKE -> "Spike"
                TrapType.TAR -> "Tar"
                TrapType.MINE -> "Mine"
            }
            val tEmoji = when (trapPlacementMode!!) {
                TrapType.SPIKE -> "\uD83D\uDDE1\uFE0F"
                TrapType.TAR -> "\uD83D\uDFE4"
                TrapType.MINE -> "\uD83D\uDCA3"
            }
            drawOutlinedText(canvas, "Place $tEmoji $tName on path", width / 2f, 160f, placeTextPaint)
        }

        // FPS display
        if (com.example.myapp.SoundManager.isShowFps(context)) {
            paint.color = 0xAAFFFFFF.toInt()
            paint.textSize = 20f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(GameStrings.fpsFmt(displayFps), width - 10f, height - 20f, paint)
            paint.textAlign = Paint.Align.CENTER
        }

        canvas.restore() // End screen shake

        // === BOSS SPAWN FLASH ===
        if (engine.bossFlashTimer > 0) {
            val flashAlpha = (255 * (engine.bossFlashTimer / 0.4f).coerceIn(0f, 1f)).toInt()
            paint.color = (flashAlpha shl 24) or 0xFFFFFF
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        // === WEATHER EFFECTS ===
        if (engine.weatherType > 0 && !engine.gameOver && !engine.isPaused) {
            val t = System.currentTimeMillis()
            val rng = java.util.Random(t / 50)
            val count = ((40 * engine.weatherIntensity) * (if (batterySaverOn) 0.5f else 1f)).toInt()
            if (engine.weatherType == 1) {
                // Rain
                paint.color = 0x336688AA
                paint.strokeWidth = 1.5f
                for (i in 0 until count) {
                    val rx = rng.nextFloat() * width
                    val ry = (t / 4f + i * 37f) % (height + 60f) - 30f
                    canvas.drawLine(rx, ry, rx - 3f, ry + 18f, paint)
                }
                paint.strokeWidth = 1f
            } else if (engine.weatherType == 2) {
                // Snow
                paint.color = 0x55FFFFFF
                for (i in 0 until count) {
                    val sx = (rng.nextFloat() * width + Math.sin((t / 2000.0) + i).toFloat() * 20f) % width
                    val sy = (t / 8f + i * 47f) % (height + 40f) - 20f
                    val sr = 2f + rng.nextFloat() * 3f
                    canvas.drawCircle(sx, sy, sr, paint)
                }
            }
        }

        // === SUBTLE VIGNETTE for atmospheric depth ===
        if (!engine.gameOver && !engine.campaignVictory) {
            val vignetteAlpha = if (engine.isNight) 50 else 25
            paint.color = (vignetteAlpha shl 24)
            val cornerSize = width * 0.35f
            // Top-left
            paint.shader = android.graphics.RadialGradient(0f, 0f, cornerSize, 0xFF000000.toInt(), 0x00000000, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, cornerSize, cornerSize, paint)
            // Top-right
            paint.shader = android.graphics.RadialGradient(width.toFloat(), 0f, cornerSize, 0xFF000000.toInt(), 0x00000000, Shader.TileMode.CLAMP)
            canvas.drawRect(width - cornerSize, 0f, width.toFloat(), cornerSize, paint)
            // Bottom-left
            paint.shader = android.graphics.RadialGradient(0f, height.toFloat(), cornerSize, 0xFF000000.toInt(), 0x00000000, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, height - cornerSize, cornerSize, height.toFloat(), paint)
            // Bottom-right
            paint.shader = android.graphics.RadialGradient(width.toFloat(), height.toFloat(), cornerSize, 0xFF000000.toInt(), 0x00000000, Shader.TileMode.CLAMP)
            canvas.drawRect(width - cornerSize, height - cornerSize, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
        }

        // === KILL STREAK BORDER GLOW ===
        if (engine.comboCount >= 5 && !engine.gameOver && !engine.isPaused) {
            val streakIntensity = ((engine.comboCount - 5).coerceAtMost(25) / 25f)
            val glowAlpha = (40 + streakIntensity * 80).toInt()
            val glowWidth = 12f + streakIntensity * 20f
            val pulseT = System.currentTimeMillis() / 1000.0
            val pulse = (0.7f + 0.3f * Math.sin(pulseT * 3.0).toFloat())
            val finalAlpha = (glowAlpha * pulse).toInt().coerceIn(0, 255)
            // Choose color based on streak level
            val glowColor = when {
                engine.comboCount >= 20 -> 0xFFFF1744.toInt()  // Red for insane streaks
                engine.comboCount >= 12 -> 0xFFFF9100.toInt()  // Orange for high streaks
                else -> 0xFFFFD600.toInt()                      // Yellow for moderate
            }
            paint.color = glowColor
            paint.alpha = finalAlpha
            // Draw border glow (4 edges)
            canvas.drawRect(0f, 0f, glowWidth, height.toFloat(), paint)               // Left
            canvas.drawRect(width - glowWidth, 0f, width.toFloat(), height.toFloat(), paint) // Right
            canvas.drawRect(0f, 0f, width.toFloat(), glowWidth, paint)               // Top
            canvas.drawRect(0f, height - glowWidth, width.toFloat(), height.toFloat(), paint) // Bottom
            paint.alpha = 255
        }

        // === BOUNTY BOARD HUD (collapsible) ===
        val bounties = engine.activeBounties
        if (bounties.isNotEmpty() && !engine.gameOver && !engine.isPaused) {
            val bx = 8f
            val headerY = height * 0.35f
            val bw = 240f
            val headerH = 28f
            val bCardH = 50f
            val arrow = if (bountyCollapsed) "▶" else "▼"
            // Header bar with tap-to-toggle arrow
            paint.color = 0xCC1B2838.toInt()
            canvas.drawRoundRect(bx, headerY, bx + bw, headerY + headerH, 8f, 8f, paint)
            bountyHeaderRect.set(bx, headerY, bx + bw, headerY + headerH)
            paint.color = 0xFFFFD54F.toInt()
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("$arrow \uD83C\uDFAF Bounties (${bounties.size})", bx + 10f, headerY + 20f, paint)

            if (!bountyCollapsed) {
                var by = headerY + headerH + 4f
                for (bounty in bounties) {
                    paint.color = if (bounty.completed) 0xCC2E7D32.toInt() else 0xCC1B2838.toInt()
                    canvas.drawRoundRect(bx, by, bx + bw, by + bCardH, 8f, 8f, paint)
                    // Border
                    paint.color = if (bounty.completed) 0xFF4CAF50.toInt() else 0x44FFFFFF
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawRoundRect(bx, by, bx + bw, by + bCardH, 8f, 8f, paint)
                    paint.style = Paint.Style.FILL
                    // Emoji + desc
                    paint.color = Color.WHITE
                    paint.textSize = 14f
                    val desc = if (bounty.description.length > 26) bounty.description.substring(0, 26) + ".." else bounty.description
                    canvas.drawText("${bounty.emoji} $desc", bx + 8f, by + 18f, paint)
                    // Progress bar
                    val progW = bw - 16f
                    val progRatio = if (bounty.target > 0) (bounty.progress.toFloat() / bounty.target).coerceIn(0f, 1f) else 1f
                    paint.color = 0x44FFFFFF
                    canvas.drawRoundRect(bx + 8f, by + 24f, bx + 8f + progW, by + 34f, 4f, 4f, paint)
                    paint.color = if (bounty.completed) 0xFF4CAF50.toInt() else 0xFFFFAB00.toInt()
                    canvas.drawRoundRect(bx + 8f, by + 24f, bx + 8f + progW * progRatio, by + 34f, 4f, 4f, paint)
                    // Progress text
                    paint.color = 0xDDFFFFFF.toInt()
                    paint.textSize = 11f
                    canvas.drawText("${bounty.progress}/${bounty.target}", bx + progW - 10f, by + 33f, paint)
                    // Reward
                    if (bounty.completed) {
                        paint.color = 0xFFFFD700.toInt()
                        paint.textSize = 12f
                        canvas.drawText("\u2714\uFE0F Done!", bx + 8f, by + bCardH - 3f, paint)
                    } else {
                        paint.color = 0xFF00E5FF.toInt()
                        paint.textSize = 11f
                        canvas.drawText("\uD83D\uDC8E${bounty.rewardDiamonds} \uD83E\uDE99${bounty.rewardGold}", bx + 8f, by + bCardH - 3f, paint)
                    }
                    by += bCardH + 4f
                }
            }
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 36f
        }

        // Pause overlay
        if (engine.isPaused && !engine.gameOver && !engine.campaignVictory) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
            drawOutlinedText(canvas, GameStrings.paused, width / 2f, height / 2f - 100f, gameOverPaint)
            // Run summary
            val summaryPaint = scoreDisplayPaint
            drawOutlinedText(canvas, GameStrings.pauseWaveScore(engine.wave, engine.score), width / 2f, height / 2f - 30f, summaryPaint)
            drawOutlinedText(canvas, GameStrings.pauseKillsGold(engine.totalKills, engine.gold), width / 2f, height / 2f + 15f, summaryPaint)
            drawOutlinedText(canvas, GameStrings.pauseTowersCombo(engine.towers.size, engine.bestCombo), width / 2f, height / 2f + 55f, summaryPaint)
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, GameStrings.pauseDiamonds(engine.diamondsEarnedThisRun), width / 2f, height / 2f + 95f, goDiamondPaint)
            }
            drawOutlinedText(canvas, GameStrings.tapResume, width / 2f, height / 2f + 140f, goldTextPaint)
        }

        // Tower stats popup (long-press)
        val statsT = towerStatsPopup
        if (statsT != null) {
            val px = statsT.x.coerceIn(130f, width - 130f)
            val py = (statsT.y - statsT.size - 120f).coerceAtLeast(40f)
            val cardW = 240f
            val cardH = 120f
            // Card background
            paint.color = 0xDD1B2838.toInt()
            canvas.drawRoundRect(px - cardW / 2, py, px + cardW / 2, py + cardH, 12f, 12f, paint)
            // Border
            paint.color = 0xFFFFD700.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(px - cardW / 2, py, px + cardW / 2, py + cardH, 12f, 12f, paint)
            paint.style = Paint.Style.FILL
            // Tower name
            val statsPaint = Paint().apply {
                color = 0xFFFFFFFF.toInt(); textSize = 18f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("${statsT.type.emoji} ${statsT.type.name} Lv${statsT.level}", px, py + 22f, statsPaint)
            statsPaint.typeface = Typeface.DEFAULT
            statsPaint.textSize = 15f
            // DPS
            val effectiveDps = statsT.damage * statsT.fireRate
            canvas.drawText("${GameStrings.towerDps}: %.1f".format(effectiveDps), px, py + 44f, statsPaint)
            // Kills
            statsPaint.color = 0xFFFFD700.toInt()
            canvas.drawText("${GameStrings.towerKills}: ${statsT.totalKills}", px, py + 64f, statsPaint)
            // Total damage
            statsPaint.color = 0xFFFF8A65.toInt()
            canvas.drawText("${GameStrings.towerDamage}: ${statsT.totalDamageDealt.toInt()}", px, py + 84f, statsPaint)
            // Ability status
            statsPaint.color = if (statsT.canUseAbility()) 0xFF4CAF50.toInt() else 0xFF9E9E9E.toInt()
            val abText = if (statsT.canUseAbility()) "${statsT.type.abilityName}: ${GameStrings.abilityReady}" else "${statsT.type.abilityName}: ${statsT.abilityTimer.toInt()}s"
            canvas.drawText(abText, px, py + 104f, statsPaint)
        }

        // Game over overlay (drawn outside shake)
        if (engine.gameOver) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            if (engine.isBossRush) {
                drawOutlinedText(canvas, GameStrings.bossRushOver, width / 2f, height / 2f - 80f, gameOverPaint)
            } else if (engine.isEndlessMode) {
                drawOutlinedText(canvas, GameStrings.endlessOver, width / 2f, height / 2f - 80f, gameOverPaint)
            } else if (engine.isRandomizerMode) {
                drawOutlinedText(canvas, GameStrings.randomizerOver, width / 2f, height / 2f - 80f, gameOverPaint)
            } else {
                drawOutlinedText(canvas, GameStrings.gameOver, width / 2f, height / 2f - 80f, gameOverPaint)
            }

            if (engine.isBossRush) {
                drawOutlinedText(canvas, GameStrings.bossesDefeatedScore(engine.bossRushWave, engine.score), width / 2f, height / 2f - 10f, scoreDisplayPaint)
            } else {
                drawOutlinedText(canvas, GameStrings.waveScore(engine.wave, engine.score), width / 2f, height / 2f - 10f, scoreDisplayPaint)
            }
            drawOutlinedText(canvas, GameStrings.killsCombo(engine.totalKills, engine.bestCombo), width / 2f, height / 2f + 35f, scoreDisplayPaint)

            // Score breakdown
            if (engine.scoreFromKills > 0) {
                scoreDisplayPaint.textSize = 18f
                drawOutlinedText(canvas, "Kills: ${engine.scoreFromKills}   +Combo: ${engine.scoreFromCombos}", width / 2f, height / 2f + 57f, scoreDisplayPaint)
                scoreDisplayPaint.textSize = 22f
            }

            // Diamonds earned this run
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, GameStrings.diamondsEarnedLong(engine.diamondsEarnedThisRun), width / 2f, height / 2f + 82f, goDiamondPaint)
            }

            if (engine.isBossRush) {
                if (engine.isNewBossRushRecord) {
                    drawOutlinedText(canvas, GameStrings.newBossRushRecord, width / 2f, height / 2f + 122f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, GameStrings.bossRushRecord(engine.bossRushHighWave), width / 2f, height / 2f + 122f, goHighScorePaint)
                }
            } else if (engine.isEndlessMode) {
                if (engine.isNewEndlessRecord) {
                    drawOutlinedText(canvas, GameStrings.newEndlessRecord, width / 2f, height / 2f + 122f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, GameStrings.endlessRecord(engine.endlessHighWave), width / 2f, height / 2f + 122f, goHighScorePaint)
                }
            } else {
                if (engine.isNewHighScore) {
                    drawOutlinedText(canvas, GameStrings.newHighScore, width / 2f, height / 2f + 122f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, GameStrings.highScoreRecord(engine.highScore, engine.highWave), width / 2f, height / 2f + 122f, goHighScorePaint)
                }
            }

            drawOutlinedText(canvas, GameStrings.tapRestart, width / 2f, height / 2f + 175f, goldTextPaint)
        }

        // Campaign victory overlay
        if (engine.campaignVictory) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            // Confetti behind text
            for (c in confettiParticles) {
                canvas.save()
                canvas.rotate(c.rotation, c.x, c.y)
                paint.color = c.color
                paint.alpha = (255 * (c.life / 5f).coerceIn(0.3f, 1f)).toInt()
                canvas.drawRect(c.x - c.size / 2, c.y - c.size / 4, c.x + c.size / 2, c.y + c.size / 4, paint)
                paint.alpha = 255
                canvas.restore()
            }

            val cl = engine.campaignLevel
            // Victory title with pulse effect
            val pulseScale = 1f + 0.05f * Math.sin((victoryAnimTimer * 3).toDouble()).toFloat()
            canvas.save()
            canvas.scale(pulseScale, pulseScale, width / 2f, height / 2f - 80f)
            drawOutlinedText(canvas, GameStrings.victory, width / 2f, height / 2f - 80f, victoryPaint)
            canvas.restore()
            if (cl != null) {
                drawOutlinedText(canvas, cl.title, width / 2f, height / 2f - 20f, scoreDisplayPaint)
            }
            // Star rating display — animate stars appearing one by one
            val stars = engine.campaignStars
            val shownStars = ((victoryAnimTimer * 2).toInt()).coerceAtMost(stars)
            val starText = "⭐".repeat(shownStars) + "☆".repeat(3 - shownStars)
            drawOutlinedText(canvas, starText, width / 2f, height / 2f + 20f, gameOverPaint)
            // Animated score counter
            drawOutlinedText(canvas, GameStrings.scoreFmt(victoryScoreDisplay), width / 2f, height / 2f + 60f, scoreDisplayPaint)
            if (cl != null) {
                val nextThreshold = when {
                    stars < 2 -> "⭐⭐ at ${cl.star2Score}"
                    stars < 3 -> "⭐⭐⭐ at ${cl.star3Score}"
                    else -> GameStrings.maxStars
                }
                drawOutlinedText(canvas, nextThreshold, width / 2f, height / 2f + 85f, goldTextPaint)
            }
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, GameStrings.diamondsEarned(engine.diamondsEarnedThisRun), width / 2f, height / 2f + 120f, goDiamondPaint)
            }
            // Total campaign stars earned so far
            val totalStars = engine.totalCampaignStars
            val maxStars = engine.totalCampaignLevels * 3
            drawOutlinedText(canvas, "⭐ $totalStars / $maxStars total", width / 2f, height / 2f + 145f, scoreDisplayPaint)
            drawOutlinedText(canvas, GameStrings.tapContinue, width / 2f, height / 2f + 185f, goldTextPaint)
        }

        // Dynamic Floating Virtual Joystick
        if (isJoystickActive) {
            joystickAlpha = (joystickAlpha + 0.15f).coerceAtMost(1f)
        } else if (joystickAlpha > 0f) {
            joystickAlpha = (joystickAlpha - 0.1f).coerceAtLeast(0f)
        }

        if (joystickAlpha > 0.01f) {
            val a = joystickAlpha
            // 1. Base translucent dark background disc
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((60 * a).toInt(), 15, 23, 42)
            canvas.drawCircle(joystickBaseX, joystickBaseY, joystickRadius, paint)

            // 2. Outer glowing ring
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3.5f
            paint.color = Color.argb((160 * a).toInt(), 96, 165, 250) // #60A5FA
            canvas.drawCircle(joystickBaseX, joystickBaseY, joystickRadius, paint)

            // 3. Inner guideline circle
            paint.strokeWidth = 1.5f
            paint.color = Color.argb((60 * a).toInt(), 147, 197, 253)
            canvas.drawCircle(joystickBaseX, joystickBaseY, joystickRadius * 0.5f, paint)

            // 4. Directional notches at 4 cardinal directions
            val tick = 10f
            paint.strokeWidth = 2.5f
            paint.color = Color.argb((180 * a).toInt(), 147, 197, 253)
            canvas.drawLine(joystickBaseX, joystickBaseY - joystickRadius, joystickBaseX, joystickBaseY - joystickRadius + tick, paint)
            canvas.drawLine(joystickBaseX, joystickBaseY + joystickRadius, joystickBaseX, joystickBaseY + joystickRadius - tick, paint)
            canvas.drawLine(joystickBaseX - joystickRadius, joystickBaseY, joystickBaseX - joystickRadius + tick, joystickBaseY, paint)
            canvas.drawLine(joystickBaseX + joystickRadius, joystickBaseY, joystickBaseX + joystickRadius - tick, joystickBaseY, paint)

            // 5. Connecting line between base center and knob
            val kdx = joystickKnobX - joystickBaseX
            val kdy = joystickKnobY - joystickBaseY
            val kdist = Math.hypot(kdx.toDouble(), kdy.toDouble()).toFloat()
            if (kdist > 5f) {
                paint.strokeWidth = 3f
                paint.color = Color.argb((120 * a).toInt(), 96, 165, 250)
                canvas.drawLine(joystickBaseX, joystickBaseY, joystickKnobX, joystickKnobY, paint)
            }

            // 6. Knob outer soft glow
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((90 * a).toInt(), 37, 99, 235)
            canvas.drawCircle(joystickKnobX, joystickKnobY, 44f, paint)

            // 7. Knob solid core
            paint.color = Color.argb((220 * a).toInt(), 59, 130, 246)
            canvas.drawCircle(joystickKnobX, joystickKnobY, 34f, paint)

            // 8. Knob 3D sphere highlight
            paint.color = Color.argb((210 * a).toInt(), 224, 242, 254)
            canvas.drawCircle(joystickKnobX - 7f, joystickKnobY - 7f, 10f, paint)

            paint.style = Paint.Style.FILL
        }
    }

    // Dynamic Floating Virtual Joystick
    private var joystickPointerId = -1
    private var joystickBaseX = 0f
    private var joystickBaseY = 0f
    private var joystickKnobX = 0f
    private var joystickKnobY = 0f
    private var isJoystickActive = false
    private var joystickAlpha = 0f
    private val joystickRadius = 140f
    private val joystickDeadzone = 12f

    private var bountyCollapsed = false
    private var bountyHeaderRect = android.graphics.RectF()

    private fun isJoystickZone(x: Float, y: Float): Boolean {
        return x < width * 0.45f && y > height * 0.15f
    }

    private fun handleBattlefieldTap(tx: Float, ty: Float): Boolean {
        longPressDownTime = System.currentTimeMillis()
        longPressDownX = tx
        longPressDownY = ty
        if (placementMode != null || blockadePlacementMode || trapPlacementMode != null) {
            placementTouchX = tx
            placementTouchY = ty
        }
        // Dismiss stats popup on any tap
        if (towerStatsPopup != null) {
            towerStatsPopup = null
            return true
        }
        // Toggle bounty board collapse
        if (bountyHeaderRect.contains(tx, ty) && engine.activeBounties.isNotEmpty()) {
            bountyCollapsed = !bountyCollapsed
            return true
        }
        synchronized(engine.lock) {
            if (engine.campaignVictory) {
                selectedTower = null
                placementMode = null
                blockadePlacementMode = false
                trapPlacementMode = null
                post { onCampaignVictory?.invoke() }
                return true
            }

            if (engine.gameOver) {
                SoundManager.play(SfxType.UI_CLICK)
                engine.restart()
                gameOverFired = false
                milestoneDialogShown = false
                selectedTower = null
                placementMode = null
                blockadePlacementMode = false
                trapPlacementMode = null
                return true
            }

            // Tower placement mode
            if (placementMode != null) {
                if (engine.placeTower(tx, ty, placementMode!!)) {
                    placementMode = null
                    placementTouchX = -1f
                    placementTouchY = -1f
                    runCatching { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    post {
                        onPlacementModeChanged?.invoke(false)
                        onGoldChanged?.invoke(engine.gold)
                    }
                }
                return true
            }

            // Blockade placement mode
            if (blockadePlacementMode) {
                if (engine.placeBlockade(tx, ty)) {
                    blockadePlacementMode = false
                    placementTouchX = -1f
                    placementTouchY = -1f
                    runCatching { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    post {
                        onPlacementModeChanged?.invoke(false)
                        onGoldChanged?.invoke(engine.gold)
                    }
                }
                return true
            }

            // Trap placement mode
            if (trapPlacementMode != null) {
                if (engine.placeTrap(tx, ty, trapPlacementMode!!)) {
                    trapPlacementMode = null
                    placementTouchX = -1f
                    placementTouchY = -1f
                    runCatching { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    post {
                        onPlacementModeChanged?.invoke(false)
                        onGoldChanged?.invoke(engine.gold)
                    }
                }
                return true
            }

            // Check if tapped on a tower (to select it)
            val tapped = engine.towers.find { it.distanceTo(tx, ty) < it.size + 24f }
            if (tapped != null) {
                selectedTower = tapped
                SoundManager.play(SfxType.UI_CLICK)
                runCatching { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                post { onTowerSelected?.invoke(tapped) }
                return true
            }

            // If a tower was previously selected, tapping ground dismisses selection without moving player
            if (selectedTower != null) {
                selectedTower = null
                post { onTowerSelected?.invoke(null) }
                return true
            }

            // Try to collect a supply drop
            if (engine.collectSupplyDrop(tx, ty)) {
                runCatching { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                post { onGoldChanged?.invoke(engine.gold) }
                return true
            }
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val actionIndex = event.actionIndex

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val pid = event.getPointerId(0)
                val tx = event.getX(0)
                val ty = event.getY(0)
                if (isJoystickZone(tx, ty)) {
                    joystickPointerId = pid
                    joystickBaseX = tx
                    joystickBaseY = ty
                    joystickKnobX = tx
                    joystickKnobY = ty
                    isJoystickActive = true
                    joystickAlpha = 1f
                    return true
                } else {
                    return handleBattlefieldTap(tx, ty)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pid = event.getPointerId(actionIndex)
                val px = event.getX(actionIndex)
                val py = event.getY(actionIndex)
                if (joystickPointerId == -1 && isJoystickZone(px, py)) {
                    joystickPointerId = pid
                    joystickBaseX = px
                    joystickBaseY = py
                    joystickKnobX = px
                    joystickKnobY = py
                    isJoystickActive = true
                    joystickAlpha = 1f
                    return true
                } else {
                    return handleBattlefieldTap(px, py)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isJoystickActive && joystickPointerId != -1) {
                    val jIdx = event.findPointerIndex(joystickPointerId)
                    if (jIdx != -1) {
                        val jx = event.getX(jIdx)
                        val jy = event.getY(jIdx)
                        val dx = jx - joystickBaseX
                        val dy = jy - joystickBaseY
                        val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        if (dist > joystickRadius) {
                            joystickKnobX = joystickBaseX + (dx / dist) * joystickRadius
                            joystickKnobY = joystickBaseY + (dy / dist) * joystickRadius
                        } else {
                            joystickKnobX = jx
                            joystickKnobY = jy
                        }
                        if (dist > joystickDeadzone) {
                            val magnitude = ((dist - joystickDeadzone) / (joystickRadius - joystickDeadzone)).coerceIn(0f, 1f)
                            val nx = dx / dist
                            val ny = dy / dist
                            synchronized(engine.lock) {
                                engine.player.setVelocity(nx, ny, magnitude)
                            }
                        } else {
                            synchronized(engine.lock) {
                                engine.player.stopMoving()
                            }
                        }
                    }
                }

                // Long press & placement preview for non-joystick pointers
                for (p in 0 until event.pointerCount) {
                    val pid = event.getPointerId(p)
                    if (pid != joystickPointerId) {
                        val ptx = event.getX(p)
                        val pty = event.getY(p)
                        if (placementMode != null || blockadePlacementMode || trapPlacementMode != null) {
                            placementTouchX = ptx
                            placementTouchY = pty
                        }
                        if (longPressDownTime > 0 && towerStatsPopup == null) {
                            val dx = ptx - longPressDownX
                            val dy = pty - longPressDownY
                            if (dx * dx + dy * dy > 30f * 30f) {
                                longPressDownTime = 0L
                            } else if (System.currentTimeMillis() - longPressDownTime > 500) {
                                synchronized(engine.lock) {
                                    val heldTower = engine.towers.find { it.distanceTo(longPressDownX, longPressDownY) < it.size + 20f }
                                    if (heldTower != null) {
                                        towerStatsPopup = heldTower
                                        longPressDownTime = 0L
                                        return true
                                    }
                                }
                                longPressDownTime = 0L
                            }
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pid = event.getPointerId(actionIndex)
                if (pid == joystickPointerId) {
                    isJoystickActive = false
                    joystickPointerId = -1
                    synchronized(engine.lock) {
                        engine.player.stopMoving()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isJoystickActive || joystickPointerId != -1) {
                    isJoystickActive = false
                    joystickPointerId = -1
                    synchronized(engine.lock) {
                        engine.player.stopMoving()
                    }
                }
                longPressDownTime = 0L
                placementTouchX = -1f
                placementTouchY = -1f
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        stopGameThread()
    }

    fun resume() {
        milestoneDialogShown = false // Re-trigger milestone dialog if pending
        if (holder.surface.isValid) {
            startGameThread()
        }
    }

    private fun startGameThread() {
        if (!running) {
            running = true
            gameThread = Thread(this)
            gameThread?.start()
        }
    }

    private fun stopGameThread() {
        running = false
        try {
            gameThread?.join(2000)
        } catch (_: InterruptedException) { }
        gameThread = null
    }

    fun getSelectedTower(): Tower? = selectedTower
    fun clearSelectedTower() { selectedTower = null; onTowerSelected?.invoke(null) }

    fun clearPlacementMode() {
        val wasPlacing = placementMode != null || blockadePlacementMode || trapPlacementMode != null
        placementMode = null
        blockadePlacementMode = false
        trapPlacementMode = null
        placementTouchX = -1f
        placementTouchY = -1f
        if (wasPlacing) {
            post { onPlacementModeChanged?.invoke(false) }
        }
    }

    fun resetGameOverState() {
        gameOverFired = false
        victoryTriggered = false
        confettiParticles.clear()
        victoryAnimTimer = 0f
        milestoneDialogShown = false
        selectedTower = null
        placementMode = null
        blockadePlacementMode = false
        trapPlacementMode = null
        placementTouchX = -1f
        placementTouchY = -1f
    }

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

        // Build smooth curves through path waypoints
        val canvasPaths = mutableListOf<Path>()
        for (gamePath in engine.paths) {
            canvasPaths.add(buildSmoothPath(gamePath.waypoints))
        }
        pathCache = canvasPaths

        // Generate decorations avoiding paths
        val decos = mutableListOf<Decoration>()
        val rng = java.util.Random(77L)
        val groundTop = h * 0.12f
        repeat(90) {
            val dx = rng.nextFloat() * w
            val dy = groundTop + rng.nextFloat() * (h - groundTop - 80f)
            // Skip near base
            val dbx = dx - bx; val dby = dy - by
            if (dbx * dbx + dby * dby < 100f * 100f) return@repeat
            // Skip near any path segment
            var onPath = false
            for (gamePath in engine.paths) {
                val wps = gamePath.waypoints
                for (i in 0 until wps.size - 1) {
                    val ax = wps[i].x; val ay = wps[i].y
                    val bpx = wps[i + 1].x; val bpy = wps[i + 1].y
                    val segLenSq = (bpx - ax) * (bpx - ax) + (bpy - ay) * (bpy - ay)
                    val t = if (segLenSq < 0.01f) 0f else
                        (((dx - ax) * (bpx - ax) + (dy - ay) * (bpy - ay)) / segLenSq).coerceIn(0f, 1f)
                    val px = ax + t * (bpx - ax)
                    val py = ay + t * (bpy - ay)
                    val pdx = dx - px; val pdy = dy - py
                    if (pdx * pdx + pdy * pdy < 55f * 55f) { onPath = true; break }
                }
                if (onPath) break
            }
            if (onPath) return@repeat
            val type = rng.nextInt(8)
            val scale = 0.7f + rng.nextFloat() * 0.6f
            decos.add(Decoration(dx, dy, type, scale, rng.nextInt()))
        }
        decorations = decos
    }

    /** Build a smooth Canvas Path through waypoints using quadratic curves */
    private fun buildSmoothPath(waypoints: List<GamePoint>): Path {
        val p = Path()
        if (waypoints.size < 2) return p
        p.moveTo(waypoints[0].x, waypoints[0].y)
        if (waypoints.size == 2) {
            p.lineTo(waypoints[1].x, waypoints[1].y)
            return p
        }
        for (i in 1 until waypoints.size - 1) {
            val curr = waypoints[i]
            val next = waypoints[i + 1]
            val midX = (curr.x + next.x) / 2f
            val midY = (curr.y + next.y) / 2f
            p.quadTo(curr.x, curr.y, midX, midY)
        }
        val last = waypoints.last()
        p.lineTo(last.x, last.y)
        return p
    }

    /** Draw terrain decorations — trees, rocks, bushes, flowers */
    private fun drawDecorations(canvas: Canvas) {
        val t = System.currentTimeMillis() / 1000.0
        for (d in decorations) {
            val x = d.x; val y = d.y; val s = d.scale
            // Per-decoration wind sway offset based on seed
            val windPhase = t * 1.2 + d.seed * 0.7
            val sway = (Math.sin(windPhase) * 2.5 * s).toFloat()
            when (d.type) {
                0 -> { // Tree (sway canopy)
                    terrainPaint.color = trunkColor
                    canvas.drawRect(x - 3f * s, y - 8f * s, x + 3f * s, y + 12f * s, terrainPaint)
                    shadowPaint.alpha = 25
                    canvas.drawOval(x - 10f * s, y + 8f * s, x + 10f * s, y + 14f * s, shadowPaint)
                    terrainPaint.color = grassColor
                    canvas.drawCircle(x + sway, y - 8f * s, 12f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x - 3f * s + sway * 1.2f, y - 12f * s, 8f * s, terrainPaint)
                    canvas.drawCircle(x + 4f * s + sway * 0.8f, y - 6f * s, 7f * s, terrainPaint)
                }
                1 -> { // Rock (static)
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
                2 -> { // Bush (gentle sway)
                    shadowPaint.alpha = 18
                    canvas.drawOval(x - 8f * s, y + 2f * s, x + 8f * s, y + 6f * s, shadowPaint)
                    terrainPaint.color = grassColor
                    canvas.drawCircle(x - 4f * s + sway * 0.6f, y, 6f * s, terrainPaint)
                    canvas.drawCircle(x + 3f * s + sway * 0.6f, y - 1f * s, 5f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x + sway * 0.8f, y - 3f * s, 5f * s, terrainPaint)
                }
                3 -> { // Flowers (sway stem)
                    val flowerSway = sway * 0.5f
                    terrainPaint.color = grassLightColor
                    canvas.drawLine(x, y, x + flowerSway, y - 6f * s, terrainPaint)
                    val fc = when (d.seed % 4) {
                        0 -> 0xFFFF6B6B.toInt()
                        1 -> 0xFFFFD93D.toInt()
                        2 -> 0xFFC084FC.toInt()
                        else -> 0xFF6BCB77.toInt()
                    }
                    terrainPaint.color = fc
                    canvas.drawCircle(x + flowerSway, y - 7f * s, 3f * s, terrainPaint)
                    terrainPaint.color = 0xFFFFFFCC.toInt()
                    canvas.drawCircle(x + flowerSway, y - 7f * s, 1.5f * s, terrainPaint)
                }
                4 -> { // Pine tree (sway top)
                    terrainPaint.color = trunkColor
                    canvas.drawRect(x - 2f * s, y - 4f * s, x + 2f * s, y + 12f * s, terrainPaint)
                    shadowPaint.alpha = 25
                    canvas.drawOval(x - 8f * s, y + 8f * s, x + 8f * s, y + 14f * s, shadowPaint)
                    terrainPaint.color = currentTheme.foliageColor1
                    val tp = Path()
                    tp.moveTo(x + sway * 1.5f, y - 22f * s)
                    tp.lineTo(x - 10f * s, y - 2f * s)
                    tp.lineTo(x + 10f * s, y - 2f * s)
                    tp.close()
                    canvas.drawPath(tp, terrainPaint)
                    terrainPaint.color = currentTheme.foliageColor2
                    val tp2 = Path()
                    tp2.moveTo(x + sway, y - 18f * s)
                    tp2.lineTo(x - 7f * s, y - 5f * s)
                    tp2.lineTo(x + 7f * s, y - 5f * s)
                    tp2.close()
                    canvas.drawPath(tp2, terrainPaint)
                    // Snow tip if snow caps active
                    if (currentTheme.hasSnowCaps) {
                        terrainPaint.color = 0xEEFFFFFF.toInt()
                        canvas.drawCircle(x + sway * 1.5f, y - 22f * s, 3f * s, terrainPaint)
                    }
                }
                5 -> { // Mushroom (static)
                    if (currentTheme.mapType == MapType.ENCHANTED) {
                        // Bioluminescent fairy mushroom
                        terrainPaint.color = 0xFFE0E0E0.toInt()
                        canvas.drawRect(x - 1.5f * s, y - 3f * s, x + 1.5f * s, y + 4f * s, terrainPaint)
                        val glowCol = if (d.seed % 2 == 0) 0xFF00E5FF.toInt() else 0xFFE040FB.toInt()
                        terrainPaint.color = glowCol
                        canvas.drawCircle(x, y - 5f * s, 5.5f * s, terrainPaint)
                        terrainPaint.color = (glowCol and 0x00FFFFFF) or 0x44000000
                        canvas.drawCircle(x, y - 5f * s, 8.5f * s, terrainPaint)
                    } else {
                        terrainPaint.color = 0xFFE0E0E0.toInt()
                        canvas.drawRect(x - 1.5f * s, y - 3f * s, x + 1.5f * s, y + 4f * s, terrainPaint)
                        terrainPaint.color = 0xFFE53935.toInt()
                        canvas.drawCircle(x, y - 5f * s, 5f * s, terrainPaint)
                        terrainPaint.color = 0xFFFFFFFF.toInt()
                        canvas.drawCircle(x - 2f * s, y - 6f * s, 1.2f * s, terrainPaint)
                        canvas.drawCircle(x + 2f * s, y - 4f * s, 1f * s, terrainPaint)
                    }
                }
                6 -> { // Tall grass (animated wave)
                    terrainPaint.color = 0xFF558B2F.toInt()
                    for (g in -2..2) {
                        val gx = x + g * 2f * s
                        val grassSway = (Math.sin(windPhase + g * 0.4) * 3.5 * s).toFloat()
                        canvas.drawLine(gx, y + 2f * s, gx + grassSway, y - 10f * s, terrainPaint)
                    }
                }
                7 -> { // Berry bush (gentle sway)
                    shadowPaint.alpha = 18
                    canvas.drawOval(x - 9f * s, y + 2f * s, x + 9f * s, y + 6f * s, shadowPaint)
                    terrainPaint.color = 0xFF33691E.toInt()
                    canvas.drawCircle(x - 4f * s + sway * 0.5f, y, 7f * s, terrainPaint)
                    canvas.drawCircle(x + 3f * s + sway * 0.5f, y - 1f * s, 6f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x + sway * 0.6f, y - 3f * s, 5f * s, terrainPaint)
                    terrainPaint.color = 0xFFD32F2F.toInt()
                    canvas.drawCircle(x - 3f * s + sway * 0.3f, y - 2f * s, 1.8f * s, terrainPaint)
                    canvas.drawCircle(x + 2f * s + sway * 0.3f, y - 4f * s, 1.5f * s, terrainPaint)
                    canvas.drawCircle(x + 4f * s + sway * 0.3f, y + 1f * s, 1.6f * s, terrainPaint)
                }
            }
        }

        // Ambient particles per biome style
        val night = engine.isNight
        val ambRng = java.util.Random(123L)
        when (currentTheme.ambientParticleType) {
            AmbientParticleType.BLIZZARD_SNOW -> {
                // Drifting blizzard snow
                paint.color = 0xEEFFFFFF.toInt()
                for (i in 0 until 24) {
                    val sx = (ambRng.nextFloat() * width + (t * (30f + i * 5f)).toFloat()) % (width + 40f) - 20f
                    val sy = (ambRng.nextFloat() * height + (t * (60f + (i % 5) * 20f)).toFloat()) % height
                    val sr = 1.5f + (i % 3) * 1.2f
                    val sway = (Math.sin(t * 2.0 + i) * 6f).toFloat()
                    paint.alpha = 140 + (i % 4) * 25
                    canvas.drawCircle(sx + sway, sy, sr, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.LAVA_EMBERS, AmbientParticleType.VOLCANO_ASH -> {
                // Rising fiery embers and drifting dark ash
                for (i in 0 until 18) {
                    val ex = (ambRng.nextFloat() * width + (Math.sin(t * 1.5 + i) * 20f).toFloat())
                    val ey = height - ((t * (40f + (i % 6) * 15f) + i * 45f).toFloat() % (height * 0.85f))
                    val er = 1.8f + (i % 3) * 1.2f
                    val isAsh = i % 4 == 0
                    if (isAsh) {
                        paint.color = 0x88212121.toInt()
                    } else {
                        paint.color = if (i % 2 == 0) 0xFFFF5722.toInt() else 0xFFFFAB00.toInt()
                        paint.alpha = (120 + 80 * Math.sin(t * 4.0 + i)).toInt().coerceIn(60, 240)
                    }
                    canvas.drawCircle(ex, ey, er, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.FAIRY_SPARKS -> {
                // Floating mystical fairy motes
                for (i in 0 until 14) {
                    val fx = ambRng.nextFloat() * width
                    val fy = height * 0.15f + ambRng.nextFloat() * (height * 0.7f)
                    val ox = (Math.sin(t * 1.2 + i * 1.7) * 25f).toFloat()
                    val oy = (Math.cos(t * 0.9 + i * 2.1) * 18f).toFloat()
                    val pulse = (0.4f + 0.6f * Math.sin(t * 3.0 + i * 1.1).toFloat()).coerceIn(0f, 1f)
                    val moteColor = when (i % 3) {
                        0 -> 0xFF00E5FF.toInt()
                        1 -> 0xFFE040FB.toInt()
                        else -> 0xFFFFD700.toInt()
                    }
                    paint.color = moteColor
                    paint.alpha = (pulse * 220).toInt()
                    canvas.drawCircle(fx + ox, fy + oy, 2.5f, paint)
                    paint.alpha = (pulse * 70).toInt()
                    canvas.drawCircle(fx + ox, fy + oy, 7f, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.DESERT_DUST -> {
                // Drifting golden sand motes and heat shimmer
                paint.color = 0xFFD4B86A.toInt()
                for (i in 0 until 12) {
                    val mx = (ambRng.nextFloat() * width + (t * 25f + i * 40f).toFloat()) % (width + 40f) - 20f
                    val my = height * 0.15f + ambRng.nextFloat() * (height * 0.7f) + (Math.sin(t * 0.8 + i) * 12f).toFloat()
                    paint.alpha = 40 + (25 * Math.sin(t + i * 0.9)).toInt().coerceIn(0, 50)
                    canvas.drawCircle(mx, my, 1.8f + (i % 2) * 1.2f, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.CANYON_WIND -> {
                // Mountain canyon dust and pine needles
                paint.color = 0xFFBCAAA4.toInt()
                for (i in 0 until 10) {
                    val mx = (ambRng.nextFloat() * width + (t * 22f + i * 35f).toFloat()) % (width + 40f) - 20f
                    val my = height * 0.15f + ambRng.nextFloat() * (height * 0.7f)
                    paint.alpha = 35 + (20 * Math.sin(t * 1.2 + i)).toInt().coerceIn(0, 45)
                    canvas.drawCircle(mx, my, 2f, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.MIST_BANNERS -> {
                // Roadside mist drifts
                paint.color = 0x22ECEFF1
                for (i in 0 until 5) {
                    val mx = (ambRng.nextFloat() * width + (t * 8f + i * 90f).toFloat()) % (width + 100f) - 50f
                    val my = height * 0.35f + ambRng.nextFloat() * (height * 0.45f)
                    canvas.drawOval(mx - 40f, my - 15f, mx + 40f, my + 15f, paint)
                }
                paint.alpha = 255
            }
            AmbientParticleType.LEAVES -> {
                if (night) {
                    // Fireflies
                    paint.color = 0xFFFFEB3B.toInt()
                    for (i in 0 until 12) {
                        val fx = ambRng.nextFloat() * width
                        val fy = height * 0.15f + ambRng.nextFloat() * (height * 0.7f)
                        val pulse = (0.3f + 0.7f * Math.sin(t * 2.0 + i * 1.3).toFloat()).coerceIn(0f, 1f)
                        paint.alpha = (pulse * 200).toInt()
                        val ox = (Math.sin(t * 0.8 + i * 2.1) * 15f).toFloat()
                        val oy = (Math.cos(t * 0.6 + i * 1.7) * 10f).toFloat()
                        canvas.drawCircle(fx + ox, fy + oy, 2.5f, paint)
                        paint.alpha = (pulse * 60).toInt()
                        canvas.drawCircle(fx + ox, fy + oy, 6f, paint)
                    }
                    paint.alpha = 255
                } else {
                    // Falling leaves
                    for (i in 0 until 7) {
                        val leafPhase = (t * 0.7 + i * 3.0) % 8.0
                        val lx = (ambRng.nextFloat() * width + (leafPhase * 25f).toFloat()) % (width + 40f) - 20f
                        val ly = (leafPhase / 8.0 * (height * 0.8f) + height * 0.1f).toFloat()
                        val leafSway = (Math.sin(t * 2.0 + i * 1.5) * 8f).toFloat()
                        val rot = (t * 60 + i * 45).toFloat()
                        paint.color = when (i % 3) { 0 -> 0xDD4CAF50.toInt(); 1 -> 0xDDFF9800.toInt(); else -> 0xDDE53935.toInt() }
                        canvas.save()
                        canvas.rotate(rot, lx + leafSway, ly)
                        canvas.drawOval(lx + leafSway - 3f, ly - 1.5f, lx + leafSway + 3f, ly + 1.5f, paint)
                        canvas.restore()
                    }
                    paint.alpha = 255
                }
            }
        }

        // --- Weather Atmospheric Overlays & Effects ---
        when (engine.currentWeather) {
            WeatherEvent.THUNDERSTORM -> {
                // Diagonal rain streaks across the field
                paint.color = 0x7790CAF9.toInt()
                paint.strokeWidth = 1.8f
                paint.style = Paint.Style.STROKE
                for (i in 0 until 40) {
                    val rx = (ambRng.nextFloat() * width + (t * 240f + i * 50f).toFloat()) % (width + 60f) - 30f
                    val ry = (ambRng.nextFloat() * height + (t * 650f + i * 40f).toFloat()) % height
                    canvas.drawLine(rx, ry, rx - 6f, ry + 24f, paint)
                }
                paint.style = Paint.Style.FILL
            }
            WeatherEvent.BLOOD_MOON -> {
                // Screen-wide translucent crimson overlay & floating blood cinders
                paint.color = 0x24B71C1C.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                for (i in 0 until 18) {
                    val cx = (ambRng.nextFloat() * width + (Math.sin(t * 1.8 + i) * 22f).toFloat())
                    val cy = height - ((t * (40f + i * 14f) + i * 50f).toFloat() % (height * 0.85f))
                    paint.color = if (i % 2 == 0) 0xFFFF1744.toInt() else 0xFFFF5252.toInt()
                    paint.alpha = (140 + 80 * Math.sin(t * 3.5 + i)).toInt().coerceIn(60, 240)
                    canvas.drawCircle(cx, cy, 2.2f, paint)
                }
                paint.alpha = 255
            }
            WeatherEvent.SOLAR_ECLIPSE -> {
                // Subtle twilight violet tint & rising prismatic starlight dust
                paint.color = 0x1A311B92.toInt()
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                for (i in 0 until 16) {
                    val sx = ambRng.nextFloat() * width
                    val sy = height * 0.15f + ambRng.nextFloat() * (height * 0.75f)
                    val pulse = (0.4f + 0.6f * Math.sin(t * 2.5 + i * 1.5).toFloat()).coerceIn(0f, 1f)
                    paint.color = if (i % 2 == 0) 0xFFFFD700.toInt() else 0xFFE040FB.toInt()
                    paint.alpha = (pulse * 190).toInt()
                    canvas.drawCircle(sx, sy, 2.4f, paint)
                }
                paint.alpha = 255
            }
            WeatherEvent.CLEAR -> {}
        }

        // Heavenly Lightning Flash & Bolt Strike
        if (engine.lightningFlashTimer > 0f) {
            val flashAlpha = ((engine.lightningFlashTimer / 0.22f) * 160).toInt().coerceIn(0, 200)
            canvas.drawColor(Color.argb(flashAlpha, 235, 248, 255))

            engine.lastLightningTarget?.let { target ->
                val boltPaint = Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    strokeWidth = 4.5f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                }
                val glowBoltPaint = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    strokeWidth = 11f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    alpha = 180
                    isAntiAlias = true
                }
                val startX = target.x + (Math.sin(engine.lightningFlashTimer.toDouble() * 25.0) * 35.0).toFloat()
                val boltPath = Path().apply {
                    moveTo(startX, 0f)
                    val steps = 6
                    val stepY = target.y / steps
                    for (s in 1..steps) {
                        val nextY = stepY * s
                        val nextX = if (s == steps) target.x else target.x + ((s % 2 * 2 - 1) * 26f * (1f - s.toFloat() / steps))
                        lineTo(nextX, nextY)
                    }
                }
                canvas.drawPath(boltPath, glowBoltPaint)
                canvas.drawPath(boltPath, boltPaint)
                canvas.drawCircle(target.x, target.y, 45f, glowBoltPaint)
                canvas.drawCircle(target.x, target.y, 22f, boltPaint)
            }
        }
    }

    /** Draw animated red arrows along enemy paths between waves */
    private fun drawPathArrows(canvas: Canvas) {
        val arrowSize = 14f
        val spacing = 70f
        val animOffset = (waterPhase * 40f) % spacing  // reuse waterPhase for animation
        for (gamePath in engine.paths) {
            val wps = gamePath.waypoints
            // Walk each segment and place arrows at regular intervals
            var accumulated = animOffset
            for (i in 0 until wps.size - 1) {
                val ax = wps[i].x; val ay = wps[i].y
                val bx = wps[i + 1].x; val by = wps[i + 1].y
                val dx = bx - ax; val dy = by - ay
                val segLen = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (segLen < 1f) continue
                val nx = dx / segLen; val ny = dy / segLen  // unit direction toward base
                var pos = accumulated
                while (pos < segLen) {
                    val px = ax + nx * pos
                    val py = ay + ny * pos
                    // Pulsing alpha
                    val pulse = (0.4f + 0.6f * Math.abs(Math.sin((waterPhase * 2f + i + pos * 0.02f).toDouble())).toFloat())
                    arrowPaint.color = 0xFFF44336.toInt()
                    arrowPaint.alpha = (180 * pulse).toInt()
                    // Draw small triangle pointing in direction (nx, ny)
                    val angle = Math.atan2(ny.toDouble(), nx.toDouble()).toFloat()
                    canvas.save()
                    canvas.translate(px, py)
                    canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())
                    arrowPath.reset()
                    arrowPath.moveTo(arrowSize, 0f)            // tip
                    arrowPath.lineTo(-arrowSize, -arrowSize * 0.7f) // left wing
                    arrowPath.lineTo(-arrowSize * 0.3f, 0f)    // inner notch
                    arrowPath.lineTo(-arrowSize, arrowSize * 0.7f)  // right wing
                    arrowPath.close()
                    canvas.drawPath(arrowPath, arrowPaint)
                    canvas.restore()
                    pos += spacing
                }
                accumulated = pos - segLen  // carry over for next segment
            }
        }
    }

    // Reusable paint for star drawing
    private val paint = Paint().apply { isAntiAlias = true }

    /** Adjust brightness of an ARGB color by shifting R/G/B by delta */
    private fun adjustBrightness(color: Int, delta: Int): Int {
        val a = (color ushr 24) and 0xFF
        val r = (((color ushr 16) and 0xFF) + delta).coerceIn(0, 255)
        val g = (((color ushr 8) and 0xFF) + delta).coerceIn(0, 255)
        val b = ((color and 0xFF) + delta).coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
