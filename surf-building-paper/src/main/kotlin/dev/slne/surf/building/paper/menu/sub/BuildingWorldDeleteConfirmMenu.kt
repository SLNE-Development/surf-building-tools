package dev.slne.surf.building.paper.menu.sub

import com.github.shynixn.mccoroutine.folia.launch
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.showBuildingWorldMenu
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.plugin.Plugin

private const val width = 9
private const val height = 3

fun showBuildingWorldDeleteConfirmMenu(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    plugin: Plugin
): SurfChestGui =
    menu(buildText { spacer("Bauwelt löschen?") }, height) {
        withOutline(width, height)
        withOutClicks()

        val cancelButton = StaticPane(2, 1, 1, 1).apply {
            addItem(GuiItem(buildItem(Material.RED_STAINED_GLASS_PANE) {
                displayName {
                    error("Abbrechen")
                }
            }) {
                it.whoClicked.playClickSound()
                showBuildingWorldMenu(it.whoClicked)
            }, 0, 0)
        }

        val confirmButton = StaticPane(6, 1, 1, 1).apply {
            addItem(GuiItem(MenuHeads.DELETE.apply {
                displayName {
                    error("Löschen bestätigen")
                }
            }) {
                it.whoClicked.closeInventory()
                it.whoClicked.playClickSound()

                it.whoClicked.sendText {
                    appendInfoPrefix()
                    info("Die Bauwelt wird gelöscht...")
                }

                plugin.launch {
                    val success = buildingWorldService.deleteBuildingWorld(buildingWorld.buildingWorldId)

                    if (success) {
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Die Bauwelt wurde erfolgreich gelöscht.")
                        }
                    } else {
                        it.whoClicked.sendText {
                            appendErrorPrefix()
                            error("Die Bauwelt konnte nicht gelöscht werden.")
                        }
                    }
                }
            }, 0, 0)
        }

        addPane(cancelButton)
        addPane(confirmButton)
        show(player)
    }
