package com.example.myapp.game

import kotlin.math.sqrt

data class Enemy(
    var x: Float,
    var y: Float,
    val speed: Float,
    var hp: Float,
    val maxHp: Float,
    val goldReward: Int,
    val damage: Float,
    val size: Float = 30f,
    val type: EnemyType = EnemyType.GOBLIN,
    val bossType: BossType? = null,
    var hitFlash: Float = 0f,
    var pathIndex: Int = 0,
    var waypointIndex: Int = 1,
    var bossAbilityTimer: Float = 0f,
    var bossAbilityCooldown: Float = 5f,
    var isCharging: Boolean = false,
    var chargeTimer: Float = 0f,
    var hasSplit: Boolean = false,
    var roarSpeedBoost: Float = 1f,
    var roarBoostTimer: Float = 0f
) {
    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx
        val dy = y - ty
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun isAtBase(baseX: Float, baseY: Float): Boolean {
        return distanceTo(baseX, baseY) < size + 30f
    }

    fun isDead(): Boolean = hp <= 0

    /** Slow from ice towers — multiplier applied to speed (0.0 to 1.0) */
    var iceSlowFactor: Float = 1f
    /** Deep freeze timer — when > 0, enemy is near-stopped by ICE ability */
    var deepFreezeTimer: Float = 0f
    /** Regen rate from wave modifier (HP per second) */
    var regenRate: Float = 0f
    /** True if enemy reached the base — should not give rewards */
    var reachedBase: Boolean = false
    /** True if this is an elite enemy (crowned, extra HP/gold) */
    var isElite: Boolean = false
    /** Shield timer — when > 0, enemy takes 70% reduced damage */
    var shieldTimer: Float = 0f
    /** Elite ability type — random special power for elite enemies */
    var eliteAbility: EliteAbility = EliteAbility.NONE
    /** Elite aura timer — tracks periodic effects */
    var eliteAuraTimer: Float = 0f
    /** Burn timer from Flame tower — fire DoT */
    var burnTimer: Float = 0f
    /** Burn DPS from Flame tower */
    var burnDps: Float = 0f
    /** Tar slow timer — when > 0, enemy is slowed by tar trap */
    var tarSlowTimer: Float = 0f
    /** Last tower that hit this enemy — used for kill attribution */
    var lastHitTower: Tower? = null
    /** Berserker rage — speed/damage multiplier increases as HP drops */
    val berserkerRage: Float get() = if (type == EnemyType.BERSERKER) (1f + (1f - (hp / maxHp).coerceIn(0f, 1f)) * 1.5f) else 1f
    /** Commander aura — buff nearby enemies */
    var commanderAuraTimer: Float = 0f
    /** Shapeshifter — current resistance profile cycles every 5s */
    var shapeshiftTimer: Float = 5f
    var shapeshiftPhase: Int = 0
    /** Death animation timer — when > 0, enemy is in death animation (shrink+fade) */
    var deathAnimTimer: Float = 0f
    /** True when death rewards have been processed */
    var deathProcessed: Boolean = false

    /** Display emoji — uses boss-specific emoji if it's a boss */
    val displayEmoji: String get() = bossType?.emoji ?: type.emoji
    /** Display color — uses boss-specific color if it's a boss */
    val displayColor: Int get() = bossType?.color ?: type.color
}

/** Damage resistance lookup — returns multiplier for enemy vs damage type.
 *  > 1.0 = weak (takes MORE damage), < 1.0 = resistant (takes LESS) */
