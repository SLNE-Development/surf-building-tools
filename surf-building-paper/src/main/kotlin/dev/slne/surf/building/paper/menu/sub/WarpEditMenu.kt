package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.dialog.showWarpNameDialog
import dev.slne.surf.building.paper.menu.showWarpMenu
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
    currentDisplayItem: Material = warp.displayItem,
    positionNeedsUpdate: Boolean = false
): SurfChestGui =
    menu(buildText { spacer("Warp bearbeiten") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var updatedName = currentName
        var updatedDisplayItem = currentDisplayItem
        var updatePosition = positionNeedsUpdate

        val nameButton = StaticPane(1, 2, 1, 1).apply {
            fun updateName() {
                clear()
                val item = buildItem(Material.NAME_TAG) {
                    displayName {
                        infoColored("Name: ")
                        variableValue(updatedName)
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    it.whoClicked.showDialog(showWarpNameDialog(buildingWorld, warp) { newName ->
                        if (newName != null) {
                            updatedName = newName
                            showWarpEditMenu(player, buildingWorld, warp, newName, updatedDisplayItem, updatePosition)
                        }
                    })
                }, 0, 0)
            }
            updateName()
        }

        val displayItemButton = StaticPane(3, 2, 1, 1).apply {
            fun updateDisplayItem() {
                clear()
                val item = buildItem(updatedDisplayItem) {
                    displayName {
                        infoColored("Display-Item: ")
                        translatable(updatedDisplayItem.translationKey())
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    showDisplayItemSelectMenuForWarp(
                        it.whoClicked,
                        updatedDisplayItem
                    ) { selectedMaterial ->
                        updatedDisplayItem = selectedMaterial
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Das Display-Item wurde geändert.")
                        }
                        showWarpEditMenu(
                            it.whoClicked,
                            buildingWorld,
                            warp,
                            updatedName,
                            selectedMaterial,
                            updatePosition
                        )
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val positionButton = StaticPane(5, 2, 1, 1).apply {
            val item = buildItem(Material.ENDER_PEARL) {
                displayName {
                    infoColored("Position aktualisieren")
                }
            }

            addItem(GuiItem(item) {
                it.whoClicked.playClickSound()
                updatePosition = true

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Die Position des Warps wird beim Speichern aktualisiert.")
                }
            }, 0, 0)
        }

        val saveButton = StaticPane(7, 2, 1, 1).apply {
            addItem(GuiItem(MenuHeads.WRITABLE_BOOK.clone().apply {
                displayName {
                    success("Änderungen speichern")
                }
            }) {
                it.whoClicked.playClickSound()

                if (updatedName.isBlank()) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Der Name darf nicht leer sein.")
                    }
                    return@GuiItem
                }

                if (updatedName != warp.name && buildingWorld.warps.any { w -> w.name == updatedName }) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Ein Warp mit diesem Namen existiert bereits.")
                    }
                    return@GuiItem
                }

                val player = it.whoClicked as? Player
                val updatedWarp = if (updatePosition && player != null) {
                    val location = player.location
                    warp.copy(
                        name = updatedName,
                        x = location.x,
                        y = location.y,
                        z = location.z,
                        pitch = location.pitch,
                        yaw = location.yaw,
                        displayItem = updatedDisplayItem
                    )
                } else {
                    warp.copy(
                        name = updatedName,
                        displayItem = updatedDisplayItem
                    )
                }

                val updatedWorld = buildingWorld.copy(
                    warps = buildingWorld.warps.map { if (it.name == warp.name) updatedWarp else it }
                )
                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp wurde aktualisiert.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        addPane(nameButton)
        addPane(displayItemButton)
        addPane(positionButton)
        addPane(saveButton)
        show(player)
    }
