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
    );

    val prefKey: String get() = "relic_unlocked_${name}"
}
