package dev.slne.surf.building.paper.menu

import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

private const val width = 9
private const val height = 6

fun showBuildingWorldCreateMenu() = menu(buildText { }, height) {
    
}