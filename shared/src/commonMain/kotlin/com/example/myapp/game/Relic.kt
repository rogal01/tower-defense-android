package com.example.myapp.game

/**
 * Legendary Artifacts purchasable with Diamonds in the Relic Vault.
 * Once unlocked, these provide powerful, permanent build-defining effects across runs.
 */
enum class RelicId(
    val title: String,
    val description: String,
    val emoji: String,
    val diamondCost: Int
) {
    ZEPHYR_GREAVES(
        "Zephyr Greaves",
        "+35% Hero movement speed. Emits a Gale Aura that slows nearby enemies by 25%.",
        "\u26A1",
        25
    ),
    ARTEMIS_QUIVER(
        "Artemis Quiver",
        "Hero attacks fire a 3-arrow spread volley with +15% innate Critical Strike chance.",
        "\uD83C\uDFF9",
        30
    ),
    CHRONO_HOURGLASS(
        "Chrono Hourglass",
        "-25% Spell cooldowns (Fireball, Freeze, Lightning). Crits trigger a 2s global slow.",
        "\u23F3",
        35
    ),
    AEGIS_OF_DAWN(
        "Aegis of the Dawn",
        "Base gains a 100 HP divine energy shield every wave. Hero aura repairs Base for 3 HP every 5s.",
        "\uD83D\uDEE1\uFE0F",
        30
    ),
    DEMOLITION_SATCHEL(
        "Demolition Satchel",
        "All traps (Spikes, Tar, Mines) gain +2 Max Uses & +40% explosion damage and radius.",
        "\uD83D\uDCA3",
        25
    ),
    MIDAS_CRUCIBLE(
        "Midas Crucible",
        "+100 Starting gold, +30% gold from all kills, and +25% higher Diamond drop chance.",
        "\uD83C\uDFFA",
        20
    ),
    PRISMATIC_CATALYST(
        "Prismatic Catalyst",
        "All 14 Elemental & Arcane Fusions deal +40% damage, +25% radius, and have a 20% chance to drop +1 Diamond.",
        "\uD83D\uDD2E",
        35
    ),
    GRIMOIRE_OF_CONDUIT(
        "Grimoire of Conduit",
        "Overload Flux binds up to 7 enemies (was 5) and echoes 60% of damage taken across all linked foes.",
        "\uD83D\uDCD6",
        30
    ),
    ASTRAL_HARVESTER(
        "Astral Harvester",
        "Astral Decay void wisps and Hellfire phantom wisps deal 2.5× damage and vacuum nearby foes.",
        "\u2728",
        28
    ),
    TITAN_WARHORN(
        "Titan Warhorn",
        "Hero sword swings cleave all enemies in a 140° frontal arc and knock back non-boss enemies.",
        "\uD83D\uDCEF",
        32
    ),
    CHRONO_SINGULARITY(
        "Chrono Singularity",
        "Extends boss ability telegraphs by +1.2s and slows elite ability recharge by 40%.",
        "\u23F1\uFE0F",
        40
    ),
    ALCHEMIST_PHILOSOPHER_STONE(
        "Philosopher's Stone",
        "Every 400 gold collected during a run converts to +1 permanent Diamond upon completion (up to +15 \uD83D\uDC8E).",
        "\uD83D\uDC8E",
        30
    );

    val prefKey: String get() = "relic_unlocked_${name}"
}
