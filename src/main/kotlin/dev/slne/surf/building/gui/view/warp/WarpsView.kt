package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.gui.view.world.WorldView
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

private sealed class WarpListItem {
    data class WarpEntry(val warp: Warp) : WarpListItem()
    data class CategoryEntry(val category: WarpCategory) : WarpListItem()
}

object WarpsView : View() {
    private val world = initialState<BuildingWorld>("world")

    private val categoryPathHolder = initialState<List<WarpCategory>>("categoryPath")

    private val paginationState = buildLazyPaginationState { context ->
        val bWorld = world.get(context)
        val path = categoryPathHolder.get(context) ?: emptyList()
        val (currentWarps, currentCategories) = if (path.isEmpty()) {
            bWorld.warps to bWorld.categories
        } else {
            val current = path.last()
            current.warps to current.subCategories
        }
        (currentCategories.map { WarpListItem.CategoryEntry(it) as WarpListItem } +
                currentWarps.map { WarpListItem.WarpEntry(it) }).toMutableList()
    }.elementFactory { context, builder, _, item ->
        when (item) {
            is WarpListItem.CategoryEntry -> {
                builder.withItem(createCategoryItem(item.category)).onClick { ctx ->
                    ctx.playGeneralClickSound()
                    val path = categoryPathHolder.get(ctx) ?: emptyList()
                    ctx.openForPlayer(
                        WarpCategoryView::class.java,
                        mutableMapOf(
                            "world" to world.get(ctx),
                            "category" to item.category,
                            "categoryPath" to path
                        )
                    )
                }
            }

            is WarpListItem.WarpEntry -> {
                builder.withItem(createWarpItem(item.warp)).onClick { ctx ->
                    ctx.playGeneralClickSound()
                    val path = categoryPathHolder.get(ctx) ?: emptyList()
                    if (ctx.player.canModifyBuildingWorld()) {
                        ctx.openForPlayer(
                            WarpView::class.java,
                            mutableMapOf(
                                "world" to world.get(ctx),
                                "warp" to item.warp,
                                "categoryPath" to path
                            )
                        )
                    }
                }
            }
        }
    }.layoutTarget('W').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.size(5).layout(
            "OOOOIOOOO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OPCOBNDOO"
        ).titleBuilder {
            primary("Warps")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('I').renderWith {
            val path = categoryPathHolder.get(render) ?: emptyList()
            if (path.isEmpty()) outlineItem else breadcrumbItem(path)
        }.watch(categoryPathHolder)

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            val path = categoryPathHolder.get(click) ?: emptyList()
            if (path.isEmpty()) {
                click.openForPlayer(
                    WorldView::class.java,
                    mutableMapOf("world" to world.get(click))
                )
            } else {
                click.openForPlayer(
                    WarpsView::class.java,
                    mutableMapOf("world" to world.get(click), "categoryPath" to path.dropLast(1))
                )
            }
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
            val path = categoryPathHolder.get(click) ?: emptyList()
            click.openForPlayer(
                WarpCreateView::class.java,
                mutableMapOf(
                    "world" to world.get(click),
                    "name" to "warp-${random.nextInt(0, 100000)}",
                    "displayItem" to Material.ENDER_EYE,
                    "categoryPath" to path
                )
            )
        }

        render.layoutSlot('D', createCategoryButton).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }
            click.playGeneralClickSound()
            val path = categoryPathHolder.get(click) ?: emptyList()
            click.openForPlayer(
                WarpCategoryCreateView::class.java,
                mutableMapOf(
                    "world" to world.get(click),
                    "categoryPath" to path,
                    "editingCategory" to emptyList<WarpCategory>(),
                    "name" to "category-${random.nextInt(0, 100000)}",
                    "displayItem" to Material.CHEST
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

    private fun breadcrumbItem(path: List<WarpCategory>) = buildItem(path.last().displayItem) {
        displayName {
            primary("📁 ")
            variableValue(path.last().name)
        }
        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                primary("Pfad: ")
                white(path.joinToString(" › ") { it.name })
            }
            line {
                spacer("»")
                appendSpace()
                variableKey("Tiefe: ")
                variableValue(path.size)
            }
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

    private val createCategoryButton = buildItem(Material.CHEST) {
        displayName {
            success("Kategorie erstellen")
        }
        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um eine neue Kategorie zu erstellen")
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                info("Kategorien können beliebig tief verschachtelt werden")
            }
        }
    }
}
