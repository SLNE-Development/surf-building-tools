package dev.slne.surf.building.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.dialog.showBuildingWorldCreateNameDialog
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.CentralMenu
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.plugin
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

object WorldCreateView : View() {
    private val nameHolder = initialState<String>("name")
    private val typeHolder = initialState<BuildingWorld.Type>("type")
    private val modifiedTypeHolder = mutableState(BuildingWorld.Type.VOID)
    private val displayItemHolder = initialState<Material>("displayItem")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3)
            .titleBuilder {
                primary("Welt erstellen")
            }
            .layout(
                "OOOOOOOOO",
                "ON  T  IO",
                "BOOOCOOOO",
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        modifiedTypeHolder.set(typeHolder.get(render), render)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(CentralMenu::class.java)
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('N').renderWith { nameItem(nameHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            click.player.showDialog(
                showBuildingWorldCreateNameDialog(
                    nameHolder.get(click),
                    modifiedTypeHolder.get(click),
                    displayItemHolder.get(click)
                )
            )
        }
        render.layoutSlot('T').renderWith { typeItem(modifiedTypeHolder.get(render)) }
            .updateOnClick()
            .onClick { click ->
                click.playGeneralClickSound()
                val currentType =
                    modifiedTypeHolder.get(click) ?: BuildingWorld.Type.entries.first()

                if (click.isLeftClick) {
                    modifiedTypeHolder.set(currentType.next(), render)
                } else {
                    modifiedTypeHolder.set(currentType.previous(), render)
                }
            }
        render.layoutSlot('I').renderWith { displayItem(displayItemHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WorldCreateItemView::class.java, mutableMapOf(
                        "name" to nameHolder.get(click),
                        "type" to modifiedTypeHolder.get(click),
                        "displayItem" to displayItemHolder.get(click)
                    )
                )
            }
        render.layoutSlot('C').renderWith {
            createItem(
                nameHolder.get(render),
                modifiedTypeHolder.get(render),
                displayItemHolder.get(render)
            )
        }.onClick { click ->
            click.playGeneralClickSound()
            val name = nameHolder.get(click)
            val type = modifiedTypeHolder.get(click)
            val displayItem = displayItemHolder.get(click)

            if (name != null && type != null && displayItem != null) {
                click.openForPlayer(CentralMenu::class.java)
                click.player.sendText {
                    appendInfoPrefix()
                    info("Die Welt wird erstellt...")
                }

                plugin.launch {
                    val world = WorldManager.createWorld(
                        name,
                        click.player.name,
                        click.player.uniqueId,
                        type,
                        displayItem
                    )

                    if (world == null) {
                        click.player.sendText {
                            appendErrorPrefix()
                            error("Die Welt konnte nicht erstellt werden. Bitte versuche es später erneut.")
                        }
                    } else {
                        click.player.sendText {
                            appendSuccessPrefix()
                            success("Die Welt wurde erfolgreich erstellt!")
                        }
                    }
                }
            }
        }
    }

    private fun nameItem(current: String?) = buildItem(Material.NAME_TAG) {
        displayName {
            primary("Name: ")
            info(current ?: "Nicht gesetzt")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um den Namen der Welt festzulegen")
            }
        }
    }

    private fun typeItem(current: BuildingWorld.Type?) = buildItem(Material.COMPASS) {
        displayName {
            primary("Typ: ")
            info(current?.displayName ?: "Nicht gesetzt")
        }

        buildLore {
            emptyLine()
            BuildingWorld.Type.entries.forEach {
                line {
                    if (current == it) {
                        spacer("✔")
                        appendSpace()
                        variableValue(it.displayName, TextDecoration.BOLD)
                    } else {
                        spacer("»")
                        appendSpace()
                        white(it.displayName)
                    }
                }
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                primary("Linksklick: ")
                white("nächster Typ")
            }
            line {
                spacer("»")
                appendSpace()
                primary("Rechtsklick: ")
                white("vorheriger Typ")
            }
        }
    }

    private fun displayItem(current: Material?) = buildItem(current ?: Material.GRASS_BLOCK) {
        displayName {
            primary("Anzeigeblock: ")
            if (current != null) {
                translatable(current.translationKey())
            } else {
                info("Nicht gesetzt")
            }
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um den Anzeigeblock der Welt festzulegen")
            }
        }
    }

    private fun createItem(name: String?, type: BuildingWorld.Type?, displayItem: Material?) =
        MenuHeads.CREATE_BUTTON.clone().apply {
            displayName {
                primary("Welt erstellen")
            }

            buildLore {
                emptyLine()
                line {
                    spacer("»")
                    appendSpace()
                    variableKey("Name: ")
                    variableValue(name ?: "Nicht gesetzt")
                }

                line {
                    spacer("»")
                    appendSpace()
                    variableKey("Typ: ")
                    variableValue(type?.displayName ?: "Nicht gesetzt")
                }

                line {
                    spacer("»")
                    appendSpace()
                    variableKey("Anzeigeblock: ")
                    if (displayItem != null) {
                        translatable(displayItem.translationKey())
                    } else {
                        variableValue("Nicht gesetzt")
                    }
                }

                if (name == null || type == null || displayItem == null) {
                    emptyLine()
                    line {
                        error("✘ Bitte alle Werte festlegen, um die Welt erstellen zu können")
                    }
                } else {
                    emptyLine()
                    line {
                        success("✔ Klicke, um die Welt mit den angegebenen Werten zu erstellen")
                    }
                }
            }
        }
}