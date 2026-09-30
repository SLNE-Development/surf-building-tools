package dev.slne.surf.buildsystem.gui.view.warp

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.gui.view.world.worldEditView
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp
import org.bukkit.Material

val warpsView: AbstractSurfView = paginatedSurfView("Warps") {
    val worldState = initialState<BuildingWorld>("world")

    settings {
        paginationViewRows(PaginationViewRows.FOUR)
        navigateBackOnOutsideClick(false)
    }

    layoutTarget('I')

    pagination<Warp> {
        lazySource { context -> worldState[context].warps }

        itemFactory { warp ->
            withItem(createWarpItem(warp, true)).onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(warpView::class.java, mapOf("world" to worldState[click], "warp" to warp))
            }
        }
    }

    onFirstRender {
        slot(5, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(worldEditView::class.java, mapOf("world" to worldState[click]))
        }

        slot(5, 9, createWarpButton).onClick { click ->
            if (!click.player.canManageWarps(worldState[click])) {
                click.playLockedSound()
                return@onClick
            }

            val world = worldState[click]
            if (world.worldUuid != click.player.world.uid) {
                click.player.sendText {
                    appendErrorPrefix()
                    error("Du musst in der Bau-Welt sein, um einen Warp zu erstellen!")
                }
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(
                warpEditorView::class.java,
                warpEditorData(
                    world = world,
                    warp = null,
                    name = "warp-${random.nextInt(0, 100000)}",
                    displayItem = Material.ENDER_EYE
                )
            )
        }
    }
}

private val createWarpButton
    get() = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
        displayName {
            success("Warp erstellen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um an deiner Position einen Warp zu erstellen.")
        }
    }
