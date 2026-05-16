package dev.slne.surf.building.gui.view.member

import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.addMemberDialog
import dev.slne.surf.building.gui.util.MenuHeads
import dev.slne.surf.building.gui.view.*
import dev.slne.surf.building.gui.view.world.WorldEditView
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext

object MembersView : View() {
    private val world = initialState<BuildingWorld>("world")

    private val paginationState = buildLazyPaginationState { context ->
        world.get(context).members.toMutableList()
    }.elementFactory { _, builder, _, memberUuid ->
        builder.withItem(createMemberItem(memberUuid, true)).onClick { context ->
            val player = context.player
            if (player.canModifyBuildingWorld()) {
                context.openForPlayer(
                    MemberRemoveView::class.java,
                    mutableMapOf("world" to world.get(context), "memberUuid" to memberUuid)
                )
            }
            context.playGeneralClickSound()
        }
    }.layoutTarget('M').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "OMMMMMMMO",
            "OPCOBNOOO"
        ).titleBuilder {
            primary("Mitglieder")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WorldEditView::class.java,
                mutableMapOf("world" to world.get(click))
            )
        }
        render.layoutSlot('C', addMemberButton).onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }
            click.playGeneralClickSound()
            val currentWorld = world.get(click)
            click.closeForPlayer()
            click.player.showDialog(
                addMemberDialog(currentWorld) { uuid ->
                    if (uuid != null) {
                        val updated = WorldManager.addMember(currentWorld, uuid)
                        viewFrame.open(
                            MembersView::class.java,
                            click.player,
                            mutableMapOf("world" to updated)
                        )
                    } else {
                        viewFrame.open(
                            MembersView::class.java,
                            click.player,
                            mutableMapOf("world" to currentWorld)
                        )
                    }
                }
            )
        }
        render.layoutSlot('P')
            .renderWith { if (pagination.canBack()) previousItem else outlineItem }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canBack()) return@onClick
                context.playNewPageSound()
                pagination.back()
            }
        render.layoutSlot('N')
            .renderWith { if (pagination.canAdvance()) nextItem else outlineItem }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canAdvance()) return@onClick
                context.playNewPageSound()
                pagination.advance()
            }
    }

    private val addMemberButton = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName { success("Mitglied hinzufügen") }
        buildLore {
            emptyLine()
            line { spacer("»"); appendSpace(); white("Klicke, um ein neues Mitglied hinzuzufügen") }
        }
    }
}
