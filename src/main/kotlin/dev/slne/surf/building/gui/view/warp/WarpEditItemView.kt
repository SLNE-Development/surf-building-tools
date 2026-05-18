package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

/**
 * Item picker used by both WarpCreateView (warpHolder = null) and WarpEditView (warpHolder set).
 * States passed in: "world", "warp" (nullable), "name" (nullable), "displayItem" (nullable)
 */
object WarpEditItemView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val warpHolder = initialState<Warp?>("warp")
    private val nameHolder = initialState<String?>("name")
    private val displayItemHolder = initialState<Material?>("displayItem")

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
            val world = worldHolder.get(context)
            val warp: Warp? = warpHolder.get(context)
            val name = nameHolder.get(context)
            if (warp != null) {
                context.openForPlayer(
                    WarpEditView::class.java,
                    mutableMapOf(
                        "world" to world,
                        "warp" to warp,
                        "name" to name,
                        "displayItem" to material
                    )
                )
            } else {
                context.openForPlayer(
                    WarpCreateView::class.java,
                    mutableMapOf("world" to world, "name" to name, "displayItem" to material)
                )
            }
        }
    }.layoutTarget('R').build()

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            val world = worldHolder.get(click)
            val warp = warpHolder.get(click)
            val name = nameHolder.get(click)
            val displayItem = displayItemHolder.get(click)
            if (warp != null) {
                click.openForPlayer(
                    WarpEditView::class.java,
                    mutableMapOf(
                        "world" to world,
                        "warp" to warp,
                        "name" to name,
                        "displayItem" to displayItem
                    )
                )
            } else {
                click.openForPlayer(
                    WarpCreateView::class.java,
                    mutableMapOf("world" to world, "name" to name, "displayItem" to displayItem)
                )
            }
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
