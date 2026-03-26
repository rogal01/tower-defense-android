package com.example.myapp.game

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
    fun update(dt: Float) {
        if (hitFlash > 0) hitFlash -= dt
        if (roarBoostTimer > 0) {
            roarBoostTimer -= dt
            if (roarBoostTimer <= 0f) roarSpeedBoost = 1f
        }
    }

    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx
        val dy = y - ty
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun isAtBase(baseX: Float, baseY: Float): Boolean {
        return distanceTo(baseX, baseY) < size + 30f
    }

    fun isDead(): Boolean = hp <= 0

    /** Display emoji — uses boss-specific emoji if it's a boss */
    val displayEmoji: String get() = bossType?.emoji ?: type.emoji
    /** Display color — uses boss-specific color if it's a boss */
    val displayColor: Int get() = bossType?.color ?: type.color
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
    GOLEM_SHARD("\uD83E\uDEA8", 0xFF795548.toInt())
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
