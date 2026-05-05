package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.showBuildingWorldEditNameDialog
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.gui.view.warp.WarpsView
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

object WorldEditView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(4).layout(
            "OOOOOOOOO",
            "ON  S  IO",
            "OW      O",
            "OOOOBOOOO"
        ).titleBuilder {
            variableValue("Welt bearbeiten")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val world = worldHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I').renderWith { displayItemSlot(worldHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WorldEditItemView::class.java,
                mutableMapOf("world" to worldHolder.get(click))
            )
        }
        render.layoutSlot('N').renderWith { nameItem(worldHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            val currentWorld = worldHolder.get(click)
            click.player.showDialog(
                showBuildingWorldEditNameDialog(currentWorld) { name ->
                    if (name != null) {
                        val updated = WorldManager.renameWorld(currentWorld, name)
                        viewFrame.open(WorldEditView::class.java, click.player, mutableMapOf("world" to updated))
                    } else {
                        viewFrame.open(WorldEditView::class.java, click.player, mutableMapOf("world" to currentWorld))
                    }
                }
            )
        }
        render.layoutSlot('S').renderWith { statusItem(worldHolder.get(render).status) }
            .updateOnClick()
            .onClick { click ->
                click.playGeneralClickSound()
                val currentWorld = worldHolder.get(click)
                val statusEntries = BuildingWorld.Status.entries
                val currentIndex = statusEntries.indexOf(currentWorld.status)
                val newStatus = if (click.isLeftClick) {
                    statusEntries[(currentIndex + 1) % statusEntries.size]
                } else {
                    statusEntries[(currentIndex - 1 + statusEntries.size) % statusEntries.size]
                }
                val updated = WorldManager.changeStatus(currentWorld, newStatus)
                if (updated) {
                    val savedWorld = WorldManager.findBuildingWorldById(currentWorld.buildingWorldId) ?: currentWorld
                    worldHolder.set(savedWorld, render)
                }
            }
        render.layoutSlot('W').renderWith { warpsItem(worldHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(WarpsView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(WorldView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
    }

    private fun nameItem(world: BuildingWorld) = buildItem(Material.NAME_TAG) {
        displayName {
            primary("Name: ")
            variableValue(world.buildingWorldName)
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                spacer("Klicke, um den Namen der Welt zu ändern")
            }
        }
    }

    private fun statusItem(status: BuildingWorld.Status) = buildItem(status.material) {
        displayName {
            primary("Status: ")
            variableValue(status.displayName)
        }

        buildLore {
            emptyLine()
            BuildingWorld.Status.entries.forEach {
                line {
                    if (it == status) {
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
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                spacer("Linksklick: nächster Status")
            }
            line {
                spacer("»")
                appendSpace()
                spacer("Rechtsklick: vorheriger Status")
            }
        }
    }

    private fun displayItemSlot(world: BuildingWorld) = buildItem(world.displayItem) {
        displayName {
            primary("Anzeigeblock: ")
            translatable(world.displayItem.translationKey())
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                spacer("Klicke, um den Anzeigeblock der Welt zu ändern")
            }
        }
    }

    private fun warpsItem(world: BuildingWorld) = buildItem(Material.ENDER_EYE) {
        displayName {
            primary("Warps verwalten")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                variableKey("Warps: ")
                if (world.warps.isEmpty()) {
                    error("Keine")
                } else {
                    warning(world.warps.size)
                }
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                spacer("Klicke, um die Warps dieser Welt zu verwalten")
            }
        }
    }
}
