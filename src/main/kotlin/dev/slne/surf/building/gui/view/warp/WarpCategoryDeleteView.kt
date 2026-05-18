package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createCategoryItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpCategoryDeleteView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val categoryHolder = initialState<WarpCategory>("category")
    private val categoryPathHolder = initialState<List<WarpCategory>>("categoryPath")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "OOAOIOCOO",
            "OOOOBOOOO"
        ).titleBuilder {
            primary("Kategorie löschen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val category = categoryHolder.get(render)
        val parentPath = categoryPathHolder.get(render) ?: emptyList()

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createCategoryItem(category))

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpCategoryView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "category" to categoryHolder.get(click),
                    "categoryPath" to parentPath
                )
            )
        }

        render.layoutSlot('A', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpCategoryView::class.java,
                mutableMapOf(
                    "world" to worldHolder.get(click),
                    "category" to categoryHolder.get(click),
                    "categoryPath" to parentPath
                )
            )
        }

        render.layoutSlot('C', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val world = worldHolder.get(click)
            val cat = categoryHolder.get(click)
            val path = categoryPathHolder.get(click) ?: emptyList()
            val updatedWorld = WorldManager.deleteCategoryAtPath(world, path, cat)
            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf("world" to updatedWorld, "categoryPath" to path)
            )
        }
    }

    private val confirmItem = buildItem(Material.LIME_STAINED_GLASS_PANE) {
        displayName {
            success("✔ Kategorie löschen")
        }
        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke hier, um die Kategorie zu löschen.")
            }
            emptyLine()
            line { error("✘ Diese Aktion kann nicht rückgängig gemacht werden!") }
            line { error("✘ Alle enthaltenen Warps und Unterkategorien gehen verloren!") }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("✘ Abbrechen")
        }
        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke hier, um abzubrechen.")
            }
        }
    }
}

