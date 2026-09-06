package com.example.myapp.game

import android.graphics.*
import com.example.myapp.game.EnemyType
import com.example.myapp.game.TowerType

/**
 * Procedural Retro-Modern Pixel Art Sprite Engine.
 * Bakes authentic 16-bit pixel art bitmaps at startup for crisp,
 * high-performance 60-120 FPS rendering across all Android screen densities.
 */
object SpriteManager {

    private var initialized = false

    // Paint optimized for sharp pixel art without blur
    val pixelPaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
        isDither = false
    }

    // Hit-flash paint for when entities take damage
    val hitFlashPaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
    }

    // Freeze paint for slowed / frozen entities
    val freezePaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
        colorFilter = PorterDuffColorFilter(0xFF81D4FA.toInt(), PorterDuff.Mode.SRC_ATOP)
    }

    // Palettes: Common retro fantasy colors
    private const val T = 0 // Transparent
    private const val K = 0xFF141419.toInt() // Dark outline / shadow
    private const val W = 0xFFFFFFFF.toInt() // Pure White
    private const val G_WOOD1 = 0xFF8D6E63.toInt()
    private const val G_WOOD2 = 0xFF5D4037.toInt()
    private const val G_WOOD3 = 0xFF3E2723.toInt()
    private const val G_STONE1 = 0xFFBDBDBD.toInt()
    private const val G_STONE2 = 0xFF757575.toInt()
    private const val G_STONE3 = 0xFF424242.toInt()
    private const val G_GOLD1 = 0xFFFFD54F.toInt()
    private const val G_GOLD2 = 0xFFFFA000.toInt()
    private const val G_IRON1 = 0xFFECEFF1.toInt()
    private const val G_IRON2 = 0xFF90A4AE.toInt()
    private const val G_IRON3 = 0xFF455A64.toInt()

    // Sprite caches: Entity -> Array of animation frame Bitmaps
    private val enemyFrames = mutableMapOf<EnemyType, Array<Bitmap>>()
    private val towerSprites = mutableMapOf<TowerType, Array<Bitmap>>() // Index 0: Lv1-3, 1: Lv4-7, 2: Lv8-10
    private var playerFrames = emptyArray<Bitmap>() // 0: Idle, 1: Step1, 2: Step2, 3: Attack
    private var baseSprites = emptyArray<Bitmap>() // 0: Healthy (>60%), 1: Damaged (25-60%), 2: Critical (<25%)

    /** Initialize and rasterize all sprite sheets */
    @Synchronized
    fun initialize() {
        if (initialized) return
        bakePlayerSprites()
        bakeEnemySprites()
        bakeTowerSprites()
        bakeBaseSprites()
        initialized = true
    }

    // --- Helper to convert character matrix to scaled Bitmap ---
    private fun parseSprite(pattern: Array<String>, palette: Map<Char, Int>, scale: Int = 3): Bitmap {
        val rows = pattern.size
        val cols = pattern.maxOf { it.length }
        val bmp = Bitmap.createBitmap(cols * scale, rows * scale, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(cols * scale * rows * scale)

        for (y in 0 until rows) {
            val line = pattern[y]
            for (x in 0 until cols) {
                val ch = if (x < line.length) line[x] else '.'
                val color = if (ch == '.' || ch == ' ') 0 else (palette[ch] ?: 0)
                if (color != 0) {
                    for (dy in 0 until scale) {
                        for (dx in 0 until scale) {
                            pixels[(y * scale + dy) * (cols * scale) + (x * scale + dx)] = color
                        }
                    }
                }
            }
        }
        bmp.setPixels(pixels, 0, cols * scale, 0, 0, cols * scale, rows * scale)
        return bmp
    }

    // ==========================================
    // 1. PLAYER SPRITES (Hero Knight)
    // ==========================================
    private fun bakePlayerSprites() {
        val palette = mapOf(
            'K' to K,
            'H' to 0xFF90CAF9.toInt(), // Helmet visor
            'A' to 0xFF1976D2.toInt(), // Royal blue armor
            'B' to 0xFF0D47A1.toInt(), // Dark armor shade
            'S' to 0xFFECEFF1.toInt(), // Steel sword / shield rim
            'G' to 0xFFFFD700.toInt(), // Gold crest / trim
            'F' to 0xFFFFCC80.toInt(), // Face / eyes
            'D' to 0xFF5D4037.toInt()  // Boots / leather
        )

        // Idle Frame
        val f0 = arrayOf(
            "....KKKK....",
            "...KGGGGK...",
            "..KGAAGAK...",
            "..KASSSAK...",
            "..KAHHHAK...",
            "..KKKKKKKK..",
            ".KSSAAAAKSDK",
            ".KSSAAAAKSDK",
            ".KSSKAAKKSSK",
            "..KKKAAK.SSK",
            "...KDDDK.KK.",
            "...KDDDK....",
            "...KK.KK...."
        )

        // Step 1
        val f1 = arrayOf(
            "....KKKK....",
            "...KGGGGK...",
            "..KGAAGAK...",
            "..KASSSAK...",
            "..KAHHHAK...",
            "..KKKKKKKK..",
            ".KSSAAAAKSDK",
            ".KSSAAAAKSDK",
            ".KSSKAAKKSSK",
            "..KKKAAK.SSK",
            "..KDDKKK.KK.",
            "..KDK.KDDK..",
            "..KK...KKK.."
        )

        // Step 2
        val f2 = arrayOf(
            "....KKKK....",
            "...KGGGGK...",
            "..KGAAGAK...",
            "..KASSSAK...",
            "..KAHHHAK...",
            "..KKKKKKKK..",
            ".KSSAAAAKSDK",
            ".KSSAAAAKSDK",
            ".KSSKAAKKSSK",
            "..KKKAAK.SSK",
            "...KKKDDKKK.",
            "..KDDK.KDK..",
            "..KKK...KK.."
        )

        // Attack Swing
        val f3 = arrayOf(
            "....KKKK..SS",
            "...KGGGGK.SS",
            "..KGAAGAKKSS",
            "..KASSSAKKSS",
            "..KAHHHAKKSS",
            "..KKKKKKKKSS",
            ".KSSAAAAKSSS",
            ".KSSAAAAKKKK",
            ".KSSKAAK....",
            "..KKKAAK....",
            "...KDDDK....",
            "..KDDKDDK...",
            "..KK...KK..."
        )

        playerFrames = arrayOf(
            parseSprite(f0, palette, 4),
            parseSprite(f1, palette, 4),
            parseSprite(f2, palette, 4),
            parseSprite(f3, palette, 4)
        )
    }

    // ==========================================
    // 2. ENEMY SPRITES (4-Frame Walk Cycles)
    // ==========================================
    private fun bakeEnemySprites() {
        // --- GOBLIN ---
        val gobPal = mapOf(
            'K' to K,
            'G' to 0xFF4CAF50.toInt(), // Skin
            'L' to 0xFF81C784.toInt(), // Light ear
            'D' to 0xFF2E7D32.toInt(), // Dark skin
            'E' to 0xFFD32F2F.toInt(), // Red eye
            'C' to 0xFF795548.toInt(), // Loincloth
            'S' to 0xFFCFD8DC.toInt()  // Dagger
        )
        val gobF0 = arrayOf(
            "L.KKKK.L",
            "LGGDGGGL",
            "KGEGGEGK",
            "KGGGGGGK",
            ".KDDDDK.",
            "KSCCCCSK",
            ".KCCCCK.",
            ".KG..GK.",
            ".KK..KK."
        )
        val gobF1 = arrayOf(
            "L.KKKK.L",
            "LGGDGGGL",
            "KGEGGEGK",
            "KGGGGGGK",
            ".KDDDDK.",
            "KSCCCCSK",
            ".KCCCCK.",
            ".KK..GK.",
            ".....KK."
        )
        val gobF2 = arrayOf(
            "L.KKKK.L",
            "LGGDGGGL",
            "KGEGGEGK",
            "KGGGGGGK",
            ".KDDDDK.",
            "KSCCCCSK",
            ".KCCCCK.",
            ".KG..KK.",
            ".KK....."
        )
        val gobF3 = arrayOf(
            "L.KKKK.L",
            "LGGDGGGL",
            "KGEGGEGK",
            "KGGGGGGK",
            ".KDDDDK.S",
            ".KCCCCKSS",
            ".KCCCCKK.",
            ".KG..GK.",
            ".KK..KK."
        )
        enemyFrames[EnemyType.GOBLIN] = arrayOf(
            parseSprite(gobF0, gobPal, 4),
            parseSprite(gobF1, gobPal, 4),
            parseSprite(gobF2, gobPal, 4),
            parseSprite(gobF3, gobPal, 4)
        )

        // --- SKELETON ---
        val skelPal = mapOf(
            'K' to K,
            'B' to 0xFFEEEEEE.toInt(), // Bone white
            'D' to 0xFF9E9E9E.toInt(), // Dark bone
            'E' to 0xFF212121.toInt(), // Eye socket
            'S' to 0xFFB0BEC5.toInt()  // Sword
        )
        val skelF0 = arrayOf(
            "..KKKK..",
            ".KBBBBK.",
            "KBEKBBEK",
            "KBBBBBBK",
            ".KBEBEK.",
            "..KBBK..",
            ".KBBBBK.",
            "SKBKKBSK",
            "SKB..BSK",
            ".KK..KK."
        )
        val skelF1 = arrayOf(
            "..KKKK..",
            ".KBBBBK.",
            "KBEKBBEK",
            "KBBBBBBK",
            ".KBEBEK.",
            "..KBBK..",
            ".KBBBBK.",
            "SKBKKBSK",
            "SKB..KK.",
            ".KK....."
        )
        val skelF2 = arrayOf(
            "..KKKK..",
            ".KBBBBK.",
            "KBEKBBEK",
            "KBBBBBBK",
            ".KBEBEK.",
            "..KBBK..",
            ".KBBBBK.",
            "SKBKKBSK",
            ".KK..BSK",
            ".....KK."
        )
        val skelF3 = arrayOf(
            "..KKKK..",
            ".KBBBBK.S",
            "KBEKBBEKSS",
            "KBBBBBBKS",
            ".KBEBEK.K",
            "..KBBK..",
            ".KBBBBK.",
            ".KBKKBSK",
            ".KB..BSK",
            ".KK..KK."
        )
        enemyFrames[EnemyType.SKELETON] = arrayOf(
            parseSprite(skelF0, skelPal, 4),
            parseSprite(skelF1, skelPal, 4),
            parseSprite(skelF2, skelPal, 4),
            parseSprite(skelF3, skelPal, 4)
        )

        // --- ORC ---
        val orcPal = mapOf(
            'K' to K,
            'O' to 0xFF388E3C.toInt(), // Orc dark green
            'L' to 0xFF66BB6A.toInt(), // Light green
            'T' to 0xFFFFFFCC.toInt(), // Tusks
            'E' to 0xFFFF5722.toInt(), // Angry eyes
            'H' to 0xFF455A64.toInt(), // Horned helm
            'A' to 0xFF6D4C41.toInt()  // Leather armor
        )
        val orcF0 = arrayOf(
            "...KKKKK...",
            "..KHHOHHK..",
            ".KHOOOOOHK.",
            ".KOEOKOEKO.",
            ".KTOOTTOOK.",
            "..KKKKKKK..",
            ".KAAOOOAAK.",
            "KOAOOOOAAOK",
            ".KAAOOOAAK.",
            "..KO...OK..",
            "..KK...KK.."
        )
        val orcF1 = arrayOf(
            "...KKKKK...",
            "..KHHOHHK..",
            ".KHOOOOOHK.",
            ".KOEOKOEKO.",
            ".KTOOTTOOK.",
            "..KKKKKKK..",
            ".KAAOOOAAK.",
            "KOAOOOOAAOK",
            ".KAAOOOAAK.",
            "..KK...OK..",
            ".......KK.."
        )
        val orcF2 = arrayOf(
            "...KKKKK...",
            "..KHHOHHK..",
            ".KHOOOOOHK.",
            ".KOEOKOEKO.",
            ".KTOOTTOOK.",
            "..KKKKKKK..",
            ".KAAOOOAAK.",
            "KOAOOOOAAOK",
            ".KAAOOOAAK.",
            "..KO...KK..",
            "..KK......."
        )
        val orcF3 = arrayOf(
            "...KKKKK...",
            "..KHHOHHK..",
            ".KHOOOOOHK.",
            ".KOEOKOEKO.",
            ".KTOOTTOOK.",
            "..KKKKKKK..",
            "KKAAOOOAAKK",
            "KOAOOOOAAOK",
            ".KAAOOOAAK.",
            "..KO...OK..",
            "..KK...KK.."
        )
        enemyFrames[EnemyType.ORC] = arrayOf(
            parseSprite(orcF0, orcPal, 4),
            parseSprite(orcF1, orcPal, 4),
            parseSprite(orcF2, orcPal, 4),
            parseSprite(orcF3, orcPal, 4)
        )

        // --- DEMON (Winged Fire Fiend) ---
        val demPal = mapOf(
            'K' to K,
            'R' to 0xFFD32F2F.toInt(), // Crimson skin
            'D' to 0xFF880E4F.toInt(), // Dark crimson
            'H' to 0xFF212121.toInt(), // Black horns
            'E' to 0xFFFFD54F.toInt(), // Yellow burning eyes
            'W' to 0xFFB71C1C.toInt()  // Leathery wings
        )
        val demF0 = arrayOf(
            "H..KKKK..H",
            "HKKRRRRKKH",
            ".KRERRERK.",
            ".KRRRRRRK.",
            "W.KRRRRK.W",
            "WWKRRRRKWW",
            "WWKDDDDWWK",
            ".KKDRRDKK.",
            "..KR..RK..",
            "..KK..KK.."
        )
        val demF1 = arrayOf(
            "H..KKKK..H",
            "HKKRRRRKKH",
            ".KRERRERK.",
            "W.KRRRRK.W",
            "WWKRRRRKWW",
            "W.KRRRRK.W",
            "..KDDDD..K",
            ".KKDRRDKK.",
            "..KK..RK..",
            "......KK.."
        )
        val demF2 = arrayOf(
            "H..KKKK..H",
            "HKKRRRRKKH",
            ".KRERRERK.",
            ".KRRRRRRK.",
            "WWKRRRRKWW",
            "W.KRRRRK.W",
            "..KDDDD..K",
            ".KKDRRDKK.",
            "..KR..KK..",
            "..KK......"
        )
        val demF3 = arrayOf(
            "H..KKKK..H",
            "HKKRRRRKKH",
            ".KRERRERK.",
            "WWKRRRRKWW",
            "WWKRRRRKWW",
            ".WKRRRRKW.",
            "..KDDDD..K",
            ".KKDRRDKK.",
            "..KR..RK..",
            "..KK..KK.."
        )
        enemyFrames[EnemyType.DEMON] = arrayOf(
            parseSprite(demF0, demPal, 4),
            parseSprite(demF1, demPal, 4),
            parseSprite(demF2, demPal, 4),
            parseSprite(demF3, demPal, 4)
        )

        // --- SLIME (Bouncing Jelly) ---
        val slimePal = mapOf(
            'K' to K,
            'G' to 0xFF00E676.toInt(), // Vivid slime green
            'L' to 0xFFB9F6CA.toInt(), // Specular highlight
            'D' to 0xFF00B0FF.toInt(), // Blue eye
            'S' to 0xFF00C853.toInt()  // Shadowed slime
        )
        val sliF0 = arrayOf(
            "...KKKK...",
            "..KGGLGGK.",
            ".KGGGGGGK.",
            "KGDGGGDGGK",
            "KGGGGGGGGK",
            "KSSSSSSSSK",
            ".KKKKKKKK."
        )
        val sliF1 = arrayOf(
            "....KK....",
            "...KGGLK..",
            "..KGGGGK..",
            ".KGDGGDGGK",
            ".KGGGGGGGK",
            ".KSSSSSSSK",
            "..KKKKKK.."
        )
        val sliF2 = arrayOf(
            "..KKKKKK..",
            ".KGGGLGGK.",
            "KGGGGGGGGK",
            "KGDGGGDGGK",
            "KSSSSSSSSK",
            "KKKKKKKKKK"
        )
        val sliF3 = sliF0
        enemyFrames[EnemyType.SLIME] = arrayOf(
            parseSprite(sliF0, slimePal, 4),
            parseSprite(sliF1, slimePal, 4),
            parseSprite(sliF2, slimePal, 4),
            parseSprite(sliF3, slimePal, 4)
        )

        // --- GHOST (Spectral Wraith) ---
        val ghostPal = mapOf(
            'K' to K,
            'G' to 0xFFE0F7FA.toInt(), // Pale glowing spirit white
            'C' to 0xFF80DEEA.toInt(), // Ethereal cyan
            'M' to 0xFF26C6DA.toInt(), // Mid cyan shade
            'E' to 0xFF006064.toInt()  // Dark hollow eye sockets
        )
        val ghF0 = arrayOf(
            "...KKKK...",
            "..KGGGGK..",
            ".KGGEGEGK.",
            ".KGGGEEGK.",
            ".KCCCCCGK.",
            ".KMMMMMKK.",
            ".KM.KM.MK.",
            "..K..K..K."
        )
        val ghF1 = arrayOf(
            "...KKKK...",
            "..KGGGGK..",
            ".KGGEGEGK.",
            ".KGGGEEGK.",
            ".KCCCCCGK.",
            ".KMMMMMKK.",
            "..KM.KM.K.",
            "...K..K..."
        )
        val ghF2 = arrayOf(
            "...KKKK...",
            "..KGGGGK..",
            ".KGGEGEGK.",
            ".KGGGEEGK.",
            ".KCCCCCGK.",
            ".KMMMMMKK.",
            ".KM.KM.MK.",
            ".K..K..K.."
        )
        val ghF3 = ghF1
        enemyFrames[EnemyType.GHOST] = arrayOf(
            parseSprite(ghF0, ghostPal, 4),
            parseSprite(ghF1, ghostPal, 4),
            parseSprite(ghF2, ghostPal, 4),
            parseSprite(ghF3, ghostPal, 4)
        )

        // --- NECROMANCER (Dark Archon) ---
        val necPal = mapOf(
            'K' to K,
            'P' to 0xFF4A148C.toInt(), // Deep purple cowl
            'L' to 0xFF7B1FA2.toInt(), // Violet robes
            'S' to 0xFFEEEEEE.toInt(), // Skull mask
            'E' to 0xFF00E676.toInt(), // Glowing green eye sockets
            'G' to 0xFFFFD700.toInt(), // Gold amulet
            'W' to 0xFF5D4037.toInt(), // Oak staff
            'O' to 0xFFBA68C8.toInt()  // Floating soul crystal
        )
        val necF0 = arrayOf(
            "..KKKK....",
            ".KPPLLPK..",
            "KPSESESPK.",
            "KPSKKKSPK.",
            ".KPLGLPK.O",
            ".KLLLLPKWK",
            "KPLLLLLPKW",
            "KPLLLLLPKW",
            ".KLLLLPKWK",
            "..KK..KK.K"
        )
        val necF1 = arrayOf(
            "..KKKK....",
            ".KPPLLPK.O",
            "KPSESESPKK",
            "KPSKKKSPKW",
            ".KPLGLPKKW",
            ".KLLLLPK.W",
            "KPLLLLLPKW",
            "KPLLLLLPKW",
            ".KK...KK.K",
            "........WK"
        )
        val necF2 = arrayOf(
            "..KKKK....",
            ".KPPLLPK..",
            "KPSESESPK.",
            "KPSKKKSPK.",
            ".KPLGLPK.O",
            ".KLLLLPKWK",
            "KPLLLLLPKW",
            "KPLLLLLPKW",
            "..KK..KKKW",
            ".........K"
        )
        val necF3 = necF1
        enemyFrames[EnemyType.NECROMANCER] = arrayOf(
            parseSprite(necF0, necPal, 4),
            parseSprite(necF1, necPal, 4),
            parseSprite(necF2, necPal, 4),
            parseSprite(necF3, necPal, 4)
        )

        // --- MAGMA CRAB (Molten Crustacean) ---
        val magPal = mapOf(
            'K' to K,
            'R' to 0xFFFF3D00.toInt(), // Red volcanic carapace
            'Y' to 0xFFFFD600.toInt(), // Glowing molten veins
            'B' to 0xFF3E2723.toInt(), // Basalt rim
            'C' to 0xFFD50000.toInt(), // Snapping pincers
            'E' to 0xFFFFFF00.toInt()  // Molten eyes
        )
        val magF0 = arrayOf(
            "C...KKKK...C",
            "CK.KRRRRK.KC",
            ".KKBEYYEBKK.",
            ".KRYRRRYRYK.",
            "KKRRYYYYRRKK",
            "K.KBBBBBBK.K",
            "...KK..KK..."
        )
        val magF1 = arrayOf(
            ".C..KKKK..C.",
            "CK.KRRRRK.KC",
            ".KKBEYYEBKK.",
            ".KRYRRRYRYK.",
            "KKRRYYYYRRKK",
            "..KBBBBBBK..",
            ".KK......KK."
        )
        val magF2 = arrayOf(
            "C...KKKK...C",
            "CK.KRRRRK.KC",
            ".KKBEYYEBKK.",
            ".KRYRRRYRYK.",
            "KKRRYYYYRRKK",
            "K.KBBBBBBK.K",
            "..KK....KK.."
        )
        val magF3 = magF1
        enemyFrames[EnemyType.MAGMA_CRAB] = arrayOf(
            parseSprite(magF0, magPal, 4),
            parseSprite(magF1, magPal, 4),
            parseSprite(magF2, magPal, 4),
            parseSprite(magF3, magPal, 4)
        )

        // --- HARPY (Winged Siren) ---
        val harPal = mapOf(
            'K' to K,
            'W' to 0xFF9575CD.toInt(), // Purple wings
            'V' to 0xFF512DA8.toInt(), // Dark plumage
            'F' to 0xFFFFCC80.toInt(), // Face
            'E' to 0xFFFF1744.toInt(), // Crimson eyes
            'B' to 0xFFFFB300.toInt(), // Beak
            'T' to 0xFFFFD54F.toInt()  // Talons
        )
        val harF0 = arrayOf(
            "W...KKKK...W",
            "WW.KFFFFK.WW",
            "WVKFEBEFKVWW",
            ".VKFFFFKVW.",
            "..KWWWWK...",
            "..KVVVVK...",
            "..KT..TK...",
            "..KK..KK..."
        )
        val harF1 = arrayOf(
            "WW..KKKK..WW",
            ".W.KFFFFK.W.",
            ".VKFEBEFKV..",
            ".VKFFFFKV...",
            "..KWWWWK...",
            "..KVVVVK...",
            "...KTTK....",
            "...KKKK...."
        )
        val harF2 = arrayOf(
            "...KKKK.....",
            "WW.KFFFFK.WW",
            "WVKFEBEFKVWW",
            "W.KFFFFK.W.",
            "..KWWWWK...",
            "..KVVVVK...",
            "..KT..TK...",
            "..KK..KK..."
        )
        val harF3 = harF1
        enemyFrames[EnemyType.HARPY] = arrayOf(
            parseSprite(harF0, harPal, 4),
            parseSprite(harF1, harPal, 4),
            parseSprite(harF2, harPal, 4),
            parseSprite(harF3, harPal, 4)
        )

        // --- TREANT (Ancient Bark Titan) ---
        val trePal = mapOf(
            'K' to K,
            'W' to 0xFF4E342E.toInt(), // Dark oak bark
            'B' to 0xFF6D4C41.toInt(), // Wood grain
            'L' to 0xFF2E7D32.toInt(), // Forest foliage
            'G' to 0xFF66BB6A.toInt(), // Bright moss/leaves
            'E' to 0xFFFFEB3B.toInt()  // Ancient amber eyes
        )
        val treF0 = arrayOf(
            "..KKLLGGLLKK..",
            ".KLGGLLGGLLK.",
            "KLLGLLLLGLLLK",
            ".KWWWWWWWWK..",
            ".KWBEWWEBWK..",
            "KKWWWWWWWWKK.",
            "KWWBBBBBBWWK.",
            ".KWWWWWWWWK..",
            "..KWW..WWK...",
            "..KKK..KKK..."
        )
        val treF1 = arrayOf(
            "..KKLLGGLLKK..",
            ".KLGGLLGGLLK.",
            "KLLGLLLLGLLLK",
            ".KWWWWWWWWK..",
            ".KWBEWWEBWK..",
            "KKWWWWWWWWKK.",
            "KWWBBBBBBWWK.",
            ".KWWWWWWWWK..",
            "..KKW..WWK...",
            ".......KKK..."
        )
        val treF2 = arrayOf(
            "..KKLLGGLLKK..",
            ".KLGGLLGGLLK.",
            "KLLGLLLLGLLLK",
            ".KWWWWWWWWK..",
            ".KWBEWWEBWK..",
            "KKWWWWWWWWKK.",
            "KWWBBBBBBWWK.",
            ".KWWWWWWWWK..",
            "..KWW..WKK...",
            "..KKK........"
        )
        val treF3 = treF1
        enemyFrames[EnemyType.TREANT] = arrayOf(
            parseSprite(treF0, trePal, 4),
            parseSprite(treF1, trePal, 4),
            parseSprite(treF2, trePal, 4),
            parseSprite(treF3, trePal, 4)
        )
    }

    // ==========================================
    // 3. TOWER SPRITES (3 Progression Tiers Each)
    // ==========================================
    private fun bakeTowerSprites() {
        val tPal = mapOf(
            'K' to K,
            'W' to G_WOOD1, 'D' to G_WOOD2, 'B' to G_WOOD3,
            'S' to G_STONE1, 'G' to G_STONE2, 'C' to G_STONE3,
            'Y' to G_GOLD1, 'I' to G_IRON1, 'R' to 0xFFD32F2F.toInt(),
            'P' to 0xFFAB47BC.toInt(), 'E' to 0xFF00E5FF.toInt(),
            'F' to 0xFFFF5722.toInt(), 'X' to 0xFF81D4FA.toInt()
        )

        // --- ARROW TOWER ---
        // Tier 1: Rustic Wooden Guard Post (Lv 1-3)
        val arrT1 = arrayOf(
            "....KKKK....",
            "...KWWWWK...",
            "..KWWDDWWK..",
            "..KWWWWWWK..",
            "..KKKKKKKK..",
            ".KWWK..KWWK.",
            ".KWWK..KWWK.",
            "KWWKK..KKWWK",
            "KWWK....KWWK",
            "KKKK....KKKK"
        )
        // Tier 2: Reinforced Stone Bastion with Crest (Lv 4-7)
        val arrT2 = arrayOf(
            "....KRRK....",
            "....KRK.....",
            "..KKSSKK....",
            ".KSGSSGSK...",
            ".KSSSSSSK...",
            ".KSSGGSSK...",
            "..KKKKKK....",
            ".KSGKKGSK...",
            "KSGK..KGSK..",
            "KSGK..KGSK..",
            "KKKK..KKKK.."
        )
        // Tier 3: Grand Fortified Citadel with Heavy Ballista (Lv 8-10)
        val arrT3 = arrayOf(
            "...KYYKKYYK...",
            "..KYYSSYYK....",
            "..KSSSSSSK....",
            ".KSGGSSGGSK...",
            ".KSGIIIGGSK...",
            ".KSSSSSSSSK...",
            "..KKKKKKKK....",
            ".KSGKKKKGSK...",
            "KSGKKYYKKGSK..",
            "KSGKKYYKKGSK..",
            "KKKKK..KKKKK.."
        )
        towerSprites[TowerType.ARROW] = arrayOf(
            parseSprite(arrT1, tPal, 3),
            parseSprite(arrT2, tPal, 3),
            parseSprite(arrT3, tPal, 3)
        )

        // --- MAGIC TOWER ---
        // Tier 1: Simple Arcane Pillar
        val magT1 = arrayOf(
            "....KPPK....",
            "...KPPEPK...",
            "....KPPK....",
            ".....KK.....",
            "....KSSK....",
            "...KSGGSK...",
            "...KSGGSK...",
            "..KSGGGGSK..",
            "..KSGGGGSK..",
            "..KKKKKKKK.."
        )
        // Tier 2: Ornate Spire with Runes
        val magT2 = arrayOf(
            "...KKPPKK...",
            "..KPEEEEPK..",
            "...KKPPKK...",
            "....KYYK....",
            "...KSSSSK...",
            "..KSGEEGSK..",
            "..KSGGGSK...",
            ".KSGGGGGSK..",
            ".KSGGGGGSK..",
            ".KKKKKKKKK.."
        )
        // Tier 3: Celestial Grand Observatory with Floating Crystal Core
        val magT3 = arrayOf(
            "...KEEEEKK...",
            "..KEEWWEEK..",
            "...KEEEEKK...",
            "KYY..KK..YYK",
            ".KYYKSSKYYK.",
            "..KSGEEGSK..",
            "..KSGGGSK...",
            ".KSGGGGGSK..",
            "KSGGYYGGSK..",
            "KKKKKKKKKK.."
        )
        towerSprites[TowerType.MAGIC] = arrayOf(
            parseSprite(magT1, tPal, 3),
            parseSprite(magT2, tPal, 3),
            parseSprite(magT3, tPal, 3)
        )

        // --- CANNON TOWER ---
        // Tier 1: Wooden Carriage Mortar
        val canT1 = arrayOf(
            "....KIIIK...",
            "...KIIIIIK..",
            "..KIIIKIIIK.",
            "..KIKKKKKIK.",
            "..KKWWWWKK..",
            ".KWWDDDDWWK.",
            "KWWKKKKKKWWK",
            "KWWK....KWWK",
            "KKKK....KKKK"
        )
        // Tier 2: Iron Turret Fortress
        val canT2 = arrayOf(
            "...KKIIIKK..",
            "..KIIIIIIIK.",
            ".KIIIKKKIIIK",
            ".KIIKKKKKIIK",
            "..KKSSSSKK..",
            ".KSGGGGGSK..",
            "KSGKKKKKGSK.",
            "KSGK....KGSK",
            "KKKK....KKKK"
        )
        // Tier 3: Twin-Barrel Siege Dreadnought
        val canT3 = arrayOf(
            ".KIKK..KKIK.",
            ".KIIK..KIIK.",
            "KIIIIKKIIIIK",
            "KIIIIKKIIIIK",
            ".KKSSYYSSKK.",
            ".KSGGGGGSK..",
            "KSGKKKKKGSK.",
            "KSGKK..KKGSK",
            "KKKKK..KKKKK"
        )
        towerSprites[TowerType.CANNON] = arrayOf(
            parseSprite(canT1, tPal, 3),
            parseSprite(canT2, tPal, 3),
            parseSprite(canT3, tPal, 3)
        )

        // Other towers (Ice, Flame, Tesla, Healer, Poison, Necro, Ballista, Vortex)
        // give each distinct elemental themes
        val iceT = arrayOf(
            "....KXXK....",
            "...KXXXXK...",
            "..KXXWWXXK..",
            "...KXXXXK...",
            "....KSSK....",
            "...KSXXSK...",
            "..KSXXXXSK..",
            ".KSXXXXXXSK.",
            ".KKKKKKKKKK."
        )
        val flmT = arrayOf(
            "....KFFK....",
            "...KFFFFK...",
            "..KFFYYFFK..",
            "...KFFFFK...",
            "....KSSK....",
            "...KSFFSK...",
            "..KSFFFFSK..",
            ".KSFFFFFFSK.",
            ".KKKKKKKKKK."
        )
        val tesT = arrayOf(
            "....KEEK....",
            "...KEEEEK...",
            "..KEEWWEEK..",
            "...KEEEEK...",
            "....KIIK....",
            "...KIEEK...",
            "..KIEEEIK...",
            ".KIIIIIIIIK.",
            ".KKKKKKKKKK."
        )
        val iceBmp = parseSprite(iceT, tPal, 3)
        val flmBmp = parseSprite(flmT, tPal, 3)
        val tesBmp = parseSprite(tesT, tPal, 3)

        towerSprites[TowerType.ICE] = arrayOf(iceBmp, iceBmp, iceBmp)
        towerSprites[TowerType.FLAME] = arrayOf(flmBmp, flmBmp, flmBmp)
        towerSprites[TowerType.TESLA] = arrayOf(tesBmp, tesBmp, tesBmp)
        towerSprites[TowerType.HEALER] = arrayOf(magT1, magT2, magT3).map { parseSprite(it, tPal, 3) }.toTypedArray()
        towerSprites[TowerType.POISON] = arrayOf(iceBmp, iceBmp, iceBmp)
        towerSprites[TowerType.NECRO] = arrayOf(magT1, magT2, magT3).map { parseSprite(it, tPal, 3) }.toTypedArray()
        towerSprites[TowerType.BALLISTA] = arrayOf(arrT1, arrT2, arrT3).map { parseSprite(it, tPal, 3) }.toTypedArray()
        towerSprites[TowerType.VORTEX] = arrayOf(magT1, magT2, magT3).map { parseSprite(it, tPal, 3) }.toTypedArray()
    }

    // ==========================================
    // 4. CASTLE BASE SPRITES (3 Health States)
    // ==========================================
    private fun bakeBaseSprites() {
        val bPal = mapOf(
            'K' to K,
            'S' to G_STONE1, 'G' to G_STONE2, 'C' to G_STONE3,
            'Y' to G_GOLD1, 'R' to 0xFFD32F2F.toInt(),
            'I' to G_IRON1, 'W' to G_WOOD2, 'F' to 0xFFFF5722.toInt()
        )

        // Pristine (>60% HP)
        val baseGood = arrayOf(
            "KRRK...........KRRK",
            "KRK.............KRK",
            "KSSK...KYYYK...KSSK",
            "KSSK...KSSSK...KSSK",
            "KSGK...KSGSK...KSGK",
            "KSSKKKKKSSSKKKKKSSK",
            "KSSSSSSSSSSSSSSSSSK",
            "KSGGSSSSGSSGGSSSGGSK",
            "KSSGGSSGGSSGGSSGGSSK",
            "KSSSSSSKIIIIKSSSSSSK",
            "KSSSSSSKWWWWKSSSSSSK",
            "KSSGGSSKWWWWKSSGGSSK",
            "KKKKKKKKKKKKKKKKKKKK"
        )

        // Damaged (25% - 60% HP) - cracks and chipped battlements
        val baseDamaged = arrayOf(
            "KRRK...........K..K",
            "KRK................",
            "KSSK...K...K...KSSK",
            "K.SK...KSSSK...K.SK",
            "KSGK...KSGSK...KSGK",
            "KSSKKK.KSSSKKKKKSSK",
            "KSSSSSS.SSSSSSSSSSK",
            "KSGGSSS.GSSGGSSSGGSK",
            "KSSGGSS..SSGGSSGGSSK",
            "KSSSSSSKIIIIKSSSSSSK",
            "KSS.SSSKWWWWKSSSS.SK",
            "KSSGGSSKWWWWKSSGGSSK",
            "KKKKKKKKKKKKKKKKKKKK"
        )

        // Critical (<25% HP) - breached gates and fire embers
        val baseCritical = arrayOf(
            "...............K..K",
            "....F..........F...",
            "K.SK...F.......K..K",
            "K.SK...K.FSK...K..K",
            "K.GK...KSGSK...K.GK",
            "K.SK...KSSSKKKKK.SK",
            "KSSSSSS.SSSS.SS.SSK",
            "KSGGSSS.GSSG.SSSGGSK",
            "KSS..SS..SSG.SSGGSSK",
            "KSSS.SSKII.IKSSSSSSK",
            "KSS.SSSKW.WWKSSSS.SK",
            "KSSGGSSKW.WWKSSGGSSK",
            "KKKKKKKKKKKKKKKKKKKK"
        )

        baseSprites = arrayOf(
            parseSprite(baseGood, bPal, 4),
            parseSprite(baseDamaged, bPal, 4),
            parseSprite(baseCritical, bPal, 4)
        )
    }

    // ==========================================
    // GETTERS & RENDER HELPERS
    // ==========================================

    fun getEnemyBitmap(type: EnemyType, animTimeMs: Long): Bitmap? {
        val frames = enemyFrames[type] ?: return null
        val frameIdx = ((animTimeMs / 140) % frames.size).toInt()
        return frames[frameIdx]
    }

    fun getTowerBitmap(type: TowerType, level: Int): Bitmap? {
        val tiers = towerSprites[type] ?: return null
        val tierIdx = when {
            level >= 8 -> 2
            level >= 4 -> 1
            else -> 0
        }
        return tiers[tierIdx]
    }

    fun getPlayerBitmap(animTimeMs: Long, isMoving: Boolean, isAttacking: Boolean): Bitmap? {
        if (playerFrames.isEmpty()) return null
        return when {
            isAttacking -> playerFrames[3]
            isMoving -> {
                val step = ((animTimeMs / 150) % 2).toInt()
                if (step == 0) playerFrames[1] else playerFrames[2]
            }
            else -> playerFrames[0]
        }
    }

    fun getBaseBitmap(hpRatio: Float): Bitmap? {
        if (baseSprites.isEmpty()) return null
        return when {
            hpRatio > 0.60f -> baseSprites[0]
            hpRatio > 0.25f -> baseSprites[1]
            else -> baseSprites[2]
        }
    }
}
