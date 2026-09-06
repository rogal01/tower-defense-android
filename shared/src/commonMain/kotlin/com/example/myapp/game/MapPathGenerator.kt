package com.example.myapp.game

import java.util.Random

/**
 * Centralized generator for map waypoints and tactical metadata across all 8 biomes.
 * Used by both GameEngine (for active gameplay) and MapPreviewView (for minimap rendering).
 */
object MapPathGenerator {

    /** Helper: jitter a base value by +/- range */
    private fun jitter(rng: Random, base: Float, range: Float): Float =
        base + (rng.nextFloat() * 2f - 1f) * range

    /** Number of active enemy marching lanes for the given map type */
    fun getLaneCount(mapType: MapType): Int = when (mapType) {
        MapType.CLASSIC -> 3
        MapType.VALLEY -> 2
        MapType.CROSSROADS -> 4
        MapType.DESERT -> 2
        MapType.SNOW -> 3
        MapType.LAVA -> 3
        MapType.ENCHANTED -> 3
        MapType.VOLCANO -> 3
    }

    /** Center coordinates of the volcano caldera (if map is Volcano) */
    fun getVolcanoCenter(w: Float, h: Float): Pair<Float, Float> =
        Pair(w * 0.50f, h * 0.40f)

    /** Center coordinates of the crossroads plaza (if map is Crossroads) */
    fun getCrossroadsCenter(w: Float, h: Float): Pair<Float, Float> =
        Pair(w * 0.50f, h * 0.45f)

    /**
     * Generate all paths for a map given dimensions, base sanctuary coordinates, and an RNG.
     */
    fun generate(
        mapType: MapType,
        w: Float,
        h: Float,
        bx: Float,
        by: Float,
        rng: Random = Random(42L)
    ): List<GamePath> {
        val paths = mutableListOf<GamePath>()
        when (mapType) {
            MapType.CLASSIC -> generateClassicPaths(paths, w, h, bx, by, rng)
            MapType.VALLEY -> generateValleyPaths(paths, w, h, bx, by, rng)
            MapType.CROSSROADS -> generateCrossroadsPaths(paths, w, h, bx, by, rng)
            MapType.DESERT -> generateDesertPaths(paths, w, h, bx, by, rng)
            MapType.SNOW -> generateSnowPaths(paths, w, h, bx, by, rng)
            MapType.LAVA -> generateLavaPaths(paths, w, h, bx, by, rng)
            MapType.ENCHANTED -> generateEnchantedPaths(paths, w, h, bx, by, rng)
            MapType.VOLCANO -> generateVolcanoPaths(paths, w, h, bx, by, rng)
        }
        return paths
    }

    private fun generateClassicPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.04f
        // Left path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.08f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.26f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
        // Center path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.46f, w * j), jitter(rng, h * 0.09f, h * j)),
            GamePoint(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        // Right path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.92f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.72f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.74f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateValleyPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.05f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.08f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.62f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.35f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateCrossroadsPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.03f
        val (cx, cy) = getCrossroadsCenter(w, h)
        val jcx = jitter(rng, cx, w * 0.04f)
        val jcy = jitter(rng, cy, h * 0.03f)

        // North entry
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        // West entry
        paths.add(GamePath(listOf(
            GamePoint(-40f, jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.12f, w * j), jitter(rng, h * 0.38f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        // East entry
        paths.add(GamePath(listOf(
            GamePoint(w + 40f, jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.88f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        // North-East flank entry
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jcx, jcy),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateDesertPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.04f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.20f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.58f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.80f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.45f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.62f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.75f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateSnowPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.035f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.15f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.46f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.85f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateLavaPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.04f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.10f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.08f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.18f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.58f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.22f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.54f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateEnchantedPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.045f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.05f, w * j), jitter(rng, h * 0.05f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.35f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.95f, w * j), jitter(rng, h * 0.05f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.35f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.60f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.76f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateVolcanoPaths(paths: MutableList<GamePath>, w: Float, h: Float, bx: Float, by: Float, rng: Random) {
        val j = 0.04f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.10f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.22f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.55f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
    }
}
