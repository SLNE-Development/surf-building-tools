package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 3

fun showWarpDeleteConfirmMenu(player: HumanEntity, buildingWorld: BuildingWorld, warp: Warp): SurfChestGui =
    menu(buildText { spacer("Warp löschen?") }, height) {
        withOutline(width, height)
        withOutClicks()

        val confirmPane = StaticPane(2, 1, 1, 1).apply {
            addItem(GuiItem(buildItem(MenuHeads.CHECK) {
                displayName {
                    success("Ja, löschen")
                }
                buildLore {
                    emptyLine()
                    line {
                        error("Der Warp '${warp.name}' wird unwiderruflich gelöscht.")
                    }
                }
            }) {
                it.whoClicked.playClickSound()

                val updatedWarps = buildingWorld.warps.filterNot {
                    it.name == warp.name
                }
                val updatedWorld = buildingWorld.copy(warps = updatedWarps)
                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp wurde erfolgreich gelöscht.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        val cancelPane = StaticPane(6, 1, 1, 1).apply {
            addItem(GuiItem(buildItem(MenuHeads.DELETE) {
                displayName {
                    error("Nein, abbrechen")
                }
            }) {
                it.whoClicked.playClickSound()
                showWarpMenu(it.whoClicked, buildingWorld)
            }, 0, 0)
        }

        addPane(confirmPane)
        addPane(cancelPane)
        show(player)
    }
