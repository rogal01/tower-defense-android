package com.example.myapp.game

import java.util.Random

/**
 * Types of strategic terrain obstacles that block tower placement.
 */
enum class ObstacleType {
    BOULDER,
    CHASM,
    RUINS
}

/**
 * A natural terrain obstacle zone that blocks tower construction.
 */
data class ObstacleZone(
    val x: Float,
    val y: Float,
    val radius: Float,
    val type: ObstacleType
) {
    fun contains(px: Float, py: Float, margin: Float = 0f): Boolean {
        val dx = px - x
        val dy = py - y
        val r = radius + margin
        return dx * dx + dy * dy < r * r
    }
}

/**
 * Procedural path topologies offering varied strategic routes and chokepoints.
 */
enum class PathTopology(
    val id: String,
    val displayNamePl: String,
    val displayNameEn: String,
    val emoji: String,
    val laneCount: Int
) {
    DEFAULT("default", "Biom Standard", "Biome Standard", "⚡", 3),
    S_CURVE("s_curve", "Meander S-Curve", "Serpentine S-Curve", "🐍", 2),
    DUAL_FORK("dual_fork", "Most i Rozwidlenie", "Fork & Chokepoint Bridge", "🌉", 2),
    CROSSFIRE("crossfire", "Skrzyżowanie Ognia", "Crossfire Intersection", "⚔️", 2),
    SPIRAL("spiral", "Spirala Cytadeli", "Citadel Spiral", "🌀", 2),
    ZIGZAG("zigzag", "Górski Zygzak", "Zig-Zag Switchback", "⚡", 2)
}

/**
 * A bridge structure spanning across water or chasms on the battlefield.
 */
data class BridgeZone(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isStone: Boolean = false
)

/**
 * Encapsulates paths, tactical terrain obstacles, and bridges for a generated map.
 */
data class MapLayout(
    val paths: List<GamePath>,
    val obstacles: List<ObstacleZone>,
    val bridges: List<BridgeZone> = emptyList(),
    val topology: PathTopology = PathTopology.DEFAULT
)

/**
 * Centralized generator for map waypoints and tactical metadata across all 8 biomes.
 * Used by GameEngine (for active gameplay), GameView, and MapPreviewView (for minimap rendering).
 */
object MapPathGenerator {

    /** Helper: jitter a base value by +/- range */
    private fun jitter(rng: Random, base: Float, range: Float): Float =
        base + (rng.nextFloat() * 2f - 1f) * range

    /** Number of active enemy marching lanes for the given map type and topology */
    fun getLaneCount(mapType: MapType, topology: PathTopology = PathTopology.DEFAULT): Int =
        if (topology == PathTopology.DEFAULT) {
            when (mapType) {
                MapType.CLASSIC -> 3
                MapType.VALLEY -> 2
                MapType.CROSSROADS -> 4
                MapType.DESERT -> 2
                MapType.SNOW -> 3
                MapType.LAVA -> 3
                MapType.ENCHANTED -> 3
                MapType.VOLCANO -> 3
            }
        } else {
            topology.laneCount
        }

    /** Center coordinates of the volcano caldera (if map is Volcano) */
    fun getVolcanoCenter(w: Float, h: Float): Pair<Float, Float> =
        Pair(w * 0.50f, h * 0.40f)

    /** Center coordinates of the crossroads plaza (if map is Crossroads) */
    fun getCrossroadsCenter(w: Float, h: Float): Pair<Float, Float> =
        Pair(w * 0.50f, h * 0.45f)

    /**
     * Backward-compatible path generator returning only the list of paths.
     */
    fun generate(
        mapType: MapType,
        w: Float,
        h: Float,
        bx: Float,
        by: Float,
        rng: Random = Random(42L)
    ): List<GamePath> = generateLayout(mapType, w, h, bx, by, rng).paths

    /**
     * Generate obstacles for a map type without needing full path resolution.
     */
    fun generateObstacles(
        mapType: MapType,
        w: Float,
        h: Float,
        rng: Random = Random(42L)
    ): List<ObstacleZone> = generateLayout(mapType, w, h, w * 0.5f, h * 0.9f, rng).obstacles

