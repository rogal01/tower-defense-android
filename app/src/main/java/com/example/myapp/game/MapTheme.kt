package com.example.myapp.game

/**
 * Atmospheric particle styles for biomes.
 */
enum class AmbientParticleType {
    LEAVES,         // Classic meadow fluttering leaves & night fireflies
    CANYON_WIND,    // Valley mountain dust trails & falling pine needles
    MIST_BANNERS,   // Crossroads floating mist & stone dust
    DESERT_DUST,    // Desert heat shimmer & drifting sand motes
    BLIZZARD_SNOW,  // Snow drifting flakes & blizzard wind streaks
    LAVA_EMBERS,    // Lava rising glowing fiery embers & smoke puffs
    FAIRY_SPARKS,   // Enchanted floating luminescent fairy motes (cyan/magenta/gold)
    VOLCANO_ASH     // Volcano falling ash flakes, smoke billows & cinder sparks
}

/**
 * Complete visual palette and atmospheric styling for a map biome.
 */
data class MapTheme(
    val mapType: MapType,
    // Sky
    val skyTopDay: Int,
    val skyBotDay: Int,
    val skyTopNight: Int,
    val skyBotNight: Int,
    val sunColor: Int,
    val sunGlowColor: Int,
    // Mountains
    val mountainColorDay: Int,
    val mountainColorNight: Int,
    val hasSnowCaps: Boolean = true,
    val mountainHighlightColor: Int? = null,
    // Terrain
    val groundTopDay: Int,
    val groundBotDay: Int,
    val groundTopNight: Int,
    val groundBotNight: Int,
    val hillColor1: Int,
    val hillColor2: Int,
    val castleMoundColor1: Int,
    val castleMoundColor2: Int,
    // Paths
    val pathBorderColor: Int,
    val pathEdgeColor: Int,
    val pathColor: Int,
    val pathCenterColor: Int,
    val cobblestone1: Int,
    val cobblestone2: Int,
    val cobblestoneHighlight: Int,
    // Foliage & Props
    val grassColor: Int,
    val grassLightColor: Int,
    val trunkColor: Int,
    val foliageColor1: Int,
    val foliageColor2: Int,
    val rockColor: Int,
    val rockHighlightColor: Int,
    val treelineColor1: Int,
    val treelineColor2: Int,
    // Atmosphere
    val ambientParticleType: AmbientParticleType,
    val hasCrater: Boolean = false,
    val hasCrossroadsSquare: Boolean = false
) {
    companion object {
        fun forType(mapType: MapType): MapTheme = when (mapType) {
            MapType.CLASSIC -> MapTheme(
                mapType = MapType.CLASSIC,
                skyTopDay = 0xFF87CEEB.toInt(),
                skyBotDay = 0xFFB0D4F1.toInt(),
                skyTopNight = 0xFF0D1B2A.toInt(),
                skyBotNight = 0xFF1B2838.toInt(),
                sunColor = 0xFFFFE082.toInt(),
                sunGlowColor = 0xFFFFD54F.toInt(),
                mountainColorDay = 0xFF78909C.toInt(),
                mountainColorNight = 0xFF37474F.toInt(),
                hasSnowCaps = true,
                mountainHighlightColor = 0xFFFFFFFF.toInt(),
                groundTopDay = 0xFF66BB6A.toInt(),
                groundBotDay = 0xFF388E3C.toInt(),
                groundTopNight = 0xFF2E5A2E.toInt(),
                groundBotNight = 0xFF1B4D1B.toInt(),
                hillColor1 = 0xFF4CAF50.toInt(),
                hillColor2 = 0xFF2E7D32.toInt(),
                castleMoundColor1 = 0xFF4CAF50.toInt(),
                castleMoundColor2 = 0xFF66BB6A.toInt(),
                pathBorderColor = 0xFF2E7D32.toInt(),
                pathEdgeColor = 0xFF6D4C41.toInt(),
                pathColor = 0xFF8D6E63.toInt(),
                pathCenterColor = 0xFFBCAAA4.toInt(),
                cobblestone1 = 0xFF9E9E9E.toInt(),
                cobblestone2 = 0xFF757575.toInt(),
                cobblestoneHighlight = 0xFFEEEEEE.toInt(),
                grassColor = 0xFF3A7D3A.toInt(),
                grassLightColor = 0xFF4CAF50.toInt(),
                trunkColor = 0xFF795548.toInt(),
                foliageColor1 = 0xFF2E7D32.toInt(),
                foliageColor2 = 0xFF4CAF50.toInt(),
                rockColor = 0xFF757575.toInt(),
                rockHighlightColor = 0xFF9E9E9E.toInt(),
                treelineColor1 = 0xFF2E7D32.toInt(),
                treelineColor2 = 0xFF388E3C.toInt(),
                ambientParticleType = AmbientParticleType.LEAVES
            )

            MapType.VALLEY -> MapTheme(
                mapType = MapType.VALLEY,
                skyTopDay = 0xFF5C6B73.toInt(),
                skyBotDay = 0xFFE0A96D.toInt(),  // Canyon golden-hour gradient
                skyTopNight = 0xFF191D24.toInt(),
                skyBotNight = 0xFF2D3238.toInt(),
                sunColor = 0xFFFFB74D.toInt(),
                sunGlowColor = 0xFFFFA726.toInt(),
                mountainColorDay = 0xFF8D6E63.toInt(),
                mountainColorNight = 0xFF3E2723.toInt(),
                hasSnowCaps = false,
                mountainHighlightColor = 0xFFBCAAA4.toInt(),
                groundTopDay = 0xFFA16E4B.toInt(),  // Layered canyon clay & sandstone
                groundBotDay = 0xFF70452E.toInt(),
                groundTopNight = 0xFF4A3022.toInt(),
                groundBotNight = 0xFF301D14.toInt(),
                hillColor1 = 0xFFB57D55.toInt(),
                hillColor2 = 0xFF8C5938.toInt(),
                castleMoundColor1 = 0xFF96603F.toInt(),
                castleMoundColor2 = 0xFFA8704D.toInt(),
                pathBorderColor = 0xFF5C3826.toInt(),
                pathEdgeColor = 0xFF754C34.toInt(),
                pathColor = 0xFFA88164.toInt(),
                pathCenterColor = 0xFFCDB49E.toInt(),
                cobblestone1 = 0xFF8D6E63.toInt(),
                cobblestone2 = 0xFF6D4C41.toInt(),
                cobblestoneHighlight = 0xFFD7CCC8.toInt(),
                grassColor = 0xFF795548.toInt(),
                grassLightColor = 0xFF8D6E63.toInt(),
                trunkColor = 0xFF4E342E.toInt(),
                foliageColor1 = 0xFF33691E.toInt(),
                foliageColor2 = 0xFF558B2F.toInt(),
                rockColor = 0xFF8D6E63.toInt(),
                rockHighlightColor = 0xFFD7CCC8.toInt(),
                treelineColor1 = 0xFF4E342E.toInt(),
                treelineColor2 = 0xFF558B2F.toInt(),
                ambientParticleType = AmbientParticleType.CANYON_WIND
            )

            MapType.CROSSROADS -> MapTheme(
                mapType = MapType.CROSSROADS,
                skyTopDay = 0xFF37474F.toInt(),
                skyBotDay = 0xFF607D8B.toInt(),  // Overcast royal sky
                skyTopNight = 0xFF102027.toInt(),
                skyBotNight = 0xFF263238.toInt(),
                sunColor = 0xFFFFF9C4.toInt(),
                sunGlowColor = 0xFFFFEE58.toInt(),
                mountainColorDay = 0xFF546E7A.toInt(),
                mountainColorNight = 0xFF263238.toInt(),
                hasSnowCaps = true,
                mountainHighlightColor = 0xFFCFD8DC.toInt(),
                groundTopDay = 0xFF455A64.toInt(),  // Fortified paved turf
                groundBotDay = 0xFF263238.toInt(),
                groundTopNight = 0xFF1E281F.toInt(),
                groundBotNight = 0xFF141C15.toInt(),
                hillColor1 = 0xFF546E7A.toInt(),
                hillColor2 = 0xFF37474F.toInt(),
                castleMoundColor1 = 0xFF455A64.toInt(),
                castleMoundColor2 = 0xFF546E7A.toInt(),
                pathBorderColor = 0xFF263238.toInt(),
                pathEdgeColor = 0xFF37474F.toInt(),
                pathColor = 0xFF607D8B.toInt(),
                pathCenterColor = 0xFF90A4AE.toInt(),
                cobblestone1 = 0xFF78909C.toInt(),
                cobblestone2 = 0xFF546E7A.toInt(),
                cobblestoneHighlight = 0xFFCFD8DC.toInt(),
                grassColor = 0xFF2E4A36.toInt(),
                grassLightColor = 0xFF3E6B4A.toInt(),
                trunkColor = 0xFF3E2723.toInt(),
                foliageColor1 = 0xFF1B5E20.toInt(),
                foliageColor2 = 0xFF2E7D32.toInt(),
                rockColor = 0xFF546E7A.toInt(),
                rockHighlightColor = 0xFF90A4AE.toInt(),
                treelineColor1 = 0xFF263238.toInt(),
                treelineColor2 = 0xFF37474F.toInt(),
                ambientParticleType = AmbientParticleType.MIST_BANNERS,
                hasCrossroadsSquare = true
            )

            MapType.DESERT -> MapTheme(
                mapType = MapType.DESERT,
                skyTopDay = 0xFFF39C12.toInt(),
                skyBotDay = 0xFFFDEBD0.toInt(),  // Blazing desert heat haze
                skyTopNight = 0xFF1A1829.toInt(),
                skyBotNight = 0xFF2C2541.toInt(),
                sunColor = 0xFFFFF9C4.toInt(),
                sunGlowColor = 0xFFFFD54F.toInt(),
                mountainColorDay = 0xFFC09858.toInt(),
                mountainColorNight = 0xFF423321.toInt(),
                hasSnowCaps = false,
                mountainHighlightColor = 0xFFD4B86A.toInt(),
                groundTopDay = 0xFFD4B86A.toInt(),  // Sand dunes
                groundBotDay = 0xFFA08040.toInt(),
                groundTopNight = 0xFF5A4820.toInt(),
                groundBotNight = 0xFF3D3010.toInt(),
                hillColor1 = 0xFFC0A050.toInt(),
                hillColor2 = 0xFFB09030.toInt(),
                castleMoundColor1 = 0xFFB89A50.toInt(),
                castleMoundColor2 = 0xFFC4A860.toInt(),
                pathBorderColor = 0xFF8C6C38.toInt(),
                pathEdgeColor = 0xFF9E8040.toInt(),
                pathColor = 0xFFB89A5A.toInt(),
                pathCenterColor = 0xFFD4BA80.toInt(),
                cobblestone1 = 0xFFC4A860.toInt(),
                cobblestone2 = 0xFFA08040.toInt(),
                cobblestoneHighlight = 0xFFFFECB3.toInt(),
                grassColor = 0xFFC2A04E.toInt(),
                grassLightColor = 0xFFD4B86A.toInt(),
                trunkColor = 0xFF6D4C41.toInt(),
                foliageColor1 = 0xFF689F38.toInt(),
                foliageColor2 = 0xFF827717.toInt(),
                rockColor = 0xFFA08060.toInt(),
                rockHighlightColor = 0xFFC0A080.toInt(),
                treelineColor1 = 0xFF8B7535.toInt(),
                treelineColor2 = 0xFF9E8040.toInt(),
                ambientParticleType = AmbientParticleType.DESERT_DUST
            )

            MapType.SNOW -> MapTheme(
                mapType = MapType.SNOW,
                skyTopDay = 0xFF90CAF9.toInt(),
                skyBotDay = 0xFFE3F2FD.toInt(),  // Crisp arctic daylight
                skyTopNight = 0xFF0A192F.toInt(),
                skyBotNight = 0xFF172A45.toInt(),
                sunColor = 0xFFFFFDE7.toInt(),
                sunGlowColor = 0xFFE0F7FA.toInt(),
                mountainColorDay = 0xFF78909C.toInt(),
                mountainColorNight = 0xFF263238.toInt(),
                hasSnowCaps = true,
                mountainHighlightColor = 0xFFFFFFFF.toInt(),
                groundTopDay = 0xFFE0E8F0.toInt(),  // Frozen snowpack
                groundBotDay = 0xFFB0C0D0.toInt(),
                groundTopNight = 0xFF506070.toInt(),
                groundBotNight = 0xFF3A4A5A.toInt(),
                hillColor1 = 0xFFD0D8E0.toInt(),
                hillColor2 = 0xFFB8C4D0.toInt(),
                castleMoundColor1 = 0xFFC8D4E0.toInt(),
                castleMoundColor2 = 0xFFD8E0E8.toInt(),
                pathBorderColor = 0xFF78909C.toInt(),
                pathEdgeColor = 0xFF90989F.toInt(),
                pathColor = 0xFFB0B8C0.toInt(),
                pathCenterColor = 0xFFD0D8E0.toInt(),
                cobblestone1 = 0xFF90A4AE.toInt(),
                cobblestone2 = 0xFF78909C.toInt(),
                cobblestoneHighlight = 0xFFFFFFFF.toInt(),
                grassColor = 0xFFE0E8F0.toInt(),
                grassLightColor = 0xFFF0F4F8.toInt(),
                trunkColor = 0xFF5D4037.toInt(),
                foliageColor1 = 0xFF1B5E20.toInt(),
                foliageColor2 = 0xFFE0E8F0.toInt(),  // Frosted tips
                rockColor = 0xFFA0A8B0.toInt(),
                rockHighlightColor = 0xFFC8D0D8.toInt(),
                treelineColor1 = 0xFF4A5A5A.toInt(),
                treelineColor2 = 0xFF5A6A6A.toInt(),
                ambientParticleType = AmbientParticleType.BLIZZARD_SNOW
            )

            MapType.LAVA -> MapTheme(
                mapType = MapType.LAVA,
                skyTopDay = 0xFF210B0B.toInt(),
                skyBotDay = 0xFF5D1010.toInt(),  // Smoky brimstone & ash haze
                skyTopNight = 0xFF140404.toInt(),
                skyBotNight = 0xFF380A0A.toInt(),
                sunColor = 0xFFFF3D00.toInt(),
                sunGlowColor = 0xFFFF6D00.toInt(),
                mountainColorDay = 0xFF2E1C1C.toInt(),
                mountainColorNight = 0xFF1B0E0E.toInt(),
                hasSnowCaps = false,
                mountainHighlightColor = 0xFFFF3D00.toInt(), // Magma flow highlight
                groundTopDay = 0xFF3E2723.toInt(),  // Charred volcanic crust
                groundBotDay = 0xFF1A100F.toInt(),
                groundTopNight = 0xFF211311.toInt(),
                groundBotNight = 0xFF120908.toInt(),
                hillColor1 = 0xFF4E342E.toInt(),
                hillColor2 = 0xFF2E1E1C.toInt(),
                castleMoundColor1 = 0xFF3E2723.toInt(),
                castleMoundColor2 = 0xFF4E342E.toInt(),
                pathBorderColor = 0xFF21100C.toInt(),
                pathEdgeColor = 0xFF3E2018.toInt(),
                pathColor = 0xFF5D3225.toInt(),
                pathCenterColor = 0xFFBF360C.toInt(),  // Glowing magma line
                cobblestone1 = 0xFF37474F.toInt(),
                cobblestone2 = 0xFF263238.toInt(),
                cobblestoneHighlight = 0xFFFF5722.toInt(),
                grassColor = 0xFF3E2723.toInt(),
                grassLightColor = 0xFF4E342E.toInt(),
                trunkColor = 0xFF1B1B1B.toInt(),
                foliageColor1 = 0xFF263238.toInt(),
                foliageColor2 = 0xFF3E2723.toInt(),
                rockColor = 0xFF616161.toInt(),
                rockHighlightColor = 0xFFFF6F00.toInt(),
                treelineColor1 = 0xFF1A100F.toInt(),
                treelineColor2 = 0xFF2E1E1C.toInt(),
                ambientParticleType = AmbientParticleType.LAVA_EMBERS
            )

            MapType.ENCHANTED -> MapTheme(
                mapType = MapType.ENCHANTED,
                skyTopDay = 0xFF1B0A2A.toInt(),
                skyBotDay = 0xFF3A1054.toInt(),  // Luminous violet twilight nebula
                skyTopNight = 0xFF0B001A.toInt(),
                skyBotNight = 0xFF20083B.toInt(),
                sunColor = 0xFFE1BEE7.toInt(),
                sunGlowColor = 0xFFBA68C8.toInt(),
                mountainColorDay = 0xFF4A148C.toInt(),
                mountainColorNight = 0xFF311B92.toInt(),
                hasSnowCaps = false,
                mountainHighlightColor = 0xFF00E5FF.toInt(), // Ethereal crystal peaks
                groundTopDay = 0xFF0E2B1E.toInt(),  // Mystical emerald grove turf
                groundBotDay = 0xFF081C13.toInt(),
                groundTopNight = 0xFF071710.toInt(),
                groundBotNight = 0xFF030C08.toInt(),
                hillColor1 = 0xFF163E2B.toInt(),
                hillColor2 = 0xFF0E281C.toInt(),
                castleMoundColor1 = 0xFF1B4D36.toInt(),
                castleMoundColor2 = 0xFF236B4B.toInt(),
                pathBorderColor = 0xFF311B92.toInt(),
                pathEdgeColor = 0xFF512DA8.toInt(),
                pathColor = 0xFF7E57C2.toInt(),
                pathCenterColor = 0xFFCE93D8.toInt(),  // Amethyst starlight road
                cobblestone1 = 0xFF673AB7.toInt(),
                cobblestone2 = 0xFF512DA8.toInt(),
                cobblestoneHighlight = 0xFFE040FB.toInt(),
                grassColor = 0xFF1B5E20.toInt(),
                grassLightColor = 0xFF2E7D32.toInt(),
                trunkColor = 0xFF4A148C.toInt(),
                foliageColor1 = 0xFF00BFA5.toInt(),
                foliageColor2 = 0xFFAB47BC.toInt(),
                rockColor = 0xFF7E57C2.toInt(),
                rockHighlightColor = 0xFF00E5FF.toInt(),
                treelineColor1 = 0xFF12002B.toInt(),
                treelineColor2 = 0xFF240046.toInt(),
                ambientParticleType = AmbientParticleType.FAIRY_SPARKS
            )

            MapType.VOLCANO -> MapTheme(
                mapType = MapType.VOLCANO,
                skyTopDay = 0xFF1E0C08.toInt(),
                skyBotDay = 0xFF4E1810.toInt(),  // Volcanic ash smog
                skyTopNight = 0xFF140502.toInt(),
                skyBotNight = 0xFF330C05.toInt(),
                sunColor = 0xFFFF3D00.toInt(),
                sunGlowColor = 0xFFFF6D00.toInt(),
                mountainColorDay = 0xFF2B120E.toInt(),
                mountainColorNight = 0xFF1A0A08.toInt(),
                hasSnowCaps = false,
                mountainHighlightColor = 0xFFFF3D00.toInt(), // Cascading lava waterfalls
                groundTopDay = 0xFF241816.toInt(),  // Dark volcanic ash plains
                groundBotDay = 0xFF160E0D.toInt(),
                groundTopNight = 0xFF190F0E.toInt(),
                groundBotNight = 0xFF0E0807.toInt(),
                hillColor1 = 0xFF331F1C.toInt(),
                hillColor2 = 0xFF261412.toInt(),
                castleMoundColor1 = 0xFF3A2320.toInt(),
                castleMoundColor2 = 0xFF482B27.toInt(),
                pathBorderColor = 0xFF1A0A06.toInt(),
                pathEdgeColor = 0xFF33120A.toInt(),
                pathColor = 0xFF4E1E12.toInt(),
                pathCenterColor = 0xFFD84315.toInt(),  // Hot cinder trail
                cobblestone1 = 0xFF3E2723.toInt(),
                cobblestone2 = 0xFF271815.toInt(),
                cobblestoneHighlight = 0xFFFF9100.toInt(),
                grassColor = 0xFF2E2E2E.toInt(),
                grassLightColor = 0xFF424242.toInt(),
                trunkColor = 0xFF1B1B1B.toInt(),
                foliageColor1 = 0xFF212121.toInt(),
                foliageColor2 = 0xFF37474F.toInt(),
                rockColor = 0xFF455A64.toInt(),
                rockHighlightColor = 0xFFFF6D00.toInt(),
                treelineColor1 = 0xFF160E0D.toInt(),
                treelineColor2 = 0xFF241816.toInt(),
                ambientParticleType = AmbientParticleType.VOLCANO_ASH,
                hasCrater = true
            )
        }
    }
}
