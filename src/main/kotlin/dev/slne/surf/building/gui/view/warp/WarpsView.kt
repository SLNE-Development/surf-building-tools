package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.gui.view.world.WorldView
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpsView : View() {
    private val world = initialState<BuildingWorld>("world")

    private val paginationState = buildLazyPaginationState { context ->
        world.get(context).warps.toMutableList()
    }.elementFactory { context, builder, _, warp ->
        builder.withItem(createWarpItem(warp)).onClick { context ->
            val player = context.player

            if (player.canModifyBuildingWorld()) {
                context.openForPlayer(
                    WarpView::class.java,
                    mutableMapOf("world" to world.get(context), "warp" to warp)
                )
            }

            context.playGeneralClickSound()
        }
    }.layoutTarget('W').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.size(5).layout(
            "OOOOOOOOO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OPCOBNOOO"
        ).titleBuilder {
            variableValue("Warps")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(WorldView::class.java, mutableMapOf("world" to world.get(click)))
        }
        render.layoutSlot('C', createWarpButton).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            if (world.get(render).worldUuid != click.player.world.uid) {
                click.player.sendText {
                    appendErrorPrefix()
                    error("Du musst in der Bau-Welt sein, um einen Warp zu erstellen!")
                }
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(
                WarpCreateView::class.java,
                mutableMapOf(
                    "world" to world.get(click),
                    "name" to "warp-${random.nextInt(0, 100000)}",
                    "displayItem" to Material.ENDER_EYE
                )
            )
        }
        render
            .layoutSlot('P')
            .renderWith {
                if (pagination.canBack()) {
                    previousItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canBack()) {
                    return@onClick
                }
                context.playNewPageSound()
                pagination.back()
            }

        render
            .layoutSlot('N')
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canAdvance()) {
                    return@onClick
                }
                context.playNewPageSound()
                pagination.advance()
            }
    }

    private val createWarpButton = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            success("Warp erstellen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um einen neuen Warp zu erstellen")
            }
        }
    }
}