    /**
     * Generate full map layout (paths + terrain obstacle zones + bridges) with optional topology.
     */
    fun generateLayout(
        mapType: MapType,
        w: Float,
        h: Float,
        bx: Float,
        by: Float,
        rng: Random = Random(42L),
        topology: PathTopology = PathTopology.DEFAULT
    ): MapLayout {
        val paths = mutableListOf<GamePath>()
        val obstacles = mutableListOf<ObstacleZone>()
        val bridges = mutableListOf<BridgeZone>()

        if (topology != PathTopology.DEFAULT) {
            when (topology) {
                PathTopology.S_CURVE -> generateSCurve(paths, obstacles, bridges, w, h, bx, by, rng)
                PathTopology.DUAL_FORK -> generateDualFork(paths, obstacles, bridges, w, h, bx, by, rng)
                PathTopology.CROSSFIRE -> generateCrossfire(paths, obstacles, bridges, w, h, bx, by, rng)
                PathTopology.SPIRAL -> generateSpiral(paths, obstacles, bridges, w, h, bx, by, rng)
                PathTopology.ZIGZAG -> generateZigZag(paths, obstacles, bridges, w, h, bx, by, rng)
                PathTopology.DEFAULT -> {}
            }
        } else {
            when (mapType) {
                MapType.CLASSIC -> generateClassic(paths, obstacles, w, h, bx, by, rng)
                MapType.VALLEY -> generateValley(paths, obstacles, w, h, bx, by, rng)
                MapType.CROSSROADS -> generateCrossroads(paths, obstacles, w, h, bx, by, rng)
                MapType.DESERT -> generateDesert(paths, obstacles, w, h, bx, by, rng)
                MapType.SNOW -> generateSnow(paths, obstacles, w, h, bx, by, rng)
                MapType.LAVA -> generateLava(paths, obstacles, w, h, bx, by, rng)
                MapType.ENCHANTED -> generateEnchanted(paths, obstacles, w, h, bx, by, rng)
                MapType.VOLCANO -> generateVolcano(paths, obstacles, w, h, bx, by, rng)
            }
        }
        return MapLayout(paths, obstacles, bridges, topology)
    }

