package dev.slne.surf.buildsystem.gui.view.world

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.open
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
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.buildsystem.gui.dialog.showBuildingWorldEditNameDialog
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.gui.view.member.membersView
import dev.slne.surf.buildsystem.gui.view.warp.warpsView
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.buildSecondary
import dev.slne.surf.buildsystem.util.translatable
import dev.slne.surf.buildsystem.world.BuildingWorld
import me.devnatan.inventoryframework.context.SlotClickContext
import org.bukkit.Material

val worldEditView: AbstractSurfView = surfView("Bau-Welt bearbeiten") {
    val worldState = initialState<BuildingWorld>("world")
    val updatableWorldState = mutableState<BuildingWorld?>(null)

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        updatableWorldState[this] = worldState[this]
        val render = this

        fun current() = requireNotNull(updatableWorldState[render])

        slot(1, 5).renderWith { createWorldItem(current()) }

        slot(2, 2).renderWith { nameItem(current()) }.onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.closeForPlayer()

            val currentWorld = current()
            click.player.showDialog(
                showBuildingWorldEditNameDialog(currentWorld) { name ->
                    val updated = if (name != null) {
                        WorldManager.renameWorld(currentWorld, name)
                    } else {
                        currentWorld
                    }

                    worldEditView.open(click.player, mapOf("world" to updated))
                }
            )
        }

        slot(2, 3).renderWith { displayItemSlot(current()) }.onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()

            val currentWorld = current()
            click.openForPlayer(
                materialSelectView::class.java,
                mapOf<String, Any>(
                    "onSelect" to { context: SlotClickContext, material: Material ->
                        val updated = WorldManager.changeDisplayItem(currentWorld, material)
                        context.openForPlayer(worldEditView::class.java, mapOf("world" to updated))
                    },
                    "onBack" to { context: SlotClickContext ->
                        context.openForPlayer(worldEditView::class.java, mapOf("world" to currentWorld))
                    }
                )
            )
        }

        slot(2, 5)
            .renderWith { statusItem(current().status) }
            .onClick { click ->
                if (!click.player.canModifyBuildingWorld()) {
                    click.playLockedSound()
                    return@onClick
                }

                click.playGeneralClickSound()

                val currentWorld = current()
                val newStatus = if (click.isRightClick) {
                    currentWorld.status.previous()
                } else {
                    currentWorld.status.next()
                }

                WorldManager.changeStatus(currentWorld, newStatus)?.let {
                    updatableWorldState[render] = it
                    render.update()
                }
            }

        slot(2, 7).renderWith { membersItem(current()) }.onClick { click ->
            if (!click.player.canModifyBuildingWorld()) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()
            click.openForPlayer(membersView::class.java, mapOf("world" to current()))
        }

        slot(2, 8).renderWith { warpsItem(current()) }.onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(warpsView::class.java, mapOf("world" to current()))
        }

        slot(3, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(worldView::class.java, mapOf("world" to current()))
        }
    }
}

private fun nameItem(world: BuildingWorld) = buildItem(Material.NAME_TAG) {
    displayName {
        buildPrimary("Name: ")
        buildSecondary(world.buildingWorldName)
    }

    buildLore {
        emptyLine()
        hint("Klicke, um den Namen der Bau-Welt zu ändern.")
    }
}

private fun displayItemSlot(world: BuildingWorld) = buildItem(world.displayItem) {
    displayName {
        buildPrimary("Anzeigeblock: ")
        translatable(world.displayItem.translationKey())
    }

    buildLore {
        emptyLine()
        hint("Klicke, um den Anzeigeblock der Bau-Welt zu ändern.")
    }
}

private fun statusItem(status: BuildingWorld.Status) =
    ViewIcon(ViewIconType.CIRCLE, statusIconColor(status)).build {
        displayName {
            buildPrimary("Status: ")
            buildSecondary(status.displayName)
        }

        buildLore {
            section("Status")
            selection(BuildingWorld.Status.entries.map { it.displayName }, status.ordinal)

            emptyLine()
            hint(status.description)

            emptyLine()
            action("Linksklick", "nächster Status")
            action("Rechtsklick", "vorheriger Status")
        }
    }

private fun membersItem(world: BuildingWorld) = ViewIcon(ViewIconType.USERS, ViewIconColor.BLUE).build {
    displayName {
        buildPrimary("Mitglieder verwalten")
    }

    buildLore {
        section("Mitglieder")
        entry("Anzahl") {
            if (world.members.isEmpty()) error("Keine") else buildSecondary(world.members.size)
        }

        emptyLine()
        hint("Mitglieder können, neben den Buildern, in der")
        hint("Bau-Welt bauen, solange diese bebaubar ist.")
        emptyLine()
        hint("Klicke, um die Mitglieder zu verwalten.")
    }
}

private fun warpsItem(world: BuildingWorld) = ViewIcon(ViewIconType.MENU, ViewIconColor.BLUE).build {
    displayName {
        buildPrimary("Warps verwalten")
    }

    buildLore {
        section("Warps")
        entry("Anzahl") {
            if (world.warps.isEmpty()) error("Keine") else buildSecondary(world.warps.size)
        }

        emptyLine()
        hint("Warps sind Teleportationspunkte innerhalb der Bau-Welt.")
        emptyLine()
        hint("Klicke, um die Warps zu verwalten.")
    }
}
