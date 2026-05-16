package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WorldCreateItemView : View() {
    private val nameHolder = initialState<String>("name")
    private val typeHolder = initialState<BuildingWorld.Type>("type")
    private val displayItemHolder = initialState<Material>("displayItem")

    private val validMaterials = Material.entries.filter { !it.isLegacy && it.isItem && !it.isAir }
        .sortedBy { it.name }

    override fun onInit(config: ViewConfigBuilder) {
        config.size(5).titleBuilder {
            variableValue("Item auswählen")
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
            context.openForPlayer(
                WorldCreateView::class.java,
                mutableMapOf(
                    "name" to nameHolder.get(context),
                    "type" to typeHolder.get(context),
                    "displayItem" to material
                )
            )
        }
    }.layoutTarget('R').build()

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.openForPlayer(
                WorldCreateView::class.java, mutableMapOf(
                    "name" to nameHolder.get(click),
                    "type" to typeHolder.get(click),
                    "displayItem" to displayItemHolder.get(click)
                )
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