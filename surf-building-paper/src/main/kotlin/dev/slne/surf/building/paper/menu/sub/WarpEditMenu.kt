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

fun showWarpEditMenu(player: HumanEntity, buildingWorld: BuildingWorld, warp: Warp): SurfChestGui =
    menu(buildText { spacer("Warp bearbeiten") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var currentLocation = Location(null, warp.x, warp.y, warp.z, warp.yaw, warp.pitch)
        var currentDisplayItem = warp.displayItem
        var currentName = warp.name

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
                        currentLocation = (it.whoClicked as Player).location
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Position wurde auf deine aktuelle Position gesetzt.")
                        }
                        updatePosition()
                        update()
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
                    it.whoClicked.showDialog(showWarpEditNameDialog(buildingWorld, warp) { name ->
                        currentName = name
                        showWarpEditMenu(it.whoClicked, buildingWorld, warp)
                    })
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
                    showDisplayItemSelectMenuForWarp(it.whoClicked, buildingWorld) { selectedMaterial ->
                        currentDisplayItem = selectedMaterial
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Das Display-Item wurde geändert.")
                        }
                        showWarpEditMenu(it.whoClicked, buildingWorld, warp)
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val saveButton = StaticPane(4, 3, 1, 1).apply {
            val item = buildItem(MenuHeads.CHECK) {
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
                    if (it.name == warp.name && it.x == warp.x && it.y == warp.y && it.z == warp.z) {
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
