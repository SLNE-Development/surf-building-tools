package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WorldEditItemView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")

    private val validMaterials = Material.entries.filter { !it.isLegacy && it.isItem && !it.isAir }
        .sortedBy { it.name }

    override fun onInit(config: ViewConfigBuilder) {
        config.size(5).titleBuilder {
            primary("Item auswählen")
        }.layout(
            "OOOOOOOOO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "OOOPBNOOO"
        ).cancelInteractions()
    }

    private val paginationState = buildLazyPaginationState { context ->
        validMaterials.toMutableList()
    }.elementFactory { context, builder, _, material ->
        builder.withItem(buildItem(material) {
            displayName {
                translatable(material.translationKey())
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            val currentWorld = worldHolder.get(context)
            val updated = WorldManager.changeDisplayItem(currentWorld, material)
            context.openForPlayer(
                WorldEditView::class.java,
                mutableMapOf("world" to updated)
            )
        }
    }.layoutTarget('R').build()

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.openForPlayer(
                WorldEditView::class.java,
                mutableMapOf("world" to worldHolder.get(click))
            )
            click.playGeneralClickSound()
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
}
