package dev.slne.surf.buildsystem.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.world.BuildingWorld

val worldDeleteView: AbstractSurfView = surfView("Bau-Welt löschen") {
    val worldState = initialState<BuildingWorld>("world")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        slot(1, 5, createWorldItem(worldState[this]))

        slot(2, 4, cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(worldView::class.java, mapOf("world" to worldState[click]))
        }

        slot(2, 6, confirmItem("Bau-Welt löschen")).onClick { click ->
            click.playGeneralClickSound()

            val targetWorld = worldState[click]
            click.openForPlayer(centralMenu::class.java)
            click.player.sendText {
                appendInfoPrefix()
                info("Die Bau-Welt wird gelöscht...")
            }

            plugin.launch {
                val success = WorldManager.deleteBuildingWorld(targetWorld.buildingWorldId)

                click.player.sendText {
                    if (success) {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich gelöscht!")
                    } else {
                        appendErrorPrefix()
                        error("Die Bau-Welt konnte nicht gelöscht werden. Bitte versuche es später erneut.")
                    }
                }
            }
        }
    }
}
