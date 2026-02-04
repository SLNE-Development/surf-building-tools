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
    type: BuildingWorld.Type? = null
): SurfChestGui =
    menu(buildText { spacer("Bau-Welt erstellen") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        var mayChangedType = type

        val nameButton = StaticPane(
            2, 2, 1, 1
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
                it.whoClicked.showDialog(showBuildingWorldCreateNameDialog(name, mayChangedType))
            }, 0, 0)
        }

        val typeButton = ToggleButton(
            4, 2, 1, 1, mayChangedType != BuildingWorld.Type.VOID
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

        val createButton = StaticPane(6, 2, 1, 1).apply {
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
                    selectedType
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
        addPane(createButton)
        show(player)
    }