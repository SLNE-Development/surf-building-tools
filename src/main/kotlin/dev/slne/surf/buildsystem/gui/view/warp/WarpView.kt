package dev.slne.surf.buildsystem.gui.view.warp

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.api.paper.util.BukkitSound
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp

val warpView: AbstractSurfView = surfView("Warp") {
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

        slot(2, 3, teleportItem).onClick { click ->
            val player = click.player
            val world = worldState[click]
            val warp = warpState[click]

            click.closeForPlayer()
            plugin.launch {
                WorldManager.joinAndOrLoadAndTeleport(player, world.buildingWorldId, warp.location())
                player.playSound(true) {
                    type(BukkitSound.ENTITY_ENDERMAN_TELEPORT)
                }
            }
        }

        slot(2, 5, editItem).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(
                warpEditorView::class.java,
                warpEditorData(worldState[click], warpState[click])
            )
        }

        slot(2, 7, deleteItem).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(
                warpDeleteView::class.java,
                mapOf("world" to worldState[click], "warp" to warpState[click])
            )
        }

        slot(3, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(warpsView::class.java, mapOf("world" to worldState[click]))
        }
    }
}

private val teleportItem
    get() = ViewIcon(ViewIconType.ARROW_RIGHT, ViewIconColor.GREEN).build {
        displayName {
            success("Zum Warp teleportieren")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um dich zu diesem Warp zu teleportieren.")
        }
    }

private val editItem
    get() = ViewIcon(ViewIconType.COG, ViewIconColor.YELLOW).build {
        displayName {
            buildPrimary("Warp bearbeiten")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um diesen Warp zu bearbeiten.")
        }
    }

private val deleteItem
    get() = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
        displayName {
            error("Warp löschen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um diesen Warp zu löschen.")
        }
    }
