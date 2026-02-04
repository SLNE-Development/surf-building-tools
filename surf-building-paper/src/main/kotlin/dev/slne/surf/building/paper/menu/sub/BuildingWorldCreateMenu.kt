package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton
import dev.slne.surf.building.paper.menu.dialog.showBuildingWorldCreateNameDialog
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withHomeButton
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.infoColored
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.util.translatable
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 5

fun showBuildingWorldCreateMenu(
    player: HumanEntity,
    name: String? = null,
    type: BuildingWorld.Type? = null,
    displayItem: Material? = null
): SurfChestGui =
    menu(buildText { spacer("Bau-Welt erstellen") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var mayChangedType = type
        var mayChangedDisplayItem = displayItem ?: Material.GRASS_BLOCK

        val nameButton = StaticPane(
            1, 2, 1, 1
        ).apply {
            val displayName = buildItem(Material.NAME_TAG) {
                displayName {
                    infoColored("Name: ")
                    if (name != null) {
                        variableValue(name)
                    } else {
                        error("Nicht gesetzt")
                    }
                }
            }

            addItem(GuiItem(displayName) {
                it.whoClicked.playClickSound()
                it.whoClicked.showDialog(showBuildingWorldCreateNameDialog(name, mayChangedType, mayChangedDisplayItem))
            }, 0, 0)
        }

        val typeButton = ToggleButton(
            3, 2, 1, 1, mayChangedType != BuildingWorld.Type.VOID
        ).apply {
            setEnabledItem(GuiItem(MenuHeads.WORLD.clone().apply {
                displayName {
                    infoColored("Weltentyp: ")
                    variableValue("Leer")
                }
            }) {
                it.whoClicked.playClickSound()
                mayChangedType = BuildingWorld.Type.FLAT

                it.whoClicked.sendText {
                    appendInfoPrefix()
                    info("Der Weltentyp wurde auf 'Flach' gesetzt.")
                }
            })

            setDisabledItem(GuiItem(MenuHeads.WORLD.clone().apply {
                displayName {
                    infoColored("Weltentyp: ")
                    variableValue("Flach")
                }
            }) {
                it.whoClicked.playClickSound()
                mayChangedType = BuildingWorld.Type.VOID

                it.whoClicked.sendText {
                    appendInfoPrefix()
                    info("Der Weltentyp wurde auf 'Leer' gesetzt.")
                }
            })
        }

        val displayItemButton = StaticPane(
            5, 2, 1, 1
        ).apply {
            fun updateDisplayItem() {
                clear()
                val item = buildItem(mayChangedDisplayItem) {
                    displayName {
                        infoColored("Display-Item: ")
                        translatable(mayChangedDisplayItem.translationKey())
                    }
                }

                addItem(GuiItem(item) {
                    it.whoClicked.playClickSound()
                    showDisplayItemSelectMenuForCreate(it.whoClicked, name, mayChangedType, mayChangedDisplayItem) { selectedMaterial ->
                        mayChangedDisplayItem = selectedMaterial
                        it.whoClicked.sendText {
                            appendInfoPrefix()
                            info("Das Display-Item wurde geändert.")
                        }
                        showBuildingWorldCreateMenu(it.whoClicked, name, mayChangedType, mayChangedDisplayItem)
                    }
                }, 0, 0)
            }
            updateDisplayItem()
        }

        val createButton = StaticPane(7, 2, 1, 1).apply {
            addItem(GuiItem(MenuHeads.CHECK.apply {
                displayName {
                    infoColored("Bauwelt erstellen")
                }
            }) {
                val selectedType = if (typeButton.isEnabled) {
                    BuildingWorld.Type.VOID
                } else {
                    BuildingWorld.Type.FLAT
                }

                val finalName = name ?: run {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Bau-Welt konnte nicht erstellt werden, da kein Name gesetzt wurde!")
                    }

                    player.playSound(true) {
                        type(Sound.ENTITY_VILLAGER_NO)
                    }
                    return@GuiItem
                }

                it.whoClicked.playClickSound()
                it.whoClicked.closeInventory()

                player.sendText {
                    appendInfoPrefix()
                    info("Die Bauwelt wird erstellt...")
                }


                val success = buildingWorldService.createBuildingWorld(
                    finalName,
                    player.name,
                    player.uniqueId,
                    selectedType,
                    mayChangedDisplayItem
                )

                if (success != null) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich erstellt!")
                        append {
                            spacer(" [")
                            success("Beitreten")
                            spacer("]")
                            clickEvent(ClickEvent.callback {
                                buildingWorldService.joinAndOrLoadBuildingWorld(
                                    player,
                                    success.buildingWorldId
                                )
                            })
                        }
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Es ist ein Fehler bei der Erstellung der Bau-Welt aufgetreten!")
                    }
                }
            }, 0, 0)
        }

        addPane(typeButton)
        addPane(nameButton)
        addPane(displayItemButton)
        addPane(createButton)
        show(player)
    }