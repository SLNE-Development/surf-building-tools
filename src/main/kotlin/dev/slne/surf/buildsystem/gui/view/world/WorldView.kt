package dev.slne.surf.buildsystem.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
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
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.*
import dev.slne.surf.buildsystem.world.BuildingWorld

val worldView: AbstractSurfView = surfView("Bau-Welt") {
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
        val world = worldState[this]

        slot(1, 5, createWorldItem(world))
        slot(2, 2, statusItem(world.status))

        slot(2, 4, joinItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()

            plugin.launch {
                WorldManager.joinAndOrLoadBuildingWorld(click.player, world.buildingWorldId)
            }
        }

        slot(2, 6, editItem).onClick { click ->
            if (!click.player.canManageWarps(worldState[click])) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(worldEditView::class.java, mapOf("world" to worldState[click]))
        }

        slot(2, 8, deleteItem).onClick { click ->
            if (!click.player.hasPermission(PermissionRegistry.WORLD_DELETE)) {
                click.playLockedSound()
                click.player.sendText {
                    appendErrorPrefix()
                    error("Du hast keine Berechtigung, um diese Aktion durchzuführen!")
                }
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(worldDeleteView::class.java, mapOf("world" to worldState[click]))
        }

        slot(3, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(centralMenu::class.java)
        }
    }
}

private val joinItem
    get() = ViewIcon(ViewIconType.HOME, ViewIconColor.GREEN).build {
        displayName {
            success("Bau-Welt betreten")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um diese Bau-Welt zu betreten.")
        }
    }

private val editItem
    get() = ViewIcon(ViewIconType.COG, ViewIconColor.YELLOW).build {
        displayName {
            buildPrimary("Bau-Welt bearbeiten")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um diese Bau-Welt zu bearbeiten.")
        }
    }

private val deleteItem
    get() = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
        displayName {
            error("Bau-Welt löschen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um diese Bau-Welt dauerhaft zu löschen.")
        }
    }

private fun statusItem(status: BuildingWorld.Status) =
    ViewIcon(ViewIconType.CIRCLE, statusIconColor(status)).build {
        displayName {
            buildPrimary("Status: ")
            buildSecondary(status.displayName)
        }

        buildLore {
            emptyLine()
            line {
                if (status.allowBuild) {
                    success("✔ Die Welt kann derzeit bearbeitet werden")
                } else {
                    error("✘ Die Welt kann derzeit nicht bearbeitet werden")
                }
            }
        }
    }