object EnemyResistances {
    fun getMultiplier(enemyType: EnemyType, damageType: DamageType): Float = when (enemyType) {
        EnemyType.SKELETON -> when (damageType) {
            DamageType.PHYSICAL -> 0.5f
            DamageType.MAGIC -> 1.5f
            DamageType.EXPLOSIVE -> 1.3f
            DamageType.FIRE -> 1.3f   // bones burn
            DamageType.DARK -> 0.7f   // undead resist dark
            else -> 1f
        }
        EnemyType.ORC -> when (damageType) {
            DamageType.PHYSICAL -> 0.7f
            DamageType.EXPLOSIVE -> 1.3f
            DamageType.ICE -> 1.2f
            DamageType.FIRE -> 1.2f   // flammable hide
            else -> 1f
        }
        EnemyType.DEMON -> when (damageType) {
            DamageType.ICE -> 1.5f
            DamageType.POISON -> 0.5f
            DamageType.MAGIC -> 0.8f
            DamageType.FIRE -> 0.3f   // fire demons resist fire
            DamageType.DARK -> 0.5f   // demon resist dark
            else -> 1f
        }
        EnemyType.DRAGON -> when (damageType) {
            DamageType.PHYSICAL -> 0.6f
            DamageType.ICE -> 1.4f
            DamageType.MAGIC -> 1.2f
            DamageType.FIRE -> 0.4f   // fire-breathing = fire resist
            DamageType.DARK -> 1.3f   // weak to dark
            else -> 1f
        }
        EnemyType.SHADOW -> when (damageType) {
            DamageType.PHYSICAL -> 0.3f
            DamageType.MAGIC -> 1.5f
            DamageType.ELECTRIC -> 1.3f
            DamageType.FIRE -> 1.4f   // light/fire hurts shadows
            DamageType.DARK -> 0.2f   // shadow = dark immune
            else -> 1f
        }
        EnemyType.GOLEM_SHARD -> when (damageType) {
            DamageType.PHYSICAL -> 0.5f
            DamageType.EXPLOSIVE -> 1.5f
            DamageType.MAGIC -> 1.3f
            DamageType.FIRE -> 0.8f
            else -> 1f
        }
        EnemyType.WISP -> when (damageType) {
            DamageType.PHYSICAL -> 0.6f
            DamageType.ICE -> 1.4f
            DamageType.ELECTRIC -> 0.7f
            DamageType.DARK -> 1.5f   // wisps weak to dark
            else -> 1f
        }
        EnemyType.FAST_SKELETON -> when (damageType) {
            DamageType.PHYSICAL -> 0.6f
            DamageType.MAGIC -> 1.4f
            DamageType.EXPLOSIVE -> 1.2f
            DamageType.FIRE -> 1.3f
            DamageType.DARK -> 0.7f
            else -> 1f
        }
        EnemyType.ARMORED_GOLEM -> when (damageType) {
            DamageType.PHYSICAL -> 0.3f
            DamageType.EXPLOSIVE -> 1.5f
            DamageType.MAGIC -> 1.4f
            DamageType.ELECTRIC -> 1.2f
            DamageType.FIRE -> 1.1f
            DamageType.DARK -> 1.3f   // dark corrodes armor
            else -> 1f
        }
        EnemyType.BERSERKER -> when (damageType) {
            DamageType.ICE -> 1.3f      // cold slows rage
            DamageType.FIRE -> 0.7f     // fire fuels rage
            DamageType.DARK -> 1.2f
            DamageType.PHYSICAL -> 0.8f  // thick hide
            else -> 1f
        }
        EnemyType.COMMANDER -> when (damageType) {
            DamageType.PHYSICAL -> 0.6f  // armored officer
            DamageType.MAGIC -> 1.3f
            DamageType.ELECTRIC -> 1.4f  // metal armor conducts
            DamageType.DARK -> 1.2f
            else -> 1f
        }
        EnemyType.SHAPESHIFTER -> 1f  // handled dynamically via shapeshiftPhase
        else -> 1f
    }

    /** Shapeshifter dynamic resistance — cycles through weakness profiles */
    fun getShapeshifterMultiplier(phase: Int, damageType: DamageType): Float = when (phase % 4) {
        0 -> if (damageType == DamageType.PHYSICAL || damageType == DamageType.EXPLOSIVE) 0.3f else 1.4f
        1 -> if (damageType == DamageType.MAGIC || damageType == DamageType.DARK) 0.3f else 1.4f
        2 -> if (damageType == DamageType.FIRE || damageType == DamageType.ICE) 0.3f else 1.4f
        3 -> if (damageType == DamageType.ELECTRIC || damageType == DamageType.POISON) 0.3f else 1.4f
        else -> 1f
    }
}

enum class EnemyType(val emoji: String, val color: Int) {
    GOBLIN("\uD83D\uDC7A", 0xFF4CAF50.toInt()),
    SKELETON("\uD83D\uDC80", 0xFFBDBDBD.toInt()),
    ORC("\uD83D\uDC79", 0xFF8BC34A.toInt()),
    DEMON("\uD83D\uDD25", 0xFFFF5722.toInt()),
    DRAGON("\uD83D\uDC09", 0xFFFF9800.toInt()),
    BOSS("\u2620\uFE0F", 0xFFE91E63.toInt()),
    // Minion variants for bosses
    MINI_ORC("\uD83D\uDC7A", 0xFF689F38.toInt()),
    MINI_SKELETON("\uD83D\uDC80", 0xFF9E9E9E.toInt()),
    MINI_DEMON("\uD83D\uDC7F", 0xFFE64A19.toInt()),
    MINI_DRAGON("\uD83D\uDC32", 0xFFFFA726.toInt()),
    SHADOW("\uD83D\uDC24", 0xFF37474F.toInt()),
    SLIME("\uD83D\uDFE2", 0xFF00C853.toInt()),
    BAT("\uD83E\uDD87", 0xFF4A148C.toInt()),
    SPIDER("\uD83D\uDD77\uFE0F", 0xFF4E342E.toInt()),
    WISP("\u2728", 0xFF00BCD4.toInt()),
    GOLEM_SHARD("\uD83E\uDEA8", 0xFF795548.toInt()),
    FAST_SKELETON("\uD83D\uDC80", 0xFFE0E0E0.toInt()),
    ARMORED_GOLEM("\uD83E\uDEA8", 0xFF6D4C41.toInt()),
    BERSERKER("\uD83E\uDDBE", 0xFFD32F2F.toInt()),
    COMMANDER("\uD83D\uDC51", 0xFFFF6F00.toInt()),
    SHAPESHIFTER("\uD83C\uDF00", 0xFF7C4DFF.toInt())
}

