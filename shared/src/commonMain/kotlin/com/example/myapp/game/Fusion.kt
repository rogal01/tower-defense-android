package com.example.myapp.game

enum class FusionCategory(val displayName: String, val icon: String) {
    ELEMENTAL("Elemental", "🔥"),
    ARCANE("Arcane", "🔮"),
    DARK_ARTS("Dark Arts", "💀"),
    SIEGE("Siege Warfare", "💣")
}

data class FusionDefinition(
    val id: String,
    val name: String,
    val category: FusionCategory,
    val elementA: DamageType,
    val elementB: DamageType,
    val icon: String,
    val description: String,
    val tacticalRole: String
)

data class FirePatch(
    val x: Float,
    val y: Float,
    val radius: Float = 90f,
    var duration: Float = 3.0f,
    val dps: Float = 40f,
    val sourceTower: Tower? = null
)

object FusionCatalog {
    val allFusions: List<FusionDefinition> = listOf(
        // === 1. Elemental Reactions (Core) ===
        FusionDefinition(
            id = "steam_burst",
            name = "Steam Burst",
            category = FusionCategory.ELEMENTAL,
            elementA = DamageType.FIRE,
            elementB = DamageType.ICE,
            icon = "💥",
            description = "Thermal shock causes an explosive vapor burst (130px AoE) that slows surrounding foes for 2s.",
            tacticalRole = "Crowd Control & AoE"
        ),
        FusionDefinition(
            id = "volatile_detonation",
            name = "Volatile Detonation",
            category = FusionCategory.ELEMENTAL,
            elementA = DamageType.FIRE,
            elementB = DamageType.POISON,
            icon = "☣️",
            description = "Ignites concentrated toxic fumes, detonating remaining poison DoT into a massive immediate blast.",
            tacticalRole = "Burst Finisher"
        ),
        FusionDefinition(
            id = "superconductor",
            name = "Superconductor",
            category = FusionCategory.ELEMENTAL,
            elementA = DamageType.ELECTRIC,
            elementB = DamageType.ICE,
            icon = "⚡",
            description = "Conductive frost chains to up to 3 nearby foes, inflicting +25% vulnerability to all damage for 4s.",
            tacticalRole = "Debuff & Multi-Target"
        ),
        FusionDefinition(
            id = "corrosive_shock",
            name = "Corrosive Shock",
            category = FusionCategory.ELEMENTAL,
            elementA = DamageType.ELECTRIC,
            elementB = DamageType.POISON,
            icon = "🧪",
            description = "Electro-chemical reaction stuns target (0.6s) and splatters virulent toxins across surrounding foes.",
            tacticalRole = "Stun & Toxic Spread"
        ),

        // === 2. Arcane Fusions (Magic / Vortex) ===
        FusionDefinition(
            id = "solar_flare",
            name = "Solar Flare",
            category = FusionCategory.ARCANE,
            elementA = DamageType.MAGIC,
            elementB = DamageType.FIRE,
            icon = "☀️",
            description = "Arcane radiance strips enemy shields (+50% bonus damage against shields) and halts HP regeneration for 3.5s.",
            tacticalRole = "Shield Breaker & Anti-Regen"
        ),
        FusionDefinition(
            id = "glacial_singularity",
            name = "Glacial Singularity",
            category = FusionCategory.ARCANE,
            elementA = DamageType.MAGIC,
            elementB = DamageType.ICE,
            icon = "🌌",
            description = "Sub-zero vortex pulls foes within 150px inward and flash-freezes them solid (1.2s stun, 90% slow).",
            tacticalRole = "Mass Vacuum & Hard CC"
        ),
        FusionDefinition(
            id = "overload_flux",
            name = "Overload Flux",
            category = FusionCategory.ARCANE,
            elementA = DamageType.MAGIC,
            elementB = DamageType.ELECTRIC,
            icon = "🔮",
            description = "Binds target and up to 4 nearby enemies into a resonance link, echoing 35% of all damage taken for 4s.",
            tacticalRole = "Damage Multiplication"
        ),
        FusionDefinition(
            id = "astral_decay",
            name = "Astral Decay",
            category = FusionCategory.ARCANE,
            elementA = DamageType.MAGIC,
            elementB = DamageType.POISON,
            icon = "✨",
            description = "Cosmic wither inflicts 35 DPS, +20% damage vulnerability, and grants +50% bonus gold + void wisp on death.",
            tacticalRole = "Boss Shred & Bounty"
        ),

        // === 3. Dark Arts (Necro) ===
        FusionDefinition(
            id = "hellfire",
            name = "Hellfire",
            category = FusionCategory.DARK_ARTS,
            elementA = DamageType.DARK,
            elementB = DamageType.FIRE,
            icon = "💀",
            description = "Cursed infernal flame deals 45 DPS. On death, the soul detonates into a vengeful phantom wisp exploding for 150 damage.",
            tacticalRole = "Single Target & Phantom Burst"
        ),
        FusionDefinition(
            id = "frost_tomb",
            name = "Frost Tomb",
            category = FusionCategory.DARK_ARTS,
            elementA = DamageType.DARK,
            elementB = DamageType.ICE,
            icon = "❄️",
            description = "Abyssal frost crystallizes foe armor, applying +40% physical & explosive vulnerability and halting HP regen for 4s.",
            tacticalRole = "Armor Shred & Anti-Regen"
        ),
        FusionDefinition(
            id = "shadow_surge",
            name = "Shadow Surge",
            category = FusionCategory.DARK_ARTS,
            elementA = DamageType.DARK,
            elementB = DamageType.ELECTRIC,
            icon = "👻",
            description = "Ethereal static shocks and enfeebles target for 5s, halving all damage dealt against base and barricades.",
            tacticalRole = "Objective Protection"
        ),
        FusionDefinition(
            id = "corpse_miasma",
            name = "Corpse Miasma",
            category = FusionCategory.DARK_ARTS,
            elementA = DamageType.DARK,
            elementB = DamageType.POISON,
            icon = "☠️",
            description = "Unleashes necrotic pestilence dealing heavy damage plus 3.5% max HP and applying a 35% slow in 140px AoE.",
            tacticalRole = "Tank & Elite Slaying"
        ),

        // === 4. Siege Warfare (Cannon) ===
        FusionDefinition(
            id = "napalm_conflagration",
            name = "Napalm Conflagration",
            category = FusionCategory.SIEGE,
            elementA = DamageType.EXPLOSIVE,
            elementB = DamageType.FIRE,
            icon = "🔥",
            description = "Concussive blast (150px AoE) that leaves behind a scorched fire crater dealing 40 DPS to enemies on it for 3s.",
            tacticalRole = "Area Denial & Hazard"
        ),
        FusionDefinition(
            id = "emp_shockwave",
            name = "EMP Shockwave",
            category = FusionCategory.SIEGE,
            elementA = DamageType.EXPLOSIVE,
            elementB = DamageType.ELECTRIC,
            icon = "⚡",
            description = "Electromagnetic shockwave cleanses energy shields, cancels ability telegraphs, and silences elite abilities for 2.5s.",
            tacticalRole = "Shield Purge & Silence"
        )
    )

    fun findById(id: String): FusionDefinition? = allFusions.firstOrNull { it.id == id }
}
