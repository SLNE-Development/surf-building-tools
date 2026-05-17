package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.util.BukkitSound
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material
import org.bukkit.entity.Player

object WarpView : View() {
    private val warpHolder = initialState<Warp>("warp")
    private val worldHolder = initialState<BuildingWorld>("world")
    private val categoryPathHolder = initialState<List<WarpCategory>?>("categoryPath")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOIOOOO",
            "OSOOWOODO",
            "OOOOBOOOO"
        ).titleBuilder {
            primary("Warp ansehen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val world = worldHolder.get(render)
        val warp = warpHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('D', deleteItem(render.player)).onClick { click ->
            click.playGeneralClickSound()
            if (click.player.canModifyBuildingWorld()) {
                click.openForPlayer(
                    WarpDeleteView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "warp" to warpHolder.get(click),
                        "categoryPath" to (categoryPathHolder.get(click) ?: emptyList<WarpCategory>())
                    )
                )
            } else {
                click.playLockedSound()
            }
        }
        render.layoutSlot('I', createWarpItem(warp))
        render.layoutSlot('S', editItem(render.player)).onClick { click ->
            click.playGeneralClickSound()
            if (click.player.canModifyBuildingWorld()) {
                click.openForPlayer(
                    WarpEditView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "warp" to warpHolder.get(click),
                        "name" to null,
                        "displayItem" to null,
                        "categoryPath" to (categoryPathHolder.get(click) ?: emptyList<WarpCategory>())
                    )
                )
            } else {
                click.playLockedSound()
            }
        }
        render.layoutSlot('W', teleportItem).onClick { click ->
            click.player.teleportAsync(warp.location(world.world)).thenRun {
                click.player.playSound(true) {
                    type(BukkitSound.ENTITY_ENDERMAN_TELEPORT)
                }
            }
        }
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "categoryPath" to (categoryPathHolder.get(click) ?: emptyList<WarpCategory>())
                )
            )
        }
    }

    private val teleportItem = buildItem(Material.ENDER_EYE) {
        displayName {
            variableValue("Zu Warp teleportieren")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um dich zu diesem Warp zu teleportieren")
            }
        }
    }

    private fun editItem(player: Player) = MenuHeads.WRITABLE_BOOK.clone().apply {
        displayName {
            variableValue("Warp bearbeiten")
        }

        buildLore {
            emptyLine()
            if (player.canModifyBuildingWorld()) {
                line {
                    spacer("»")
                    appendSpace()
                    white("Klicke, um diesen Warp zu bearbeiten")
                }
            } else {
                line {
                    error("✘ Nur Builder können diesen Warp bearbeiten!")
                }
            }
        }
    }

    private fun deleteItem(player: Player) = MenuHeads.DELETE.clone().apply {
        displayName {
            variableValue("Warp löschen")
        }

        buildLore {
            emptyLine()
            if (player.canModifyBuildingWorld()) {
                line {
                    spacer("»")
                    appendSpace()
                    white("Klicke, um diesen Warp zu löschen")
                }
                emptyLine()
                line {
                    error("✘ Diese Aktion kann nicht rückgängig gemacht werden!")
                }
            } else {
                line {
                    error("✘ Nur Builder können diesen Warp löschen!")
                }
            }
        }
    }
}