    // =========================================================================
    // 1. CLASSIC — Verdant Plains (Flanking Approaches & Central Kingsroad)
    // =========================================================================
    private fun generateClassic(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        // West flank grove
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.12f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.26f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.18f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.82f, h * j)),
            GamePoint(bx, by)
        )))
        // Central sweeping road
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.42f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        // East flank ridge
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.88f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.74f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.82f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.82f, h * j)),
            GamePoint(bx, by)
        )))

        // Tactical terrain obstacles (boulders and ruins creating natural chokepoints)
        obs.add(ObstacleZone(w * 0.35f, h * 0.34f, 32f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.65f, h * 0.34f, 32f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.48f, h * 0.56f, 36f, ObstacleType.RUINS))
    }

    // =========================================================================
    // 2. VALLEY — Canyon Switchbacks (Tight Hairpin Turns)
    // =========================================================================
    private fun generateValley(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        // Primary deep hairpin switchback
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.26f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.60f, h * j)),
            GamePoint(jitter(rng, w * 0.26f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))
        // Secondary gorge path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.35f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(jitter(rng, w * 0.68f, w * j), jitter(rng, h * 0.76f, h * j)),
            GamePoint(bx, by)
        )))

        // Massive canyon boulders lining the hairpin bends
        obs.add(ObstacleZone(w * 0.50f, h * 0.20f, 38f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.50f, h * 0.38f, 38f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.50f, h * 0.54f, 38f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.88f, h * 0.38f, 28f, ObstacleType.CHASM))
    }

    // =========================================================================
    // 3. CROSSROADS — Grand Nexus Roundabout (4 Multi-vector Entries)
    // =========================================================================
    private fun generateCrossroads(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        val (cx, cy) = getCrossroadsCenter(w, h)
        val jcx = jitter(rng, cx, w * 0.02f)
        val jcy = jitter(rng, cy, h * 0.02f)

        // North entry
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.46f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.54f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        // West gate entry
        paths.add(GamePath(listOf(
            GamePoint(-40f, jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.46f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        // East gate entry
        paths.add(GamePath(listOf(
            GamePoint(w + 40f, jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.54f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        // North-East flank highway
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.74f, w * j), jitter(rng, h * 0.18f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))

        // Four fortified quadrant ruins surrounding the central plaza
        obs.add(ObstacleZone(w * 0.24f, h * 0.22f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.76f, h * 0.22f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.22f, h * 0.64f, 30f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.78f, h * 0.64f, 30f, ObstacleType.RUINS))
    }

    // =========================================================================
    // 4. DESERT — Dunes & Dry Wadi Chokepoints
    // =========================================================================
    private fun generateDesert(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.20f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.32f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.72f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.68f, w * j), jitter(rng, h * 0.60f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.80f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.68f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.46f, h * j)),
            GamePoint(jitter(rng, w * 0.38f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(jitter(rng, w * 0.62f, w * j), jitter(rng, h * 0.76f, h * j)),
            GamePoint(bx, by)
        )))

        // Quicksand chasms and sandstone megaliths
        obs.add(ObstacleZone(w * 0.50f, h * 0.22f, 35f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.50f, h * 0.48f, 36f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.18f, h * 0.58f, 30f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.82f, h * 0.58f, 30f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 5. SNOW — Glacial Crevasse Bridges
    // =========================================================================
    private fun generateSnow(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        // West frozen trail
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.15f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.12f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.32f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        // Central ice bridge
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(bx, by)
        )))
        // East glacier trail
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.85f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.88f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.72f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.68f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))

        // Deep icy chasms and permafrost monoliths
        obs.add(ObstacleZone(w * 0.36f, h * 0.40f, 34f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.64f, h * 0.40f, 34f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.50f, h * 0.22f, 30f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 6. LAVA — Basalt Bridges & Caldera Chokepoints
    // =========================================================================
    private fun generateLava(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.12f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.24f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.62f, h * j)),
            GamePoint(jitter(rng, w * 0.48f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.88f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.76f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.26f, h * j)),
            GamePoint(jitter(rng, w * 0.24f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.76f, w * j), jitter(rng, h * 0.54f, h * j)),
            GamePoint(jitter(rng, w * 0.42f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.42f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.54f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))

        // Lava chasms and volcanic basalt ridges
        obs.add(ObstacleZone(w * 0.32f, h * 0.32f, 36f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.68f, h * 0.32f, 36f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.50f, h * 0.62f, 34f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 7. ENCHANTED — Fey Forest Spiral & Ancient Glade
    // =========================================================================
    private fun generateEnchanted(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.08f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.14f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.92f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.86f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.42f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.62f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.46f, h * j)),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(bx, by)
        )))

        // Mystical monoliths and enchanted forest ruins
        obs.add(ObstacleZone(w * 0.34f, h * 0.26f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.66f, h * 0.26f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.50f, h * 0.50f, 34f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.30f, h * 0.62f, 28f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 8. VOLCANO — Caldera Funnel Gorge
    // =========================================================================
    private fun generateVolcano(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.025f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.12f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.18f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.24f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.18f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.88f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.82f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.76f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.82f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.52f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(bx, by)
        )))

        // Volcanic caldera vents and obsidian boulders
        obs.add(ObstacleZone(w * 0.28f, h * 0.42f, 32f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.72f, h * 0.42f, 32f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.50f, h * 0.20f, 32f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 9. TOPOLOGY: S-CURVE (Serpentine Meander & Center Flank Pockets)
    // =========================================================================
    private fun generateSCurve(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        bridges: MutableList<BridgeZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        // Primary sweeping S-path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.15f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.82f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(jitter(rng, w * 0.86f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.14f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))

        // Secondary counter-weaving serpentine path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.85f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.18f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(jitter(rng, w * 0.14f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.86f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))

        // Obstacle clusters in loop pockets
        obs.add(ObstacleZone(w * 0.50f, h * 0.26f, 32f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.50f, h * 0.50f, 34f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.50f, h * 0.74f, 30f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.06f, h * 0.36f, 26f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.94f, h * 0.50f, 26f, ObstacleType.CHASM))
    }

    // =========================================================================
    // 10. TOPOLOGY: DUAL FORK (Two Armies Converging on a Chokepoint Bridge)
    // =========================================================================
    private fun generateDualFork(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        bridges: MutableList<BridgeZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        val bridgeX = w * 0.50f
        val bridgeY = h * 0.70f

        // West Flank Army
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.18f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.18f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.32f, w * j), jitter(rng, h * 0.54f, h * j)),
            GamePoint(bridgeX, bridgeY - 30f),
            GamePoint(bridgeX, bridgeY + 35f),
            GamePoint(bx, by)
        )))

        // East Flank Army
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.82f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.18f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.68f, w * j), jitter(rng, h * 0.54f, h * j)),
            GamePoint(bridgeX, bridgeY - 30f),
            GamePoint(bridgeX, bridgeY + 35f),
            GamePoint(bx, by)
        )))

        // Central Fortified Bridge spanning across the chasm / river gorge
        bridges.add(BridgeZone(x = bridgeX, y = bridgeY, width = 64f, height = 75f, isStone = true))

        // Chasm / River terrain dividing upper battlefield
        obs.add(ObstacleZone(w * 0.50f, h * 0.32f, 44f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.26f, bridgeY, 32f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.74f, bridgeY, 32f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.10f, bridgeY, 28f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.90f, bridgeY, 28f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 11. TOPOLOGY: CROSSFIRE (X-Pattern Central Intersection Killzone)
    // =========================================================================
    private fun generateCrossfire(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        bridges: MutableList<BridgeZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        val ix = w * 0.50f
        val iy = h * 0.44f

        // NW to SE Cross lane
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.12f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(ix, iy),
            GamePoint(jitter(rng, w * 0.78f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))

        // NE to SW Cross lane
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.88f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(ix, iy),
            GamePoint(jitter(rng, w * 0.22f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))

        bridges.add(BridgeZone(x = ix, y = iy, width = 60f, height = 60f, isStone = true))

        obs.add(ObstacleZone(w * 0.22f, h * 0.38f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.78f, h * 0.38f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.22f, h * 0.52f, 32f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.78f, h * 0.52f, 32f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 12. TOPOLOGY: SPIRAL (Citadel High-Ground Orbital March)
    // =========================================================================
    private fun generateSpiral(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        bridges: MutableList<BridgeZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        // Clockwise orbital march
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.88f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.12f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.38f, h * j)),
            GamePoint(jitter(rng, w * 0.34f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.42f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))

        // Counter-orbital march
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.12f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jitter(rng, w * 0.88f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.38f, h * j)),
            GamePoint(jitter(rng, w * 0.66f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))

        // Citadel plateau monuments
        obs.add(ObstacleZone(w * 0.50f, h * 0.32f, 30f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.50f, h * 0.56f, 32f, ObstacleType.RUINS))
        obs.add(ObstacleZone(w * 0.30f, h * 0.24f, 26f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.70f, h * 0.24f, 26f, ObstacleType.BOULDER))
    }

    // =========================================================================
    // 13. TOPOLOGY: ZIGZAG (Alpine Canyon Switchback)
    // =========================================================================
    private fun generateZigZag(
        paths: MutableList<GamePath>,
        obs: MutableList<ObstacleZone>,
        bridges: MutableList<BridgeZone>,
        w: Float, h: Float, bx: Float, by: Float, rng: Random
    ) {
        val j = 0.02f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.20f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.24f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))

        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.80f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.16f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.52f, h * j)),
            GamePoint(jitter(rng, w * 0.76f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))

        obs.add(ObstacleZone(w * 0.50f, h * 0.24f, 36f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.50f, h * 0.43f, 36f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.50f, h * 0.61f, 36f, ObstacleType.BOULDER))
        obs.add(ObstacleZone(w * 0.90f, h * 0.34f, 26f, ObstacleType.CHASM))
        obs.add(ObstacleZone(w * 0.10f, h * 0.52f, 26f, ObstacleType.CHASM))
    }
}
