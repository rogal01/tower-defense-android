package com.example.myapp.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Random

/**
 * A lightweight, hardware-accelerated Canvas View that renders a crisp 2D minimap
 * preview of any battlefield biome and its marching paths.
 */
class MapPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentMapType: MapType = MapType.CLASSIC
    private var theme: MapTheme = MapTheme.forType(currentMapType)

    // Cached paths & geometries
    private val cachedPaths = mutableListOf<Path>()
    private val cachedObstacles = mutableListOf<ObstacleZone>()
    private val spawnPoints = mutableListOf<PointF>()
    private var basePoint = PointF(0f, 0f)
    private val cardClipPath = Path()
    private val cornerRadius = 24f

    // Pre-allocated Paints
    private val bgPaint = Paint().apply { isAntiAlias = true }
    private val pathBorderPaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val pathEdgePaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val pathFillPaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val pathCenterPaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val landmarkPaint = Paint().apply { isAntiAlias = true }
    private val portalPaint = Paint().apply { isAntiAlias = true }
    private val fortressPaint = Paint().apply { isAntiAlias = true }
    private val framePaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 3f; color = 0x66FFD700
    }
    private val badgeBgPaint = Paint().apply { isAntiAlias = true; color = 0xCC0D1B2A.toInt() }
    private val badgeTextPaint = Paint().apply {
        isAntiAlias = true; color = 0xFFFFD700.toInt(); textSize = 26f; typeface = Typeface.DEFAULT_BOLD
    }

    init {
        setWillNotDraw(false)
    }

    fun setMapType(type: MapType) {
        if (currentMapType != type || cachedPaths.isEmpty()) {
            currentMapType = type
            theme = MapTheme.forType(type)
            rebuildPaths()
            invalidate()
        }
    }

    fun getMapType(): MapType = currentMapType

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            cardClipPath.reset()
            cardClipPath.addRoundRect(
                0f, 0f, w.toFloat(), h.toFloat(),
                cornerRadius, cornerRadius, Path.Direction.CW
            )
            rebuildPaths()
        }
    }

    private fun rebuildPaths() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        cachedPaths.clear()
        spawnPoints.clear()
        cachedObstacles.clear()

        val bx = w * 0.50f
        val by = h * 0.88f
        basePoint.set(bx, by)

        // Generate deterministic canonical waypoints and obstacles for this map preview (seed 42)
        val layout = MapPathGenerator.generateLayout(currentMapType, w, h, bx, by, Random(42L))
        val gamePaths = layout.paths
        cachedObstacles.addAll(layout.obstacles)
        for (gp in gamePaths) {
            val p = Path()
            val wps = gp.waypoints
            if (wps.isNotEmpty()) {
                // Clamp spawn point to top edge of preview for clean appearance
                val startX = wps[0].x.coerceIn(8f, w - 8f)
                val startY = wps[0].y.coerceAtLeast(8f)
                spawnPoints.add(PointF(startX, startY))

                p.moveTo(startX, startY)
                if (wps.size == 2) {
                    p.lineTo(wps[1].x, wps[1].y)
                } else {
                    for (i in 1 until wps.size - 1) {
                        val curr = wps[i]
                        val next = wps[i + 1]
                        val mx = (curr.x + next.x) / 2f
                        val my = (curr.y + next.y) / 2f
                        p.quadTo(curr.x, curr.y, mx, my)
                    }
                    val last = wps.last()
                    p.lineTo(last.x, last.y)
                }
            }
            cachedPaths.add(p)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        canvas.save()
        canvas.clipPath(cardClipPath)

        val skyHeight = h * 0.20f

        // 1. Sky Gradient
        bgPaint.shader = LinearGradient(
            0f, 0f, 0f, skyHeight,
            theme.skyTopDay, theme.skyBotDay, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, skyHeight, bgPaint)
        bgPaint.shader = null

        // Sun / Moon / Celestial Orb in sky
        landmarkPaint.color = (theme.sunGlowColor and 0x00FFFFFF) or 0x30000000
        canvas.drawCircle(w * 0.82f, skyHeight * 0.40f, 22f, landmarkPaint)
        landmarkPaint.color = theme.sunColor
        canvas.drawCircle(w * 0.82f, skyHeight * 0.40f, 9f, landmarkPaint)

        // Distant mountains silhouette
        landmarkPaint.color = theme.mountainColorDay
        val mtPath = Path()
        mtPath.moveTo(0f, skyHeight)
        mtPath.lineTo(w * 0.15f, skyHeight - 14f)
        mtPath.lineTo(w * 0.35f, skyHeight + 4f)
        mtPath.lineTo(w * 0.50f, skyHeight - 20f)
        mtPath.lineTo(w * 0.70f, skyHeight + 2f)
        mtPath.lineTo(w * 0.85f, skyHeight - 16f)
        mtPath.lineTo(w, skyHeight)
        mtPath.close()
        canvas.drawPath(mtPath, landmarkPaint)

        if (theme.hasSnowCaps) {
            landmarkPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(w * 0.50f, skyHeight - 18f, 4.5f, landmarkPaint)
            canvas.drawCircle(w * 0.15f, skyHeight - 12f, 3.5f, landmarkPaint)
            canvas.drawCircle(w * 0.85f, skyHeight - 14f, 3.5f, landmarkPaint)
        } else theme.mountainHighlightColor?.let { hlColor ->
            landmarkPaint.color = hlColor
            canvas.drawCircle(w * 0.50f, skyHeight - 18f, 3.5f, landmarkPaint)
            canvas.drawCircle(w * 0.15f, skyHeight - 12f, 2.5f, landmarkPaint)
            canvas.drawCircle(w * 0.85f, skyHeight - 14f, 2.5f, landmarkPaint)
        }

        // 2. Terrain Gradient
        bgPaint.shader = LinearGradient(
            0f, skyHeight, 0f, h,
            theme.groundTopDay, theme.groundBotDay, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, skyHeight, w, h, bgPaint)
        bgPaint.shader = null

        // Rolling hill accents for depth
        landmarkPaint.color = theme.hillColor1
        landmarkPaint.alpha = 45
        canvas.drawOval(w * 0.1f, h * 0.35f, w * 0.6f, h * 0.50f, landmarkPaint)
        landmarkPaint.color = theme.hillColor2
        canvas.drawOval(w * 0.45f, h * 0.55f, w * 0.95f, h * 0.72f, landmarkPaint)
        landmarkPaint.alpha = 255

        // 3. Biome Landmarks
        if (theme.hasCrater) {
            // Volcano caldera in center
            val vc = MapPathGenerator.getVolcanoCenter(w, h)
            val vcx = vc.first
            val vcy = vc.second
            landmarkPaint.color = 0xFF263238.toInt()
            val cone = Path()
            cone.moveTo(vcx, vcy - 30f)
            cone.lineTo(vcx - 44f, vcy + 22f)
            cone.lineTo(vcx + 44f, vcy + 22f)
            cone.close()
            canvas.drawPath(cone, landmarkPaint)
            // Magma pool
            landmarkPaint.color = 0xFFFF3D00.toInt()
            canvas.drawOval(vcx - 16f, vcy - 24f, vcx + 16f, vcy - 14f, landmarkPaint)
            landmarkPaint.color = 0xFFFFD54F.toInt()
            canvas.drawOval(vcx - 9f, vcy - 22f, vcx + 9f, vcy - 16f, landmarkPaint)
        } else if (theme.hasCrossroadsSquare) {
            // Crossroads central stone plaza
            val cc = MapPathGenerator.getCrossroadsCenter(w, h)
            val cx = cc.first
            val cy = cc.second
            landmarkPaint.color = 0xAA263238.toInt()
            canvas.drawCircle(cx, cy, 32f, landmarkPaint)
            landmarkPaint.color = 0xCC455A64.toInt()
            canvas.drawCircle(cx, cy, 25f, landmarkPaint)
            landmarkPaint.style = Paint.Style.STROKE
            landmarkPaint.strokeWidth = 2f
            landmarkPaint.color = 0x9990A4AE.toInt()
            canvas.drawCircle(cx, cy, 18f, landmarkPaint)
            landmarkPaint.style = Paint.Style.FILL
        }

        // 3.5 Terrain Obstacles Preview
        for (obs in cachedObstacles) {
            val ox = obs.x
            val oy = obs.y
            val r = obs.radius * 0.65f
            when (obs.type) {
                ObstacleType.BOULDER -> {
                    landmarkPaint.color = 0xFF455A64.toInt()
                    canvas.drawCircle(ox, oy, r, landmarkPaint)
                    landmarkPaint.color = 0xFF78909C.toInt()
                    canvas.drawCircle(ox - r * 0.3f, oy - r * 0.3f, r * 0.4f, landmarkPaint)
                }
                ObstacleType.CHASM -> {
                    landmarkPaint.color = 0xFF0A0D14.toInt()
                    canvas.drawOval(ox - r * 1.1f, oy - r * 0.4f, ox + r * 1.1f, oy + r * 0.4f, landmarkPaint)
                }
                ObstacleType.RUINS -> {
                    landmarkPaint.color = 0xFF546E7A.toInt()
                    canvas.drawRoundRect(ox - r, oy - r * 0.5f, ox + r, oy + r * 0.5f, 2f, 2f, landmarkPaint)
                }
            }
        }

        // 4. Paths with Layered Stroke
        pathBorderPaint.color = theme.pathBorderColor
        pathBorderPaint.strokeWidth = 26f
        for (p in cachedPaths) canvas.drawPath(p, pathBorderPaint)

        pathEdgePaint.color = theme.pathEdgeColor
        pathEdgePaint.strokeWidth = 20f
        for (p in cachedPaths) canvas.drawPath(p, pathEdgePaint)

        pathFillPaint.color = theme.pathColor
        pathFillPaint.strokeWidth = 14f
        for (p in cachedPaths) canvas.drawPath(p, pathFillPaint)

        pathCenterPaint.color = theme.pathCenterColor
        pathCenterPaint.strokeWidth = 3f
        pathCenterPaint.alpha = 150
        for (p in cachedPaths) canvas.drawPath(p, pathCenterPaint)

        // 5. Spawn Portals (red glowing nodes at path heads)
        for (sp in spawnPoints) {
            portalPaint.color = 0x44FF1744.toInt()
            canvas.drawCircle(sp.x, sp.y, 14f, portalPaint)
            portalPaint.color = 0x88FF5252.toInt()
            canvas.drawCircle(sp.x, sp.y, 9f, portalPaint)
            portalPaint.color = 0xFFFF1744.toInt()
            canvas.drawCircle(sp.x, sp.y, 5f, portalPaint)
            portalPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(sp.x, sp.y, 2f, portalPaint)
        }

        // 6. Sanctuary Base Fortress (at bottom destination)
        val bx = basePoint.x
        val by = basePoint.y
        // Base hill pedestal
        fortressPaint.color = theme.castleMoundColor1
        canvas.drawOval(bx - 36f, by - 10f, bx + 36f, by + 18f, fortressPaint)

        // Fortress shield/icon
        fortressPaint.color = 0xFF37474F.toInt()
        val baseShield = Path()
        baseShield.moveTo(bx - 18f, by - 16f)
        baseShield.lineTo(bx + 18f, by - 16f)
        baseShield.lineTo(bx + 18f, by + 4f)
        baseShield.lineTo(bx, by + 18f)
        baseShield.lineTo(bx - 18f, by + 4f)
        baseShield.close()
        canvas.drawPath(baseShield, fortressPaint)

        // Golden fortress emblem
        fortressPaint.color = 0xFFFFD700.toInt()
        canvas.drawCircle(bx, by - 2f, 6f, fortressPaint)
        fortressPaint.color = 0xFF00E5FF.toInt()
        canvas.drawCircle(bx, by - 2f, 3.5f, fortressPaint)

        // 7. Lane count badge in top-left corner
        val lanes = MapPathGenerator.getLaneCount(currentMapType)
        val laneText = "$lanes LANES"
        val badgeW = badgeTextPaint.measureText(laneText) + 24f
        val badgeH = 34f
        canvas.drawRoundRect(14f, 14f, 14f + badgeW, 14f + badgeH, 12f, 12f, badgeBgPaint)
        canvas.drawText(laneText, 26f, 14f + badgeH * 0.72f, badgeTextPaint)

        canvas.restore()

        // 8. Outer Gold/Silver Border Frame
        canvas.drawRoundRect(
            1.5f, 1.5f, w - 1.5f, h - 1.5f,
            cornerRadius, cornerRadius, framePaint
        )
    }
}
