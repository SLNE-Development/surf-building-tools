package dev.slne.surf.building.paper.menu.util

import org.bukkit.Material

/**
 * Converts a Material name to a human-readable display name.
 * Example: GRASS_BLOCK -> "Grass block"
 */
fun Material.toDisplayName(): String =
    this.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
