package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.permission.PermissionRegistry
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext

object WorldView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOIOOOO",
            "OSOOWOODO",
            "OOOOBOOOO"
        ).titleBuilder {
            primary("Bauwelt ansehen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val world = worldHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('D', deleteItem).onClick { click ->
            if (!click.player.hasPermission(PermissionRegistry.WORLD_DELETE)) {
                click.playLockedSound()
                click.player.sendText {
                    appendErrorPrefix()
                    error("Du hast keine Berechtigung, um diese Aktion durchzuführen!")
                }
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(
                WorldDeleteView::class.java,
                mutableMapOf("world" to worldHolder.get(click))
            )
        }
        render.layoutSlot('I', createWorldItem(world))
        render.layoutSlot('S').renderWith {
            statusItem(world.status)
        }.updateOnClick()
        render.layoutSlot('W', editItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WorldEditView::class.java,
                mutableMapOf("world" to worldHolder.get(click))
            )
        }
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(CentralMenu::class.java)
        }
    }

    private val deleteItem = MenuHeads.DELETE.clone().apply {
        displayName {
            variableValue("Bauwelt löschen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um diese Bauwelt dauerhaft zu löschen")
            }
            emptyLine()
            line {
                error("✘ Diese Aktion kann nicht rückgängig gemacht werden!")
            }
        }
    }

    private val editItem = MenuHeads.WRITABLE_BOOK.clone().apply {
        displayName {
            variableValue("Bauwelt bearbeiten")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um diese Bauwelt zu bearbeiten")
            }
        }
    }

    private fun statusItem(status: BuildingWorld.Status) = buildItem(status.material) {
        displayName {
            variableValue("Status: ")
            variableValue(status.displayName)
        }

        buildLore {
            emptyLine()
            line {
                if (status.allowBuild) {
                    success("✔ Die Welt kann derzeit bearbeitet werden")
                } else {
                    error("✘ Die Welt kann derzeit nicht bearbeitet werden")
                }
            }
        }
    }
}