package dev.slne.surf.building.paper.menu

import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 5

fun showBuildingWorldCreateMenu(player: HumanEntity) = menu(buildText { }, height) {
    withOutline(width, height)
    withOutClicks()
    withHomeButton(height)


    show(player)
}