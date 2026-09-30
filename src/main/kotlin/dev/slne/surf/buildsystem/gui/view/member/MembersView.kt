package dev.slne.surf.buildsystem.gui.view.member

import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.open
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
import dev.slne.surf.buildsystem.gui.dialog.addMemberDialog
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.gui.view.world.worldEditView
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.world.BuildingWorld
import java.util.*

val membersView: AbstractSurfView = paginatedSurfView("Mitglieder") {
    val worldState = initialState<BuildingWorld>("world")

    settings {
        paginationViewRows(PaginationViewRows.THREE)
        navigateBackOnOutsideClick(false)
    }

    layoutTarget('I')

    pagination<UUID> {
        lazySource { context -> worldState[context].members }

        itemFactory { memberUuid ->
            withItem(createMemberItem(memberUuid, true)).onClick { click ->
                if (!click.player.canModifyBuildingWorld()) {
                    click.playLockedSound()
                    return@onClick
                }

                click.playGeneralClickSound()
                click.openForPlayer(
                    memberRemoveView::class.java,
                    mapOf("world" to worldState[click], "memberUuid" to memberUuid)
                )
            }
        }
    }

    onFirstRender {
        slot(4, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(worldEditView::class.java, mapOf("world" to worldState[click]))
        }

        slot(4, 9, addMemberItem).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()

            val currentWorld = worldState[click]
            val player = click.player
            click.closeForPlayer()
            player.showDialog(
                addMemberDialog(currentWorld) { uuid ->
                    val updated = uuid?.let { WorldManager.addMember(currentWorld, it) } ?: currentWorld
                    membersView.open(player, mapOf("world" to updated))
                }
            )
        }
    }
}

private val addMemberItem
    get() = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
        displayName {
            success("Mitglied hinzufügen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um ein neues Mitglied hinzuzufügen.")
        }
    }
