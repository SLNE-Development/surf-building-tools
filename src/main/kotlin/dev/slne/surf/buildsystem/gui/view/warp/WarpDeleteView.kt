package dev.slne.surf.buildsystem.gui.view.warp

import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp

val warpDeleteView: AbstractSurfView = surfView("Warp löschen") {
    val worldState = initialState<BuildingWorld>("world")
    val warpState = initialState<Warp>("warp")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        slot(1, 5, createWarpItem(warpState[this]))

        slot(2, 4, cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                warpView::class.java,
                mapOf("world" to worldState[click], "warp" to warpState[click])
            )
        }

        slot(2, 6, confirmItem("Warp löschen")).onClick { click ->
            click.playGeneralClickSound()

            val updated = WorldManager.deleteWarp(worldState[click], warpState[click])
            click.openForPlayer(warpsView::class.java, mapOf("world" to updated))
        }
    }
}
