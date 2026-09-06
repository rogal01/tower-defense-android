package com.example.myapp.game

/** Classification of merchant goods */
enum class MerchantTier(val displayName: String, val badgeColor: Int) {
    PERMANENT_SYNERGY("PERMANENT SYNERGY", 0xFFAB47BC.toInt()), // Purple
    HIGH_STAKES_PACT("HIGH-STAKES PACT", 0xFFFF5722.toInt()),   // Deep Orange
    TEMPORARY_BOOSTER("3-WAVE BOOSTER", 0xFF00B0FF.toInt())    // Cyan
}

/** Unique IDs for merchant wares */
enum class MerchantItemId {
    THERMAL_SHOCK,
    NEUROTOXIN_CHAIN,
    SOUL_CONDUIT,
    GLASS_CANNON,
    GREED_CURSE,
    BLOOD_OFFERING,
    ALCHEMIST_ELIXIR,
    MIDAS_TONIC,
    FORTRESS_AEGIS,
    CATALYST_AMPLIFIER,
    VOID_PACT,
    ARCANE_SUPERCONDUCTOR
}

/** Data representation of a draftable shop card */
data class MerchantCard(
    val id: MerchantItemId,
    val tier: MerchantTier,
    val title: String,
    val emoji: String,
    val description: String,
    val synergyFormula: String?,
    val cost: Int,
    val durationWaves: Int // 0 = permanent for the run
)

/** Catalog and generation engine for the Wandering Merchant */
object MerchantCatalog {

    val allCards = listOf(
        // --- Tier 1: Permanent Elemental Synergies ---
        MerchantCard(
            id = MerchantItemId.THERMAL_SHOCK,
            tier = MerchantTier.PERMANENT_SYNERGY,
            title = "Thermal Shock",
            emoji = "💥",
            description = "Hitting frozen/chilled enemies with fire or burning enemies with ice causes an explosive 140 AoE shatter burst.",
            synergyFormula = "🔥 Flame + ❄️ Ice",
            cost = 120,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.NEUROTOXIN_CHAIN,
            tier = MerchantTier.PERMANENT_SYNERGY,
            title = "Neurotoxin Arc",
            emoji = "⚡",
            description = "Tesla bolts arcing into poisoned enemies detonate a virulent chemical cloud, spreading poison to all nearby foes.",
            synergyFormula = "☠️ Poison + ⚡ Tesla",
            cost = 110,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.SOUL_CONDUIT,
            tier = MerchantTier.PERMANENT_SYNERGY,
            title = "Soul Singularity",
            emoji = "🌀",
            description = "Raised skeletons and vortex pulls resonate. When a skeleton dies near a vortex, it releases a gravitational shockwave.",
            synergyFormula = "💀 Necro + 🌀 Vortex",
            cost = 115,
            durationWaves = 0
        ),

        // --- Tier 2: High-Stakes Pacts ---
        MerchantCard(
            id = MerchantItemId.GLASS_CANNON,
            tier = MerchantTier.HIGH_STAKES_PACT,
            title = "Glass Cannon",
            emoji = "🗡️",
            description = "All towers and hero deal +50% extra damage, but Base Max HP is reduced by 35%.",
            synergyFormula = "High Risk / High Damage",
            cost = 80,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.GREED_CURSE,
            tier = MerchantTier.HIGH_STAKES_PACT,
            title = "Greed's Bargain",
            emoji = "👹",
            description = "Enemies gain +20% move speed & +15% HP, but creep kills grant 2.5× Gold and +1 bonus Diamond every 20 kills.",
            synergyFormula = "Faster Creeps / 2.5× Gold",
            cost = 75,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.BLOOD_OFFERING,
            tier = MerchantTier.HIGH_STAKES_PACT,
            title = "Blood Offering",
            emoji = "🩸",
            description = "Sacrifice 25 Base HP immediately in exchange for +180 instant Gold.",
            synergyFormula = "Instant Cash Infusion",
            cost = 0,
            durationWaves = 0
        ),

        // --- Tier 3: 3-Wave Boosters ---
        MerchantCard(
            id = MerchantItemId.ALCHEMIST_ELIXIR,
            tier = MerchantTier.TEMPORARY_BOOSTER,
            title = "Alchemist's Draft",
            emoji = "🧪",
            description = "+35% Tower attack speed and +20% hero move speed for the next 3 waves.",
            synergyFormula = "3 Waves Duration",
            cost = 45,
            durationWaves = 3
        ),
        MerchantCard(
            id = MerchantItemId.MIDAS_TONIC,
            tier = MerchantTier.TEMPORARY_BOOSTER,
            title = "Midas Tonic",
            emoji = "🪙",
            description = "+40% bonus Gold rewarded on wave completions for the next 3 waves.",
            synergyFormula = "3 Waves Duration",
            cost = 40,
            durationWaves = 3
        ),
        MerchantCard(
            id = MerchantItemId.FORTRESS_AEGIS,
            tier = MerchantTier.TEMPORARY_BOOSTER,
            title = "Fortress Aegis",
            emoji = "🛡️",
            description = "Instantly restores 40 Base HP and reduces incoming base damage by 20% for the next 3 waves.",
            synergyFormula = "3 Waves Duration",
            cost = 50,
            durationWaves = 3
        ),
        MerchantCard(
            id = MerchantItemId.CATALYST_AMPLIFIER,
            tier = MerchantTier.PERMANENT_SYNERGY,
            title = "Catalyst Amplifier",
            emoji = "⚗️",
            description = "Increases all fusion blast radii by 35%, and Napalm ground fire craters persist for 5s (was 3s).",
            synergyFormula = "All 14 Fusions",
            cost = 130,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.VOID_PACT,
            tier = MerchantTier.HIGH_STAKES_PACT,
            title = "Void Pact",
            emoji = "💀",
            description = "Dark Arts fusions deal +80% damage, but all monsters move 15% faster.",
            synergyFormula = "High-Stakes Dark Pact",
            cost = 90,
            durationWaves = 0
        ),
        MerchantCard(
            id = MerchantItemId.ARCANE_SUPERCONDUCTOR,
            tier = MerchantTier.PERMANENT_SYNERGY,
            title = "Arcane Superconductor",
            emoji = "⚡",
            description = "Superconductor and Overload Flux shred enemy armor, reducing defense by 40% for 6s.",
            synergyFormula = "Electric + Ice / Magic",
            cost = 120,
            durationWaves = 0
        )
    )

    /** Generates 3 balanced shop cards for the current draft */
    fun generateShop(wave: Int, activePermanent: Set<MerchantItemId>): List<MerchantCard> {
        val available = allCards.filter { it.id !in activePermanent }.toMutableList()
        
        val synergies = available.filter { it.tier == MerchantTier.PERMANENT_SYNERGY }.shuffled()
        val pacts = available.filter { it.tier == MerchantTier.HIGH_STAKES_PACT }.shuffled()
        val boosters = available.filter { it.tier == MerchantTier.TEMPORARY_BOOSTER }.shuffled()

        val selected = mutableListOf<MerchantCard>()
        if (synergies.isNotEmpty()) {
            selected.add(synergies.first())
        }
        if (pacts.isNotEmpty()) {
            selected.add(pacts.first())
        }
        if (boosters.isNotEmpty()) {
            selected.add(boosters.first())
        }

        while (selected.size < 3 && available.isNotEmpty()) {
            val remaining = available.filter { it !in selected }
            if (remaining.isEmpty()) break
            selected.add(remaining.random())
        }

        return selected.shuffled()
    }
}