/** Elite enemy special abilities — randomly assigned to crowned enemies */
enum class EliteAbility(val label: String, val color: Int) {
    NONE("", 0),
    REGEN("Regen", 0xFF66BB6A.toInt()),         // Heals self over time
    SPEED_AURA("Haste", 0xFFFFD740.toInt()),     // Speeds up nearby enemies
    SHIELD("Shield", 0xFF42A5F5.toInt()),         // Periodic damage shield
    THORNS("Thorns", 0xFFFF5252.toInt())          // Damages towers that attack it
}

/** Boss special ability types */
enum class BossAbility {
    CHARGE,       // Speed burst toward base
    SUMMON,       // Spawn extra minions mid-fight
    HEAL,         // Heal self
    AOE_DAMAGE,   // Damage all towers in range
    SHIELD,       // Temporary damage reduction
    ROAR,         // Buff nearby minions speed
    TELEPORT,     // Jump ahead on path
    DRAIN,        // Steal gold from player
    QUAKE,        // Screen shake + slow towers
    SPLIT         // Spawn clones when low HP
}

/** 10 unique bosses — each with themed minion type, stats, and special ability */
enum class BossType(
    val displayName: String,
    val emoji: String,
    val color: Int,
    val minionType: EnemyType,
    val minionCount: Int,
    val baseHp: Float,
    val baseSpeed: Float,
    val baseGold: Float,
    val baseDmg: Float,
    val ability: BossAbility
) {
    ORC_KING(
        "Orc King", "\uD83D\uDC79", 0xFF33691E.toInt(),
        EnemyType.MINI_ORC, 6, 700f, 35f, 120f, 45f,
        BossAbility.CHARGE
    ),
    LICH_LORD(
        "Lich Lord", "\uD83D\uDC80", 0xFF6A1B9A.toInt(),
        EnemyType.MINI_SKELETON, 8, 600f, 45f, 100f, 35f,
        BossAbility.SUMMON
    ),
    DEMON_PRINCE(
        "Demon Prince", "\uD83D\uDE08", 0xFFB71C1C.toInt(),
        EnemyType.MINI_DEMON, 5, 900f, 40f, 150f, 55f,
        BossAbility.AOE_DAMAGE
    ),
    DRAGON_QUEEN(
        "Dragon Queen", "\uD83D\uDC32", 0xFFE65100.toInt(),
        EnemyType.MINI_DRAGON, 4, 1100f, 30f, 200f, 60f,
        BossAbility.ROAR
    ),
    SHADOW_WRAITH(
        "Shadow Wraith", "\uD83D\uDC7B", 0xFF263238.toInt(),
        EnemyType.SHADOW, 10, 550f, 60f, 90f, 30f,
        BossAbility.TELEPORT
    ),
    SLIME_KING(
        "Slime King", "\uD83E\uDDA0", 0xFF00E676.toInt(),
        EnemyType.SLIME, 12, 800f, 25f, 110f, 25f,
        BossAbility.SPLIT
    ),
    VAMPIRE_LORD(
        "Vampire Lord", "\uD83E\uDDDB", 0xFF880E4F.toInt(),
        EnemyType.BAT, 8, 750f, 50f, 130f, 40f,
        BossAbility.DRAIN
    ),
    SPIDER_QUEEN(
        "Spider Queen", "\uD83D\uDD77\uFE0F", 0xFF3E2723.toInt(),
        EnemyType.SPIDER, 7, 650f, 45f, 100f, 35f,
        BossAbility.SUMMON
    ),
    FROST_TITAN(
        "Frost Titan", "\u2744\uFE0F", 0xFF0288D1.toInt(),
        EnemyType.WISP, 6, 1200f, 20f, 180f, 50f,
        BossAbility.QUAKE
    ),
    STONE_GOLEM(
        "Stone Golem", "\uD83E\uDEA8", 0xFF5D4037.toInt(),
        EnemyType.GOLEM_SHARD, 5, 1400f, 15f, 220f, 65f,
        BossAbility.SHIELD
    )
}
