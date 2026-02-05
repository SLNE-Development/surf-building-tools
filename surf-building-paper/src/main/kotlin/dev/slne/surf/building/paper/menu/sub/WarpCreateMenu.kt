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

fun showWarpCreateMenu(
    player: HumanEntity, 
    buildingWorld: BuildingWorld,
    currentName: String? = null,
    currentDisplayItem: Material = Material.COMPASS
): SurfChestGui =
    menu(buildText { spacer("Warp erstellen") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var updatedName: String? = currentName
        var updatedDisplayItem = currentDisplayItem

        val nameButton = StaticPane(2, 2, 1, 1).apply {
            fun updateName() {
                clear()
                val name = updatedName ?: "Warp"
                val item = buildItem(Material.NAME_TAG) {
                    displayName {
                        infoColored("Name: ")
                        variableValue(name)
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    it.whoClicked.showDialog(showWarpNameDialog(buildingWorld, null) { newName ->
                        if (newName != null) {
                            updatedName = newName
                            showWarpCreateMenu(player, buildingWorld, newName, updatedDisplayItem)
                        }
                    })
                }, 0, 0)
            }
            updateName()
        }

        val displayItemButton = StaticPane(4, 2, 1, 1).apply {
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
                        showWarpCreateMenu(player, buildingWorld, updatedName, selectedMaterial)
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val saveButton = StaticPane(6, 2, 1, 1).apply {
            addItem(GuiItem(MenuHeads.WRITABLE_BOOK.clone().apply {
                displayName {
                    success("Warp speichern")
                }
            }) {
                it.whoClicked.playClickSound()

                val name = updatedName
                if (name.isNullOrBlank()) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Bitte gib einen Namen für den Warp ein.")
                    }
                    return@GuiItem
                }

                if (buildingWorld.warps.any { warp -> warp.name == name }) {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Ein Warp mit diesem Namen existiert bereits.")
                    }
                    return@GuiItem
                }

                val player = it.whoClicked as? Player ?: return@GuiItem
                val location = player.location

                val newWarp = Warp(
                    name = name,
                    x = location.x,
                    y = location.y,
                    z = location.z,
                    pitch = location.pitch,
                    yaw = location.yaw,
                    displayItem = updatedDisplayItem
                )

                val updatedWorld = buildingWorld.copy(
                    warps = buildingWorld.warps + newWarp
                )
                buildingWorldService.saveBuildingWorld(updatedWorld)

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Der Warp ")
                    variableValue(name)
                    success(" wurde erstellt.")
                }

                showWarpMenu(it.whoClicked, updatedWorld)
            }, 0, 0)
        }

        addPane(nameButton)
        addPane(displayItemButton)
        addPane(saveButton)
        show(player)
    }
