package com.example.myapp.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.myapp.SoundManager
import com.example.myapp.SfxType
import java.util.Locale

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    @Volatile private var running = false
    @Volatile private var initialized = false
    private val engine = GameEngine(context)
    private val pendingReadyActions = mutableListOf<() -> Unit>()
    init {
        GameEngineHolder.engine = engine
    }

    var onGoldChanged: ((Int) -> Unit)? = null
    var onWaveChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int, Int) -> Unit)? = null
    var onDiamondsChanged: ((Int) -> Unit)? = null
    var onTowerSelected: ((Tower?) -> Unit)? = null
    var onCampaignVictory: (() -> Unit)? = null
    @Volatile private var gameOverFired = false
    private var lastFpsTime = System.nanoTime()
    private var frameCount = 0
    private var displayFps = 0

    // Paints
    // Static terrain colors — overridden per mapType in updateThemeColors()
    private var grassColor = 0xFF3A7D3A.toInt()
    private var grassLightColor = 0xFF4CAF50.toInt()
    private var pathColor = 0xFF8D6E63.toInt()
    private var pathEdgeColor = 0xFF6D4C41.toInt()
    private var trunkColor = 0xFF795548.toInt()
    private var rockColor = 0xFF757575.toInt()
    private var rockHighlightColor = 0xFF9E9E9E.toInt()

    /** Update terrain palette based on map theme */
    private fun updateThemeColors() {
        when (engine.mapType) {
            MapType.DESERT -> {
                grassColor = 0xFFC2A04E.toInt()       // sandy
                grassLightColor = 0xFFD4B86A.toInt()   // light sand
                pathColor = 0xFFB89A5A.toInt()         // dusty trail
                pathEdgeColor = 0xFF9E8040.toInt()     // dark sand
                trunkColor = 0xFF6D4C41.toInt()        // dry wood
                rockColor = 0xFFA08060.toInt()         // sandstone
                rockHighlightColor = 0xFFC0A080.toInt()
            }
            MapType.SNOW -> {
                grassColor = 0xFFE0E8F0.toInt()        // snowy ground
                grassLightColor = 0xFFF0F4F8.toInt()   // fresh snow
                pathColor = 0xFFB0B8C0.toInt()         // icy path
                pathEdgeColor = 0xFF90989F.toInt()     // darker ice
                trunkColor = 0xFF5D4037.toInt()        // cold bark
                rockColor = 0xFFA0A8B0.toInt()         // frozen rock
                rockHighlightColor = 0xFFC8D0D8.toInt()
            }
            else -> {
                grassColor = 0xFF3A7D3A.toInt()
                grassLightColor = 0xFF4CAF50.toInt()
                pathColor = 0xFF8D6E63.toInt()
                pathEdgeColor = 0xFF6D4C41.toInt()
                trunkColor = 0xFF795548.toInt()
                rockColor = 0xFF757575.toInt()
                rockHighlightColor = 0xFF9E9E9E.toInt()
            }
        }
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
    private var riverPathCache: Path = Path()
    private var bridgePositions: List<PointF> = emptyList()
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
        color = 0xFFFFD700.toInt(); textSize = 72f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val waveBannerSubPaint = Paint().apply {
        color = 0xFFBDBDBD.toInt(); textSize = 34f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
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
    private var selectedTower: Tower? = null
    // Path arrow drawing
    private val arrowPaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val arrowPath = Path()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    fun getEngine(): GameEngine = engine

    override fun surfaceCreated(holder: SurfaceHolder) {
        engine.init(width.toFloat(), height.toFloat())
        updateThemeColors()
        generateTerrain()
        initialized = true
        synchronized(pendingReadyActions) {
            pendingReadyActions.forEach { it.invoke() }
            pendingReadyActions.clear()
        }
        startGameThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Only update screen dimensions—init() already ran in surfaceCreated
        engine.screenW = width.toFloat()
        engine.screenH = height.toFloat()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameThread()
        initialized = false
        // Clear callbacks to prevent activity memory leak
        onGoldChanged = null
        onWaveChanged = null
        onGameOver = null
        onDiamondsChanged = null
        onTowerSelected = null
        onCampaignVictory = null
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
            synchronized(engine.lock) {
                goldSnap = engine.gold
                waveSnap = engine.wave
                diamondSnap = engine.skillTree.diamonds
                isGameOver = engine.gameOver
                scoreSnap = engine.score
                isVictory = engine.campaignVictory
            }
            post {
                onGoldChanged?.invoke(goldSnap)
                onWaveChanged?.invoke(waveSnap)
                onDiamondsChanged?.invoke(diamondSnap)
                if (isGameOver && !gameOverFired) {
                    gameOverFired = true
                    onGameOver?.invoke(scoreSnap, waveSnap)
                }
                // Victory overlay is drawn; player taps to continue (handled in onTouchEvent)
            }

            // Cap at ~60 fps
            val frameTime = (System.nanoTime() - now) / 1_000_000
            if (frameTime < 16) {
                Thread.sleep(16 - frameTime)
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
        val skyTop = if (night) 0xFF0D1B2A.toInt() else 0xFF87CEEB.toInt()
        val skyBot = if (night) 0xFF1B2838.toInt() else 0xFFB0D4F1.toInt()
        terrainPaint.shader = LinearGradient(
            0f, 0f, 0f, height * 0.14f,
            skyTop, skyBot, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), terrainPaint)
        terrainPaint.shader = null

        // Stars at night
        if (night) {
            paint.color = 0xCCFFFFFF.toInt()
            val starRng = java.util.Random(42L)
            repeat(30) {
                val sx = starRng.nextFloat() * width
                val sy = starRng.nextFloat() * height * 0.12f
                val sr = 1f + starRng.nextFloat() * 1.5f
                canvas.drawCircle(sx, sy, sr, paint)
            }
        }

        // Distant mountains silhouette
        val groundTop0 = height * 0.12f
        terrainPaint.color = if (night) 0xFF37474F.toInt() else 0xFF78909C.toInt()
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
        // Snow caps
        terrainPaint.color = 0xFFFFFFFF.toInt()
        terrainPaint.alpha = if (night) 15 else 25
        canvas.drawCircle(width * 0.30f, groundTop0 - 46f, 10f, terrainPaint)
        canvas.drawCircle(width * 0.80f, groundTop0 - 41f, 9f, terrainPaint)
        canvas.drawCircle(width * 0.55f, groundTop0 - 31f, 7f, terrainPaint)
        terrainPaint.alpha = 255

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
            // Sun glow
            paint.color = 0x18FFD54F
            canvas.drawCircle(width * 0.82f, height * 0.04f, 80f, paint)
            paint.color = 0x30FFD54F
            canvas.drawCircle(width * 0.82f, height * 0.04f, 45f, paint)
            paint.color = 0xDDFFE082.toInt()
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
        val isDesert = engine.mapType == MapType.DESERT
        val isSnow = engine.mapType == MapType.SNOW
        val terrTop = if (night) {
            if (isDesert) 0xFF5A4820.toInt() else if (isSnow) 0xFF506070.toInt() else 0xFF2E5A2E.toInt()
        } else {
            if (isDesert) 0xFFD4B86A.toInt() else if (isSnow) 0xFFE0E8F0.toInt() else 0xFF66BB6A.toInt()
        }
        val terrBot = if (night) {
            if (isDesert) 0xFF3D3010.toInt() else if (isSnow) 0xFF3A4A5A.toInt() else 0xFF1B4D1B.toInt()
        } else {
            if (isDesert) 0xFFA08040.toInt() else if (isSnow) 0xFFB0C0D0.toInt() else 0xFF388E3C.toInt()
        }
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
            terrainPaint.color = if (isDesert) {
                if (hillRng.nextBoolean()) 0xFFC0A050.toInt() else 0xFFB09030.toInt()
            } else if (isSnow) {
                if (hillRng.nextBoolean()) 0xFFD0D8E0.toInt() else 0xFFB8C4D0.toInt()
            } else {
                if (hillRng.nextBoolean()) 0xFF4CAF50.toInt() else 0xFF2E7D32.toInt()
            }
            terrainPaint.alpha = 30 + hillRng.nextInt(25)
            canvas.drawOval(hx - hw, hy - hh / 2, hx + hw, hy + hh / 2, terrainPaint)
        }
        terrainPaint.alpha = 255

        // Castle hill mound
        terrainPaint.color = if (isDesert) 0xFFB89A50.toInt() else if (isSnow) 0xFFC8D4E0.toInt() else 0xFF4CAF50.toInt()
        terrainPaint.alpha = 80
        canvas.drawOval(
            engine.baseX - 130f, engine.baseY - 25f,
            engine.baseX + 130f, engine.baseY + 55f, terrainPaint
        )
        terrainPaint.color = if (isDesert) 0xFFC4A860.toInt() else if (isSnow) 0xFFD8E0E8.toInt() else 0xFF66BB6A.toInt()
        terrainPaint.alpha = 50
        canvas.drawOval(
            engine.baseX - 95f, engine.baseY - 12f,
            engine.baseX + 95f, engine.baseY + 35f, terrainPaint
        )
        terrainPaint.alpha = 255

        // === DIRT PATHS WITH LAYERED DETAIL ===
        for (p in pathCache) {
            pathPaint.style = Paint.Style.STROKE
            // Grass border around path
            pathPaint.color = if (isDesert) 0xFF9E8040.toInt() else if (isSnow) 0xFF90A0B0.toInt() else 0xFF2E7D32.toInt()
            pathPaint.strokeWidth = 58f
            canvas.drawPath(p, pathPaint)
            // Dark dirt edge
            pathPaint.color = pathEdgeColor
            pathPaint.strokeWidth = 48f
            canvas.drawPath(p, pathPaint)
            // Main dirt fill
            pathPaint.color = pathColor
            pathPaint.strokeWidth = 36f
            canvas.drawPath(p, pathPaint)
            // Lighter center tread
            pathPaint.color = if (isDesert) 0xFFD4BA80.toInt() else if (isSnow) 0xFFD0D8E0.toInt() else 0xFFBCAAA4.toInt()
            pathPaint.strokeWidth = 14f
            pathPaint.alpha = 60
            canvas.drawPath(p, pathPaint)
            pathPaint.alpha = 255
        }

        // Pebbles/cobblestones scattered on paths
        val pebbleRng = java.util.Random(42L)
        for (gamePath in engine.paths) {
            val wps = gamePath.waypoints
            for (i in 0 until wps.size - 1) {
                val ax = wps[i].x; val ay = wps[i].y
                val bpx = wps[i + 1].x; val bpy = wps[i + 1].y
                val segLen = Math.sqrt(((bpx - ax) * (bpx - ax) + (bpy - ay) * (bpy - ay)).toDouble()).toFloat()
                val pebbleCount = (segLen / 30f).toInt()
                repeat(pebbleCount) {
                    val t = pebbleRng.nextFloat()
                    val px = ax + t * (bpx - ax) + (pebbleRng.nextFloat() - 0.5f) * 12f
                    val py = ay + t * (bpy - ay) + (pebbleRng.nextFloat() - 0.5f) * 12f
                    val ps = 1.5f + pebbleRng.nextFloat() * 2f
                    terrainPaint.color = if (pebbleRng.nextBoolean()) 0xFF9E9E9E.toInt() else 0xFF8D6E63.toInt()
                    terrainPaint.alpha = 50 + pebbleRng.nextInt(50)
                    canvas.drawCircle(px, py, ps, terrainPaint)
                }
            }
        }
        terrainPaint.alpha = 255

        // === PATH DIRECTION ARROWS (between waves) ===
        if (!engine.waveInProgress && !engine.gameOver && !engine.campaignVictory && engine.wave > 0) {
            drawPathArrows(canvas)
        }

        // === ANIMATED RIVER ===
        drawRiver(canvas)

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
            terrainPaint.color = if (isDesert) 0xFF8B7535.toInt() else if (isSnow) 0xFF4A5A5A.toInt() else 0xFF2E7D32.toInt()
            terrainPaint.alpha = 35 + treeSeed.nextInt(25)
            canvas.drawCircle(tx, gtop + 10f, ts, terrainPaint)
            terrainPaint.color = if (isDesert) 0xFF9E8040.toInt() else if (isSnow) 0xFF5A6A6A.toInt() else 0xFF388E3C.toInt()
            canvas.drawCircle(tx - ts * 0.3f, gtop + 6f, ts * 0.7f, terrainPaint)
        }
        terrainPaint.alpha = 255

        // Decorations (trees, rocks, bushes, flowers)
        drawDecorations(canvas)

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
        if (engine.mapType == MapType.CROSSROADS) {
            paint.color = 0x22FF8A65
            canvas.drawCircle(engine.crossroadsZoneX, engine.crossroadsZoneY, engine.crossroadsKillZoneRadius, paint)
            paint.color = 0x77FFAB91
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(engine.crossroadsZoneX, engine.crossroadsZoneY, engine.crossroadsKillZoneRadius, paint)
            paint.style = Paint.Style.FILL
        }
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
                }
                val glowAlpha = (0x15 * synergyCount).coerceAtMost(0x44)
                paint.color = (glowAlpha shl 24) or synergyColor
                canvas.drawCircle(tower.x, tower.y, tower.size + 12f + synergyCount * 3f, paint)
            }
            if (tower == selectedTower) {
                val displayRange = engine.getDisplayedTowerRange(tower)
                // Pulsing selection ring
                paint.color = 0x3300E5FF
                canvas.drawCircle(tower.x, tower.y, displayRange, paint)
                paint.color = 0x6600E5FF
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(tower.x, tower.y, displayRange, paint)
                paint.style = Paint.Style.FILL
                // Selection glow under tower
                paint.color = 0x4400E5FF
                canvas.drawCircle(tower.x, tower.y, tower.size + 8f, paint)
            }
            val tScale = depthFactor(tower.y)
            canvas.save()
            canvas.scale(tScale, tScale, tower.x, tower.y)
            EntityRenderer.drawTower(canvas, tower, tower == selectedTower)
            canvas.restore()
            // Ice tower aura ring
            if (tower.type == TowerType.ICE) {
                val displayRange = engine.getDisplayedTowerRange(tower)
                paint.color = 0x2281D4FA
                canvas.drawCircle(tower.x, tower.y, displayRange, paint)
                paint.color = 0x5581D4FA
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(tower.x, tower.y, displayRange, paint)
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
        }

        // Enemies (with perspective scale — further up = slightly smaller)
        for (enemy in engine.enemies) {
            val depthScale = depthFactor(enemy.y)
            canvas.save()
            canvas.scale(depthScale, depthScale, enemy.x, enemy.y)
            EntityRenderer.drawEnemy(canvas, enemy, engine.freezeTimer > 0)
            canvas.restore()

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
                // Elite glow ring
                paint.color = 0x33FFD700
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
                canvas.drawRoundRect(bossBarX - 8f, bossBarY - 30f, bossBarX + bossBarW + 8f, bossBarY + bossBarH + 12f, 10f, 10f, paint)
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
                // Boss name
                val bossName = enemy.bossType?.let { "${it.emoji} ${it.displayName}" } ?: "\u2620\uFE0F BOSS"
                bossNamePaint.textSize = 22f; bossNamePaint.color = Color.WHITE
                drawOutlinedText(canvas, bossName, width / 2f, bossBarY - 10f, bossNamePaint)
                // HP text on bar
                val hpText = "${enemy.hp.toInt()} / ${enemy.maxHp.toInt()}"
                bossNamePaint.textSize = 13f; bossNamePaint.color = Color.WHITE
                canvas.drawText(hpText, width / 2f, bossBarY + bossBarH - 3f, bossNamePaint)
                // Ability indicator
                val abilityLabel = enemy.bossType?.ability?.displayName ?: ""
                if (abilityLabel.isNotEmpty()) {
                    bossNamePaint.textSize = 12f; bossNamePaint.color = 0xFFBDBDBD.toInt()
                    canvas.drawText("\u26A0\uFE0F $abilityLabel: ${enemy.bossType?.ability?.shortDescription ?: ""}", width / 2f, bossBarY + bossBarH + 10f, bossNamePaint)
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

        // Player (with perspective)
        val phRatioForDraw = engine.player.hp / engine.player.maxHp
        val pScale = depthFactor(engine.player.y)
        canvas.save()
        canvas.scale(pScale, pScale, engine.player.x, engine.player.y)
        EntityRenderer.drawPlayer(canvas, engine.player.x, engine.player.y, engine.player.size, phRatioForDraw)
        canvas.restore()
        // Player range circle — subtle
        val playerRange =
            engine.player.attackRange *
                (if (engine.currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f) *
                (if (engine.mapType == MapType.DESERT && engine.desertStormDuration > 0f) 0.9f else 1f)
        paint.color = 0x1542A5F5
        canvas.drawCircle(engine.player.x, engine.player.y, playerRange, paint)
        paint.color = 0x3342A5F5
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawCircle(engine.player.x, engine.player.y, playerRange, paint)
        paint.style = Paint.Style.FILL

        // Player HP bar removed — focus on base health only

        // Dash cooldown indicator under player
        if (engine.dashCooldown > 0) {
            val dashRatio = (1f - engine.dashCooldown / engine.dashCooldownMax).coerceIn(0f, 1f)
            val dBarW = 50f
            val dBarX = engine.player.x - dBarW / 2f
            val dBarY = engine.player.y + engine.player.size + 6f
            paint.color = 0x88000000.toInt()
            canvas.drawRoundRect(dBarX, dBarY, dBarX + dBarW, dBarY + 4f, 2f, 2f, paint)
            paint.color = 0xFF00BFA5.toInt()
            canvas.drawRoundRect(dBarX, dBarY, dBarX + dBarW * dashRatio, dBarY + 4f, 2f, 2f, paint)
        }

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
        } else if (engine.waveInProgress) {
            val modTag = if (engine.currentWaveModifier != WaveModifier.NONE) " ${engine.currentWaveModifier.emoji}" else ""
            val bossName = if (engine.isBossRush && engine.currentBoss != null) " \u2014 ${engine.currentBoss!!.displayName}" else ""
            val wText = "${rushTag}${endlessTag}${randTag}Wave ${engine.wave}$modTag$bossName  \u2694\uFE0F  ${engine.enemies.size} enemies"
            drawOutlinedText(canvas, wText, width / 2f, 80f, waveTextPaint)
            engine.getCombatDebuffStatus()?.let { debuffText ->
                previewTextPaint.color = 0xFFFFB300.toInt()
                previewTextPaint.textAlign = Paint.Align.CENTER
                drawOutlinedText(canvas, debuffText, width / 2f, 108f, previewTextPaint)
                previewTextPaint.textAlign = Paint.Align.LEFT
            }

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
        }

        // Wave banner overlay
        if (engine.showWaveBanner) {
            canvas.drawRect(0f, height * 0.32f, width.toFloat(), height * 0.60f, bannerBgPaint)
            val isBoss = engine.wave % engine.bossInterval == 0
            val bossLabel = engine.currentBoss?.let { "${it.emoji} ${it.displayName}" }
            val waveLabel = if (isBoss && bossLabel != null) "$bossLabel" else "\u2694\uFE0F WAVE ${engine.wave} \u2694\uFE0F"
            drawOutlinedText(canvas, waveLabel, width / 2f, height * 0.44f, waveBannerPaint)
            // Show enemy breakdown
            val preview = engine.nextWavePreview
            val totalEnemies = engine.totalEnemiesThisWave
            if (isBoss) {
                drawOutlinedText(canvas, "Wave ${engine.wave}", width / 2f, height * 0.50f, waveBannerSubPaint)
            }
            if (totalEnemies > 0) {
                drawOutlinedText(canvas, "$totalEnemies enemies incoming", width / 2f, height * 0.55f, waveBannerSubPaint)
            }
        }

        // Combo display (top right)
        if (engine.comboCount >= 3) {
            comboPaint.textSize = 28f + engine.comboCount.coerceAtMost(20) * 1.5f
            drawOutlinedText(canvas, "${engine.comboCount}x COMBO", width - 20f, 170f, comboPaint)
            comboMultPaint.textSize = 22f
            drawOutlinedText(
                canvas,
                "x${String.format(Locale.getDefault(), "%.1f", engine.comboMultiplier)} gold",
                width - 20f,
                198f,
                comboMultPaint
            )
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

        drawOutlinedText(canvas, engine.getMapMechanicStatus(), 10f, 201f, previewLabelPaint)

        // Wave preview panel (between waves, bottom-left area above controls)
        val preview = engine.nextWavePreview
        if (preview != null && !engine.waveInProgress && !engine.gameOver) {
            val pvX = 10f
            val pvY = height * 0.62f
            val pvW = width * 0.58f
            val hasModifier = preview.modifier != WaveModifier.NONE
            val modExtra = if (hasModifier) 28f else 0f
            val pvH = if (preview.isBoss) (90f + modExtra) else (55f + preview.enemies.size * 24f + modExtra).coerceAtMost(160f)
            // Panel background with subtle border
            canvas.drawRoundRect(pvX, pvY, pvX + pvW, pvY + pvH, 12f, 12f, previewBgPaint)
            paint.color = 0x33FFFFFF
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(pvX, pvY, pvX + pvW, pvY + pvH, 12f, 12f, paint)
            paint.style = Paint.Style.FILL
            // Header
            val headerY = pvY + 22f
            previewLabelPaint.color = 0xFFFFD54F.toInt()
            previewLabelPaint.textSize = 16f
            canvas.drawText("\u2694\uFE0F Wave ${engine.wave + 1} Preview", pvX + 12f, headerY, previewLabelPaint)
            // Separator line
            paint.color = 0x44FFFFFF
            canvas.drawRect(pvX + 10f, headerY + 6f, pvX + pvW - 10f, headerY + 7f, paint)
            // Modifier tag at top if present
            var contentY = headerY + 24f
            if (hasModifier) {
                paint.color = 0x44FFAB00
                canvas.drawRoundRect(pvX + 8f, contentY - 14f, pvX + pvW - 8f, contentY + 10f, 6f, 6f, paint)
                previewTextPaint.color = 0xFFFFAB00.toInt()
                canvas.drawText("${preview.modifier.emoji} ${preview.modifier.displayName}: ${preview.modifier.description}", pvX + 14f, contentY + 4f, previewTextPaint)
                contentY += 26f
            }
            if (preview.isBoss && preview.bossType != null) {
                previewTextPaint.color = preview.bossType.color
                previewTextPaint.textSize = 15f
                canvas.drawText("${preview.bossType.emoji} ${preview.bossType.displayName}", pvX + 14f, contentY + 4f, previewTextPaint)
                previewTextPaint.textSize = 13f
                previewTextPaint.color = 0xFFBDBDBD.toInt()
                canvas.drawText("\u26A0\uFE0F ${preview.bossType.ability.displayName}: ${preview.bossType.ability.shortDescription}", pvX + 14f, contentY + 22f, previewTextPaint)
            } else {
                val totalCount = preview.enemies.values.sum()
                previewTextPaint.color = 0xFFBDBDBD.toInt()
                canvas.drawText("$totalCount enemies:", pvX + 14f, contentY + 4f, previewTextPaint)
                contentY += 20f
                for ((type, count) in preview.enemies) {
                    if (contentY > pvY + pvH - 8f) break
                    previewTextPaint.color = type.color
                    canvas.drawText("${type.emoji} ${type.name} \u00D7$count", pvX + 22f, contentY, previewTextPaint)
                    contentY += 22f
                }
            }
        }

        // Achievement banner
        val ach = engine.newAchievement
        if (ach != null && engine.achievementBannerTimer > 0) {
            val abY = height * 0.25f
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, achBgPaint)
            canvas.drawRoundRect(width * 0.1f, abY, width * 0.9f, abY + 70f, 12f, 12f, achBorderPaint)
            drawOutlinedText(canvas, "${ach.emoji} Achievement Unlocked!", width / 2f, abY + 28f, achievePaint)
            drawOutlinedText(canvas, "${ach.title} — ${ach.description}", width / 2f, abY + 55f, achDescPaint)
        }

        // Placement mode indicator
        if (placementMode != null) {
            drawOutlinedText(canvas, "Tap to place ${placementMode!!.emoji} tower", width / 2f, 160f, placeTextPaint)
        }

        // FPS display
        if (com.example.myapp.SoundManager.isShowFps(context)) {
            paint.color = 0xAAFFFFFF.toInt()
            paint.textSize = 20f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$displayFps FPS", width - 10f, height - 20f, paint)
            paint.textAlign = Paint.Align.CENTER
        }

        canvas.restore() // End screen shake

        // Pause overlay
        if (engine.isPaused && !engine.gameOver && !engine.campaignVictory) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
            drawOutlinedText(canvas, "\u23F8\uFE0F PAUSED", width / 2f, height / 2f - 100f, gameOverPaint)
            // Run summary
            val summaryPaint = scoreDisplayPaint
            drawOutlinedText(canvas, "Wave: ${engine.wave}  |  Score: ${engine.score}", width / 2f, height / 2f - 30f, summaryPaint)
            drawOutlinedText(canvas, "Kills: ${engine.totalKills}  |  Gold: ${engine.gold}", width / 2f, height / 2f + 15f, summaryPaint)
            drawOutlinedText(canvas, "Towers: ${engine.towers.size}  |  Combo Best: ${engine.bestCombo}x", width / 2f, height / 2f + 55f, summaryPaint)
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, "\uD83D\uDC8E ${engine.diamondsEarnedThisRun} Diamonds", width / 2f, height / 2f + 95f, goDiamondPaint)
            }
            drawOutlinedText(canvas, "Tap \u25B6\uFE0F to resume", width / 2f, height / 2f + 140f, goldTextPaint)
        }

        // Game over overlay (drawn outside shake)
        if (engine.gameOver) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

            if (engine.isBossRush) {
                drawOutlinedText(canvas, "\uD83D\uDC80 BOSS RUSH OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            } else if (engine.isEndlessMode) {
                drawOutlinedText(canvas, "ENDLESS OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            } else if (engine.isRandomizerMode) {
                drawOutlinedText(canvas, "\uD83C\uDFB2 RANDOMIZER OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            } else {
                drawOutlinedText(canvas, "GAME OVER", width / 2f, height / 2f - 80f, gameOverPaint)
            }

            if (engine.isBossRush) {
                drawOutlinedText(canvas, "Bosses defeated: ${engine.bossRushWave}  |  Score: ${engine.score}", width / 2f, height / 2f - 10f, scoreDisplayPaint)
            } else {
                drawOutlinedText(canvas, "Wave: ${engine.wave}  |  Score: ${engine.score}", width / 2f, height / 2f - 10f, scoreDisplayPaint)
            }
            drawOutlinedText(canvas, "Kills: ${engine.totalKills}  |  Best Combo: ${engine.bestCombo}x", width / 2f, height / 2f + 35f, scoreDisplayPaint)

            // Diamonds earned this run
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, "\uD83D\uDC8E +${engine.diamondsEarnedThisRun} Diamonds earned!", width / 2f, height / 2f + 70f, goDiamondPaint)
            }

            if (engine.isBossRush) {
                if (engine.isNewBossRushRecord) {
                    drawOutlinedText(canvas, "\u2B50 NEW BOSS RUSH RECORD! \u2B50", width / 2f, height / 2f + 110f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, "Boss Rush Record: ${engine.bossRushHighWave} bosses", width / 2f, height / 2f + 110f, goHighScorePaint)
                }
            } else if (engine.isEndlessMode) {
                if (engine.isNewEndlessRecord) {
                    drawOutlinedText(canvas, "\u2B50 NEW ENDLESS RECORD! \u2B50", width / 2f, height / 2f + 110f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, "Endless Record: Wave ${engine.endlessHighWave}", width / 2f, height / 2f + 110f, goHighScorePaint)
                }
            } else {
                if (engine.isNewHighScore) {
                    drawOutlinedText(canvas, "\u2B50 NEW HIGH SCORE! \u2B50", width / 2f, height / 2f + 110f, goNewBestPaint)
                } else {
                    drawOutlinedText(canvas, "High Score: ${engine.highScore}  |  Best Wave: ${engine.highWave}", width / 2f, height / 2f + 110f, goHighScorePaint)
                }
            }

            drawOutlinedText(canvas, "Tap to restart", width / 2f, height / 2f + 160f, goldTextPaint)
        }

        // Campaign victory overlay
        if (engine.campaignVictory) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
            val cl = engine.campaignLevel
            drawOutlinedText(canvas, "⭐ VICTORY! ⭐", width / 2f, height / 2f - 80f, victoryPaint)
            if (cl != null) {
                drawOutlinedText(canvas, cl.title, width / 2f, height / 2f - 20f, scoreDisplayPaint)
            }
            drawOutlinedText(canvas, "Wave: ${engine.wave}  |  Score: ${engine.score}", width / 2f, height / 2f + 30f, scoreDisplayPaint)
            if (engine.diamondsEarnedThisRun > 0) {
                drawOutlinedText(canvas, "\uD83D\uDC8E +${engine.diamondsEarnedThisRun} Diamonds!", width / 2f, height / 2f + 70f, goDiamondPaint)
            }
            drawOutlinedText(canvas, "Tap to continue", width / 2f, height / 2f + 130f, goldTextPaint)
        }
    }

    private var isDraggingPlayer = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val tx = event.x
        val ty = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDraggingPlayer = false
                synchronized(engine.lock) {
                    if (engine.campaignVictory) {
                        selectedTower = null
                        post { onCampaignVictory?.invoke() }
                        return true
                    }

                    if (engine.gameOver) {
                        SoundManager.play(SfxType.UI_CLICK)
                        engine.restart()
                        gameOverFired = false
                        selectedTower = null
                        return true
                    }

                    // Tower placement mode
                    if (placementMode != null) {
                        if (engine.placeTower(tx, ty, placementMode!!)) {
                            placementMode = null
                            post { onGoldChanged?.invoke(engine.gold) }
                        }
                        return true
                    }

                    // Check if tapped on a tower (to select it)
                    val tapped = engine.towers.find { it.distanceTo(tx, ty) < it.size + 20f }
                    if (tapped != null) {
                        selectedTower = tapped
                        SoundManager.play(SfxType.UI_CLICK)
                        post { onTowerSelected?.invoke(tapped) }
                        return true
                    }
                    selectedTower = null
                    post { onTowerSelected?.invoke(null) }

                    // Move player (if alive and not on water)
                    if (engine.player.hp > 0 && !engine.isPointOnRiver(tx, ty)) {
                        engine.player.moveTo(tx, ty)
                        isDraggingPlayer = true
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingPlayer) {
                    synchronized(engine.lock) {
                        if (engine.player.hp > 0 && !engine.isPointOnRiver(tx, ty)) {
                            engine.player.moveTo(tx, ty)
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingPlayer = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun pause() {
        stopGameThread()
    }

    fun resume() {
        if (holder.surface.isValid) {
            startGameThread()
        }
    }

    fun whenReady(action: () -> Unit) {
        if (initialized) {
            action()
            return
        }
        synchronized(pendingReadyActions) {
            if (initialized) action() else pendingReadyActions.add(action)
        }
    }

    fun refreshMapPresentation() {
        updateThemeColors()
        generateTerrain()
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

        // Build river canvas path
        riverPathCache = buildSmoothPath(engine.riverWaypoints)

        // Compute bridge positions: where each enemy path actually crosses the river
        val bridges = mutableListOf<PointF>()
        val rw = engine.riverWaypoints
        if (rw.size >= 2) {
            for (gamePath in engine.paths) {
                val wps = gamePath.waypoints
                var found = false
                for (i in 0 until wps.size - 1) {
                    if (found) break
                    val p1x = wps[i].x; val p1y = wps[i].y
                    val p2x = wps[i + 1].x; val p2y = wps[i + 1].y
                    // Check intersection with each river segment
                    for (j in 0 until rw.size - 1) {
                        val r1x = rw[j].x; val r1y = rw[j].y
                        val r2x = rw[j + 1].x; val r2y = rw[j + 1].y
                        // Line-segment intersection test
                        val dx1 = p2x - p1x; val dy1 = p2y - p1y
                        val dx2 = r2x - r1x; val dy2 = r2y - r1y
                        val denom = dx1 * dy2 - dy1 * dx2
                        if (Math.abs(denom) < 0.01f) continue
                        val t1 = ((r1x - p1x) * dy2 - (r1y - p1y) * dx2) / denom
                        val t2 = ((r1x - p1x) * dy1 - (r1y - p1y) * dx1) / denom
                        if (t1 in 0f..1f && t2 in 0f..1f) {
                            bridges.add(PointF(p1x + t1 * dx1, p1y + t1 * dy1))
                            found = true
                            break
                        }
                    }
                }
            }
        }
        bridgePositions = bridges

        // Generate decorations avoiding paths and river
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
            // Skip near river
            val riverY = h * 0.33f
            if (Math.abs(dy - riverY) < 35f) return@repeat
            val type = rng.nextInt(8)
            val scale = 0.7f + rng.nextFloat() * 0.6f
            decos.add(Decoration(dx, dy, type, scale, rng.nextInt()))
        }
        decorations = decos
    }

    /** Build a smooth Canvas Path through waypoints using quadratic curves */
    private fun buildSmoothPath(waypoints: List<PointF>): Path {
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
                4 -> { // Pine tree (tall triangle)
                    terrainPaint.color = trunkColor
                    canvas.drawRect(x - 2f * s, y - 4f * s, x + 2f * s, y + 12f * s, terrainPaint)
                    shadowPaint.alpha = 25
                    canvas.drawOval(x - 8f * s, y + 8f * s, x + 8f * s, y + 14f * s, shadowPaint)
                    terrainPaint.color = 0xFF1B5E20.toInt()
                    val tp = Path()
                    tp.moveTo(x, y - 22f * s)
                    tp.lineTo(x - 10f * s, y - 2f * s)
                    tp.lineTo(x + 10f * s, y - 2f * s)
                    tp.close()
                    canvas.drawPath(tp, terrainPaint)
                    terrainPaint.color = 0xFF2E7D32.toInt()
                    val tp2 = Path()
                    tp2.moveTo(x, y - 18f * s)
                    tp2.lineTo(x - 7f * s, y - 5f * s)
                    tp2.lineTo(x + 7f * s, y - 5f * s)
                    tp2.close()
                    canvas.drawPath(tp2, terrainPaint)
                }
                5 -> { // Mushroom
                    terrainPaint.color = 0xFFE0E0E0.toInt()
                    canvas.drawRect(x - 1.5f * s, y - 3f * s, x + 1.5f * s, y + 4f * s, terrainPaint)
                    terrainPaint.color = 0xFFE53935.toInt()
                    canvas.drawCircle(x, y - 5f * s, 5f * s, terrainPaint)
                    terrainPaint.color = 0xFFFFFFFF.toInt()
                    canvas.drawCircle(x - 2f * s, y - 6f * s, 1.2f * s, terrainPaint)
                    canvas.drawCircle(x + 2f * s, y - 4f * s, 1f * s, terrainPaint)
                }
                6 -> { // Tall grass
                    terrainPaint.color = 0xFF558B2F.toInt()
                    for (g in -2..2) {
                        val gx = x + g * 2f * s
                        val lean = g * 1.5f * s
                        canvas.drawLine(gx, y + 2f * s, gx + lean, y - 10f * s, terrainPaint)
                    }
                }
                7 -> { // Berry bush
                    shadowPaint.alpha = 18
                    canvas.drawOval(x - 9f * s, y + 2f * s, x + 9f * s, y + 6f * s, shadowPaint)
                    terrainPaint.color = 0xFF33691E.toInt()
                    canvas.drawCircle(x - 4f * s, y, 7f * s, terrainPaint)
                    canvas.drawCircle(x + 3f * s, y - 1f * s, 6f * s, terrainPaint)
                    terrainPaint.color = grassLightColor
                    canvas.drawCircle(x, y - 3f * s, 5f * s, terrainPaint)
                    // Berries
                    terrainPaint.color = 0xFFD32F2F.toInt()
                    canvas.drawCircle(x - 3f * s, y - 2f * s, 1.8f * s, terrainPaint)
                    canvas.drawCircle(x + 2f * s, y - 4f * s, 1.5f * s, terrainPaint)
                    canvas.drawCircle(x + 4f * s, y + 1f * s, 1.6f * s, terrainPaint)
                }
            }
        }
    }

    /** Draw the animated river with flowing water ripples and bridges */
    private fun drawRiver(canvas: Canvas) {
        val rw = engine.riverWaypoints
        if (rw.size < 2) return

        // River banks (dark)
        pathPaint.style = Paint.Style.STROKE
        val isDesertRiver = engine.mapType == MapType.DESERT
        val isSnowRiver = engine.mapType == MapType.SNOW
        pathPaint.color = if (isDesertRiver) 0xFF8B6914.toInt() else if (isSnowRiver) 0xFF506070.toInt() else 0xFF1B5E20.toInt()
        pathPaint.strokeWidth = 52f
        canvas.drawPath(riverPathCache, pathPaint)

        // River water body
        pathPaint.color = 0xFF1976D2.toInt()
        pathPaint.strokeWidth = 40f
        canvas.drawPath(riverPathCache, pathPaint)

        // Lighter water center
        pathPaint.color = 0xFF42A5F5.toInt()
        pathPaint.strokeWidth = 24f
        canvas.drawPath(riverPathCache, pathPaint)

        // Animated ripples flowing along the river
        for (i in 0 until 28) {
            val baseT = (i / 28f + waterPhase * 0.12f) % 1f
            val idx = (baseT * (rw.size - 1)).toInt().coerceIn(0, rw.size - 2)
            val localT = (baseT * (rw.size - 1)) - idx
            val rx = rw[idx].x + (rw[idx + 1].x - rw[idx].x) * localT
            val ry = rw[idx].y + (rw[idx + 1].y - rw[idx].y) * localT
            val waveOff = Math.sin((waterPhase * 3f + i * 1.3f).toDouble()).toFloat() * 4f
            paint.color = 0x55FFFFFF
            canvas.drawLine(rx - 7f, ry + waveOff, rx + 7f, ry + waveOff, paint)
            if (i % 3 == 0) {
                paint.color = 0x2290CAF9
                canvas.drawCircle(rx + 3f, ry + waveOff + 2f, 3f, paint)
            }
        }

        // Bridges where paths cross the river
        for (bp in bridgePositions) {
            val bw = 36f
            val bh = 50f
            // Bridge shadow
            shadowPaint.alpha = 25
            canvas.drawOval(bp.x - bw, bp.y + bh * 0.3f, bp.x + bw, bp.y + bh * 0.5f, shadowPaint)
            // Stone arch
            terrainPaint.color = 0xFF5D4037.toInt()
            canvas.drawRoundRect(bp.x - bw, bp.y - bh / 2, bp.x + bw, bp.y + bh / 2, 6f, 6f, terrainPaint)
            // Lighter planks
            terrainPaint.color = 0xFF8D6E63.toInt()
            canvas.drawRoundRect(bp.x - bw + 4f, bp.y - bh / 2 + 3f, bp.x + bw - 4f, bp.y + bh / 2 - 3f, 4f, 4f, terrainPaint)
            // Plank lines
            terrainPaint.color = 0xFF4E342E.toInt()
            terrainPaint.alpha = 80
            var plankX = bp.x - bw + 10f
            while (plankX < bp.x + bw - 8f) {
                canvas.drawLine(plankX, bp.y - bh / 2 + 5f, plankX, bp.y + bh / 2 - 5f, terrainPaint)
                plankX += 9f
            }
            terrainPaint.alpha = 255
            // Rails
            terrainPaint.color = 0xFF3E2723.toInt()
            canvas.drawRect(bp.x - bw, bp.y - bh / 2, bp.x + bw, bp.y - bh / 2 + 3f, terrainPaint)
            canvas.drawRect(bp.x - bw, bp.y + bh / 2 - 3f, bp.x + bw, bp.y + bh / 2, terrainPaint)
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
}
