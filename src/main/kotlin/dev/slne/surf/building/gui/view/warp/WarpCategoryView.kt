package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material
import org.bukkit.entity.Player

object WarpCategoryView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val categoryHolder = initialState<WarpCategory>("category")

    private val categoryPathHolder = initialState<List<WarpCategory>?>("categoryPath")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOIOOOO",
            "OEOOBSDOO",
            "OOOOOOOOO"
        ).titleBuilder {
            primary("Kategorie ansehen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val category = categoryHolder.get(render)
        val parentPath = categoryPathHolder.get(render) ?: emptyList()

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createCategoryItem(category))

        render.layoutSlot('E', enterItem).onClick { click ->
            click.playGeneralClickSound()
            val cat = categoryHolder.get(click)
            val path = categoryPathHolder.get(click) ?: emptyList()
            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "categoryPath" to (path + cat)
                )
            )
        }

        render.layoutSlot('S', editItem(render.player)).onClick { click ->
            click.playGeneralClickSound()
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }
            val cat = categoryHolder.get(click)
            val path = categoryPathHolder.get(click) ?: emptyList()
            click.openForPlayer(
                WarpCategoryCreateView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "categoryPath" to path,
                    "editingCategory" to cat,
                    "name" to cat.name,
                    "displayItem" to cat.displayItem
                )
            )
        }

        render.layoutSlot('D', deleteItem(render.player)).onClick { click ->
            click.playGeneralClickSound()
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }
            click.openForPlayer(
                WarpCategoryDeleteView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "category" to categoryHolder.get(click),
                    "categoryPath" to (categoryPathHolder.get(click) ?: emptyList())
                )
            )
        }

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "categoryPath" to parentPath
                )
            )
        }
    }

    private val enterItem = buildItem(Material.ENDER_EYE) {
        displayName {
            variableValue("Kategorie öffnen")
        }
        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um in diese Kategorie zu wechseln")
            }
        }
    }

    private fun editItem(player: Player) = MenuHeads.WRITABLE_BOOK.clone().apply {
        displayName {
            variableValue("Kategorie bearbeiten")
        }
        buildLore {
            emptyLine()
            if (player.canModifyBuildingWorld()) {
                line {
                    spacer("»")
                    appendSpace()
                    white("Klicke, um diese Kategorie zu bearbeiten")
                }
            } else {
                line { error("✘ Nur Builder können Kategorien bearbeiten!") }
            }
        }
    }

    private fun deleteItem(player: Player) = MenuHeads.DELETE.clone().apply {
        displayName {
            variableValue("Kategorie löschen")
        }
        buildLore {
            emptyLine()
            if (player.canModifyBuildingWorld()) {
                line {
                    spacer("»")
                    appendSpace()
                    white("Klicke, um diese Kategorie zu löschen")
                }
                emptyLine()
                line { error("✘ Alle Warps und Unterkategorien werden ebenfalls gelöscht!") }
            } else {
                line { error("✘ Nur Builder können Kategorien löschen!") }
            }
        }
    }
}

