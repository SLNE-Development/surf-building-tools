package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createWarpItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpDeleteView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val warpHolder = initialState<Warp>("warp")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "OOAOIOCOO",
            "OOOOBOOOO"
        ).titleBuilder {
            primary("Warp löschen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val warp = warpHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createWarpItem(warp))
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpView::class.java,
                mutableMapOf("world" to worldHolder.get(click), "warp" to warpHolder.get(click))
            )
        }
        render.layoutSlot('A', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpView::class.java,
                mutableMapOf("world" to worldHolder.get(click), "warp" to warpHolder.get(click))
            )
        }
        render.layoutSlot('C', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val world = worldHolder.get(click)
            val warpToDelete = warpHolder.get(click)
            val updatedWorld = WorldManager.deleteWarp(world, warpToDelete)
            click.openForPlayer(WarpsView::class.java, mutableMapOf("world" to updatedWorld))
        }
    }

    private val confirmItem = buildItem(Material.LIME_STAINED_GLASS_PANE) {
        displayName {
            success("✔ Warp löschen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke hier, um den Warp zu löschen.")
            }
            emptyLine()
            line {
                error("✘ Diese Aktion kann nicht rückgängig gemacht werden!")
            }
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
