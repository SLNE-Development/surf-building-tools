package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpCategoryEditItemView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val categoryPathHolder = initialState<List<WarpCategory>>("categoryPath")
    private val editingCategoryHolder = initialState<WarpCategory>("editingCategory")
    private val nameHolder = initialState<String>("name")
    private val displayItemHolder = initialState<Material>("displayItem")

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

    private val paginationState = buildLazyPaginationState { _ ->
        validMaterials.toMutableList()
    }.elementFactory { context, builder, _, material ->
        builder.withItem(buildItem(material) {
            displayName {
                translatable(material.translationKey())
            }
        }).onClick { ctx ->
            ctx.playGeneralClickSound()
            ctx.openForPlayer(
                WarpCategoryCreateView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(ctx),
                    "categoryPath" to categoryPathHolder.get(ctx),
                    "editingCategory" to editingCategoryHolder.get(ctx),
                    "name" to nameHolder.get(ctx),
                    "displayItem" to material
                )
            )
        }
    }.layoutTarget('R').build()

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpCategoryCreateView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "categoryPath" to categoryPathHolder.get(click),
                    "editingCategory" to editingCategoryHolder.get(click),
                    "name" to nameHolder.get(click),
                    "displayItem" to displayItemHolder.get(click)
                )
            )
        }
        render.layoutSlot('P').renderWith {
            if (pagination.canBack()) previousItem else outlineItem
        }.watch(paginationState).onClick { ctx ->
            if (!pagination.canBack()) return@onClick
            ctx.playNewPageSound()
            pagination.back()
        }
        render.layoutSlot('N').renderWith {
            if (pagination.canAdvance()) nextItem else outlineItem
        }.watch(paginationState).onClick { ctx ->
            if (!pagination.canAdvance()) return@onClick
            ctx.playNewPageSound()
            pagination.advance()
        }
    }
}

