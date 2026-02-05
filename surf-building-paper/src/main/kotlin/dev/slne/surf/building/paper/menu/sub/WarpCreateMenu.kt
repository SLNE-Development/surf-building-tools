package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.dialog.showWarpCreateNameDialog
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

fun showWarpCreateMenu(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    warpName: String? = null,
    location: Location? = null,
    displayItem: Material? = null
): SurfChestGui =
    menu(buildText { spacer("Neuen Warp erstellen") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var currentLocation = location ?: if (player is Player) player.location else null
        val currentDisplayItem = displayItem ?: Material.ENDER_PEARL

        val setPositionButton = StaticPane(2, 2, 1, 1).apply {
            fun updatePosition() {
                clear()
                val item = buildItem(Material.COMPASS) {
                    displayName {
                        infoColored("Position: ")
                        if (currentLocation != null) {
                            variableValue("%.1f, %.1f, %.1f".format(
                                currentLocation!!.x,
                                currentLocation!!.y,
                                currentLocation!!.z
                            ))
                        } else {
                            variableValue("Nicht gesetzt")
                        }
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    if (it.whoClicked is Player) {
                        currentLocation = (it.whoClicked as Player).location
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Position wurde auf deine aktuelle Position gesetzt.")
                        }
                        showWarpCreateMenu(
                            it.whoClicked,
                            buildingWorld,
                            warpName,
                            currentLocation,
                            currentDisplayItem
                        )
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
                        if (warpName != null) {
                            variableValue(warpName)
                        } else {
                            error("Nicht gesetzt")
                        }
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    it.whoClicked.showDialog(showWarpCreateNameDialog(buildingWorld, warpName, currentLocation, currentDisplayItem))
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
                    showDisplayItemSelectMenuForWarp(it.whoClicked, currentDisplayItem) { selectedMaterial ->
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Das Display-Item wurde geändert.")
                        }
                        showWarpCreateMenu(it.whoClicked, buildingWorld, warpName, currentLocation, selectedMaterial)
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val createButton = StaticPane(4, 3, 1, 1).apply {
            val item = buildItem(MenuHeads.CHECK) {
                displayName {
                    success("Warp erstellen")
                }
            }

            addItem(GuiItem(item) {
                it.whoClicked.playClickSound()

                if (warpName == null) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Bitte gib einen Namen für den Warp ein.")
                    }
                    return@GuiItem
                }

                if (buildingWorld.warps.any { it.name == warpName }) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Ein Warp mit diesem Namen existiert bereits.")
                    }
                    return@GuiItem
                }

                if (currentLocation == null) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Bitte setze eine Position für den Warp.")
                    }
                    return@GuiItem
                }

                val newWarp = Warp(
                    name = warpName,
                    x = currentLocation!!.x,
                    y = currentLocation!!.y,
                    z = currentLocation!!.z,
                    pitch = currentLocation!!.pitch,
                    yaw = currentLocation!!.yaw,
                    displayItem = currentDisplayItem
                )

                val updatedWorld = buildingWorld.copy(
                    warps = buildingWorld.warps + newWarp
                )

                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp wurde erfolgreich erstellt.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        addPane(setPositionButton)
        addPane(setNameButton)
        addPane(displayItemButton)
        addPane(createButton)
        show(player)
    }

fun showDisplayItemSelectMenuForWarp(
    player: HumanEntity,
    currentDisplayItem: Material?,
    onSelect: (Material) -> Unit
): SurfChestGui {
    return showDisplayItemSelectMenuForCreate(player, null, null, currentDisplayItem, onSelect)
}
