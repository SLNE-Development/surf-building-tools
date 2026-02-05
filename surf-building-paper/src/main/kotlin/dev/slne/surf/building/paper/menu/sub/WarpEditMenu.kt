package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.dialog.showWarpEditNameDialog
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withHomeButton
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.infoColored
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.util.translatable
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

private const val width = 9
private const val height = 5

fun showWarpEditMenu(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    warp: Warp,
    currentName: String = warp.name,
    currentLocation: Location = Location(null, warp.x, warp.y, warp.z, warp.yaw, warp.pitch),
    currentDisplayItem: Material = warp.displayItem
): SurfChestGui =
    menu(buildText { spacer("Warp bearbeiten") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        val setPositionButton = StaticPane(2, 2, 1, 1).apply {
            fun updatePosition() {
                clear()
                val item = buildItem(Material.COMPASS) {
                    displayName {
                        infoColored("Position: ")
                        variableValue("%.1f, %.1f, %.1f".format(
                            currentLocation.x,
                            currentLocation.y,
                            currentLocation.z
                        ))
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    if (it.whoClicked is Player) {
                        val newLocation = (it.whoClicked as Player).location
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Position wurde auf deine aktuelle Position gesetzt.")
                        }
                        showWarpEditMenu(it.whoClicked, buildingWorld, warp, currentName, newLocation, currentDisplayItem)
                    }
                }, 0, 0)
            }
            updatePosition()
        }

        val setNameButton = StaticPane(4, 2, 1, 1).apply {
            fun updateName() {
                clear()
                val item = buildItem(Material.NAME_TAG) {
                    displayName {
                        infoColored("Name: ")
                        variableValue(currentName)
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    it.whoClicked.showDialog(showWarpEditNameDialog(buildingWorld, warp, currentName, currentLocation, currentDisplayItem))
                }, 0, 0)
            }
            updateName()
        }

        val displayItemButton = StaticPane(6, 2, 1, 1).apply {
            fun updateDisplayItem() {
                clear()
                val item = buildItem(currentDisplayItem) {
                    displayName {
                        infoColored("Display-Item: ")
                        translatable(currentDisplayItem.translationKey())
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    showDisplayItemSelectMenuForWarpEdit(it.whoClicked, currentDisplayItem) { selectedMaterial ->
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Das Display-Item wurde geändert.")
                        }
                        showWarpEditMenu(it.whoClicked, buildingWorld, warp, currentName, currentLocation, selectedMaterial)
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val saveButton = StaticPane(4, 3, 1, 1).apply {
            val item = buildItem(Material.WRITABLE_BOOK) {
                displayName {
                    success("Änderungen speichern")
                }
            }

            addItem(GuiItem(item) {
                it.whoClicked.playClickSound()

                val updatedWarp = Warp(
                    name = currentName,
                    x = currentLocation.x,
                    y = currentLocation.y,
                    z = currentLocation.z,
                    pitch = currentLocation.pitch,
                    yaw = currentLocation.yaw,
                    displayItem = currentDisplayItem
                )

                val updatedWarps = buildingWorld.warps.map {
                    if (it.name == warp.name) {
                        updatedWarp
                    } else {
                        it
                    }
                }

                val updatedWorld = buildingWorld.copy(warps = updatedWarps)
                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp wurde erfolgreich bearbeitet.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        addPane(setPositionButton)
        addPane(setNameButton)
        addPane(displayItemButton)
        addPane(saveButton)
        show(player)
    }

fun showDisplayItemSelectMenuForWarpEdit(
    player: HumanEntity,
    currentDisplayItem: Material?,
    onSelect: (Material) -> Unit
): SurfChestGui {
    return showDisplayItemSelectMenuForCreate(player, null, null, currentDisplayItem, onSelect)
}
