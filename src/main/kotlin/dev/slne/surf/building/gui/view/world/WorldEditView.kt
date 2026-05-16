package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.showBuildingWorldEditNameDialog
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.member.MembersView
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.gui.view.warp.WarpsView
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

@Suppress("NULLABILITY_MISMATCH_BASED_ON_EXPLICIT_TYPE_ARGUMENTS_FOR_JAVA")
object WorldEditView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val updatableWorldHolder = mutableState<BuildingWorld?>(null)

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "ON SMW IO",
            "OOOOBOOOO"
        ).titleBuilder {
            variableValue("Welt bearbeiten")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val world = worldHolder.get(render)
        updatableWorldHolder.set(world, render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I').renderWith { displayItemSlot(updatableWorldHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WorldEditItemView::class.java,
                    mutableMapOf("world" to updatableWorldHolder.get(click))
                )
            }
        render.layoutSlot('N').renderWith { nameItem(updatableWorldHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                val currentWorld = updatableWorldHolder.get(click)
                click.player.showDialog(
                    showBuildingWorldEditNameDialog(currentWorld) { name ->
                        if (name != null) {
                            val updated = WorldManager.renameWorld(currentWorld, name)
                            viewFrame.open(
                                WorldEditView::class.java,
                                click.player,
                                mutableMapOf("world" to updated)
                            )
                        } else {
                            viewFrame.open(
                                WorldEditView::class.java,
                                click.player,
                                mutableMapOf("world" to currentWorld)
                            )
                        }
                    }
                )
            }
        render.layoutSlot('S').renderWith { statusItem(updatableWorldHolder.get(render).status) }
            .updateOnClick()
            .onClick { click ->
                click.playGeneralClickSound()
                val currentWorld = updatableWorldHolder.get(click)
                val statusEntries = BuildingWorld.Status.entries
                val currentIndex = statusEntries.indexOf(currentWorld.status)
                val newStatus = if (click.isLeftClick) {
                    statusEntries[(currentIndex + 1) % statusEntries.size]
                } else {
                    statusEntries[(currentIndex - 1 + statusEntries.size) % statusEntries.size]
                }
                val updated = WorldManager.changeStatus(currentWorld, newStatus)
                if (updated != null) {
                    updatableWorldHolder.set(updated, render)
                }
            }
        render.layoutSlot('W').renderWith { warpsItem(updatableWorldHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WarpsView::class.java,
                    mutableMapOf("world" to updatableWorldHolder.get(click))
                )
            }
        render.layoutSlot('M').renderWith { membersItem(updatableWorldHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    MembersView::class.java,
                    mutableMapOf("world" to updatableWorldHolder.get(click))
                )
            }
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WorldView::class.java,
                mutableMapOf("world" to updatableWorldHolder.get(click))
            )
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
                white("Klicke, um den Namen der Welt zu ändern")
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
                white("nächster Status")
            }
            line {
                spacer("»")
                appendSpace()
                primary("Rechtsklick: ")
                white("vorheriger Status")
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
                white("Klicke, um den Anzeigeblock der Welt zu ändern")
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
                white("Klicke, um die Warps dieser Welt zu verwalten")
            }
        }
    }

    private fun membersItem(world: BuildingWorld) = buildItem(Material.PLAYER_HEAD) {
        displayName {
            primary("Mitglieder verwalten")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                variableKey("Mitglieder: ")
                if (world.members.isEmpty()) {
                    error("Keine")
                } else {
                    warning(world.members.size)
                }
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um die Mitglieder dieser Welt zu verwalten")
            }
        }
    }
}
