package dev.slne.surf.building.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createWorldItem
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WorldDeleteView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3)
            .titleBuilder {
                primary("Welt löschen")
            }
            .layout(
                "OOOOOOOOO",
                "OOAOIOCOO",
                "OOOOBOOOO"
            ).cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem)
        render.layoutSlot('C', confirmItem)
        render.layoutSlot('A', cancelItem)
        render.layoutSlot('I', createWorldItem(worldHolder.get(render)))
    }

    private val confirmItem = buildItem(Material.LIME_STAINED_GLASS_PANE) {
        displayName {
            success("Welt löschen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                spacer("Klicke hier, um die Welt zu löschen.")
            }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("Abbrechen")
        }
    }
}