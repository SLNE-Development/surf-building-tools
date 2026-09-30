package dev.slne.surf.buildsystem.gui.view.member

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
import java.util.*

val memberRemoveView: AbstractSurfView = surfView("Mitglied entfernen") {
    val worldState = initialState<BuildingWorld>("world")
    val memberUuidState = initialState<UUID>("memberUuid")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        slot(1, 5, createMemberItem(memberUuidState[this]))

        slot(2, 4, cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(membersView::class.java, mapOf("world" to worldState[click]))
        }

        slot(2, 6, confirmItem("Mitglied entfernen")).onClick { click ->
            click.playGeneralClickSound()

            val updated = WorldManager.removeMember(worldState[click], memberUuidState[click])
            click.openForPlayer(membersView::class.java, mapOf("world" to updated))
        }
    }
}
