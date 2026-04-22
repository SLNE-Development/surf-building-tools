package dev.slne.surf.building.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.dialog.showBuildingWorldCreateNameDialog
import dev.slne.surf.building.gui.view.CentralMenu
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.plugin
import dev.slne.surf.building.service.BuildingWorldService
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

object WorldCreateView : View() {
    private val nameHolder = initialState<String?>("name")
    private val typeHolder = initialState<BuildingWorld.Type?>("type")
    private val displayItemHolder = initialState<Material?>("displayItem")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3)
            .titleBuilder {
                variableValue("Welt erstellen")
            }
            .layout(
                "OOOOOOOOO",
                "ON  T  IO",
                "BOOOCOOOO",
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
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
                    typeHolder.get(click),
                    displayItemHolder.get(click)
                )
            )
        }
        render.layoutSlot('T').renderWith { typeItem(typeHolder.get(render)) }.updateOnClick()
            .onClick { click ->
                click.playGeneralClickSound()
                val currentType = typeHolder.get(click) ?: BuildingWorld.Type.entries.first()

                if (click.isLeftClick) {
                    typeHolder.set(currentType.next(), render)
                } else {
                    typeHolder.set(currentType.previous(), render)
                }
            }
        render.layoutSlot('I').renderWith { displayItem(displayItemHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WorldCreateItemView::class.java, mutableMapOf(
                        "name" to nameHolder.get(click),
                        "type" to typeHolder.get(click),
                        "displayItem" to displayItemHolder.get(click)
                    )
                )
            }
        render.layoutSlot('C').renderWith {
            createItem(
                nameHolder.get(render),
                typeHolder.get(render),
                displayItemHolder.get(render)
            )
        }.onClick { click ->
            click.playGeneralClickSound()
            val name = nameHolder.get(click)
            val type = typeHolder.get(click)
            val displayItem = displayItemHolder.get(click)

            if (name != null && type != null && displayItem != null) {
                click.openForPlayer(CentralMenu::class.java)
                click.player.sendText {
                    appendInfoPrefix()
                    info("Die Welt wird erstellt...")
                }

                plugin.launch {
                    val world = BuildingWorldService.createBuildingWorld(
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
                spacer("Klicke, um den Namen der Welt festzulegen")
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
                        appendSpace()
                        appendSpace()
                        spacer("-")
                        appendSpace()
                        variableValue(it.displayName, TextDecoration.BOLD)
                    } else {
                        spacer("»")
                        appendSpace()
                        variableValue(it.displayName)
                    }
                }
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
                spacer("Klicke, um den Anzeigeblock der Welt festzulegen")
            }
        }
    }

    private fun createItem(name: String?, type: BuildingWorld.Type?, displayItem: Material?) =
        buildItem(Material.GREEN_CONCRETE) {
            displayName {
                primary("Welt erstellen")
            }

            buildLore {
                line {
                    spacer("»")
                    appendSpace()
                    info("Name: ")
                    note(name ?: "Nicht gesetzt")
                }

                line {
                    spacer("»")
                    appendSpace()
                    info("Typ: ")
                    note(type?.displayName ?: "Nicht gesetzt")
                }

                line {
                    spacer("»")
                    appendSpace()
                    info("Anzeigeblock: ")
                    if (displayItem != null) {
                        translatable(displayItem.translationKey())
                    } else {
                        note("Nicht gesetzt")
                    }
                }

                if (name == null || type == null || displayItem == null) {
                    line {
                        spacer("»")
                        appendSpace()
                        spacer("Bitte alle Werte festlegen, um die Welt erstellen zu können")
                    }
                } else {
                    line {
                        spacer("»")
                        appendSpace()
                        spacer("Klicke, um die Welt mit den angegebenen Werten zu erstellen")
                    }
                }
            }
        }
}