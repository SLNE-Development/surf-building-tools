package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.showWarpMenu
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 3

fun showWarpDeleteConfirmMenu(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    warp: Warp
): SurfChestGui =
    menu(buildText { spacer("Warp löschen?") }, height) {
        withOutline(width, height)
        withOutClicks()

        val cancelButton = StaticPane(2, 1, 1, 1).apply {
            addItem(GuiItem(buildItem(Material.RED_STAINED_GLASS_PANE) {
                displayName {
                    error("Abbrechen")
                }
            }) {
                it.whoClicked.playClickSound()
                showWarpMenu(it.whoClicked, buildingWorld)
            }, 0, 0)
        }

        val confirmButton = StaticPane(6, 1, 1, 1).apply {
            addItem(GuiItem(MenuHeads.DELETE.clone().apply {
                displayName {
                    error("Löschen bestätigen")
                }
            }) {
                it.whoClicked.closeInventory()
                it.whoClicked.playClickSound()

                val updatedWorld = buildingWorld.copy(
                    warps = buildingWorld.warps.filter { it.name != warp.name }
                )
                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp ")
                    variableValue(warp.name)
                    success(" wurde gelöscht.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        addPane(cancelButton)
        addPane(confirmButton)
        show(player)
    }
