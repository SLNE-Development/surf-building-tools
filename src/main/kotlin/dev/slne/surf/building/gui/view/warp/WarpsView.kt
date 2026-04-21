package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.canModifyBuildingWorld
import dev.slne.surf.building.gui.view.createWarpItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext

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
    }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.size(5).layout(
            "OOOOOOOOO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OWWWWWWWO",
            "OOOOBNOOO"
        ).titleBuilder {
            variableValue("Warps")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)
    }
}