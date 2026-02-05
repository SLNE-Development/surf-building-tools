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

fun showWarpEditMenu(player: HumanEntity, buildingWorld: BuildingWorld, warp: Warp): SurfChestGui =
    menu(buildText { spacer("Warp bearbeiten") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var currentName = warp.name
        var currentDisplayItem = warp.displayItem
        var shouldUpdatePosition = false

        val nameButton = StaticPane(1, 2, 1, 1).apply {
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
                    it.whoClicked.showDialog(showWarpNameDialog(buildingWorld, warp) { newName ->
                        if (newName != null) {
                            currentName = newName
                            showWarpEditMenu(player, buildingWorld, warp.copy(name = currentName))
                        }
                    })
                }, 0, 0)
            }
            updateName()
        }

        val displayItemButton = StaticPane(3, 2, 1, 1).apply {
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
                    showDisplayItemSelectMenuForWarp(
                        it.whoClicked,
                        currentDisplayItem
                    ) { selectedMaterial ->
                        currentDisplayItem = selectedMaterial
                        it.whoClicked.sendText {
                            appendSuccessPrefix()
                            success("Das Display-Item wurde geändert.")
                        }
                        showWarpEditMenu(
                            it.whoClicked,
                            buildingWorld,
                            warp.copy(displayItem = currentDisplayItem)
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
                shouldUpdatePosition = true

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

                if (currentName.isBlank()) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Der Name darf nicht leer sein.")
                    }
                    return@GuiItem
                }

                if (currentName != warp.name && buildingWorld.warps.any { w -> w.name == currentName }) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Ein Warp mit diesem Namen existiert bereits.")
                    }
                    return@GuiItem
                }

                val player = it.whoClicked as? Player
                val updatedWarp = if (shouldUpdatePosition && player != null) {
                    val location = player.location
                    warp.copy(
                        name = currentName,
                        x = location.x,
                        y = location.y,
                        z = location.z,
                        pitch = location.pitch,
                        yaw = location.yaw,
                        displayItem = currentDisplayItem
                    )
                } else {
                    warp.copy(
                        name = currentName,
                        displayItem = currentDisplayItem
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
