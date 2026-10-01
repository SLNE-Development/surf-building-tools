package dev.slne.surf.buildsystem.gui.view

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.random
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.buildsystem.gui.GuiState
import dev.slne.surf.buildsystem.gui.dialog.searchWorldDialog
import dev.slne.surf.buildsystem.gui.guiState
import dev.slne.surf.buildsystem.gui.view.world.worldCreateView
import dev.slne.surf.buildsystem.gui.view.world.worldView
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.world.BuildingWorld
import org.bukkit.Material
import org.bukkit.entity.Player

val centralMenu: AbstractSurfView = paginatedSurfView("Bau-Welten") {
    settings {
        paginationViewRows(PaginationViewRows.FIVE)
        navigateBackOnOutsideClick(false)
    }

    layoutTarget('I')

    pagination<BuildingWorld> {
        lazySource { context ->
            val state = context.player.guiState()
            getBuildingWorlds(state.currentSortOrDefault, state.currentSearch)
        }

        itemFactory { world ->
            withItem(createWorldItem(world, true)).onClick { context ->
                context.playGeneralClickSound()

                if (context.player.canManageWarps(world) && context.isRightClick) {
                    context.openForPlayer(worldView::class.java, mapOf("world" to world))
                    return@onClick
                }

                val player = context.player
                context.closeForPlayer()
                plugin.launch {
                    if (!WorldManager.joinAndOrLoadBuildingWorld(player, world.buildingWorldId)) {
                        LobbyService.openMenuLater(player)
                    }
                }
            }
        }
    }

    onFirstRender {
        slot(6, 1, sortItem(player)).onClick { context ->
            context.playGeneralClickSound()

            val current = context.player.guiState().currentSortOrDefault
            GuiState.setSort(
                context.player.uniqueId,
                if (context.isRightClick) current.previous() else current.next()
            )
            context.openForPlayer(centralMenu::class.java)
        }

        slot(6, 2, searchItem(player)).onClick { context ->
            context.playGeneralClickSound()

            if (context.isShiftClick) {
                GuiState.setSearch(context.player.uniqueId, null)
                context.openForPlayer(centralMenu::class.java)
                return@onClick
            }

            context.closeForPlayer()
            context.player.showDialog(
                searchWorldDialog(
                    context.player.guiState().currentSearch ?: ""
                )
            )
        }

        if (!LobbyService.isInLobby(player)) {
            slot(6, 8, closeItem).onClick { context ->
                context.playGeneralClickSound()
                context.closeForPlayer()
            }
        } else {
            slot(6, 8, leaveServerItem).onClick { context ->
                context.playGeneralClickSound()
                context.closeForPlayer()
                context.player.kick(buildText {
                    info("Du hast den Bau-Server verlassen.")
                })
            }
        }

        slot(6, 9, createItem).onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canModifyBuildingWorld()) {
                context.playLockedSound()
                return@onClick
            }

            context.openForPlayer(
                worldCreateView::class.java,
                mapOf(
                    "name" to "bauwelt-${context.player.name}-${random.nextInt(0, 100000)}",
                    "type" to BuildingWorld.Type.VOID,
                    "displayItem" to Material.GRASS_BLOCK
                )
            )
        }
    }
}

private fun getBuildingWorlds(
    sortType: GuiState.Sorting,
    search: String?
): List<BuildingWorld> {
    val base = WorldManager.findBuildingWorlds()

    val filtered = if (search.isNullOrBlank()) {
        base
    } else {
        val terms = search
            .trim()
            .lowercase()
            .split(' ')
            .filter { it.isNotEmpty() }

        base.filter { world ->
            val authorName = world.authorName.lowercase()
            val worldName = world.buildingWorldName.lowercase()

            terms.all { term ->
                if (term.startsWith("@")) {
                    val authorSearch = term.removePrefix("@")
                    authorSearch.isEmpty() || authorName.contains(authorSearch)
                } else {
                    authorName.contains(term) || worldName.contains(term)
                }
            }
        }
    }

    return GuiState.Sorting.sort(filtered, sortType)
}

private val createItem
    get() = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
        displayName {
            success("Bau-Welt erstellen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um eine neue Bau-Welt zu erstellen.")
        }
    }

private val leaveServerItem
    get() = ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
        displayName {
            error("Server verlassen")
        }

        buildLore {
            emptyLine()
            hint("Klicke, um den Server zu verlassen.")
        }
    }

private fun searchItem(player: Player) = ViewIcon(ViewIconType.SEARCH, ViewIconColor.YELLOW).build {
    displayName {
        buildPrimary("Suchen")
    }

    val search = player.guiState().currentSearch

    buildLore {
        if (search != null) {
            section("Suche")
            entry("Suchbegriff", search)
        }

        emptyLine()
        hint("Nutze @Name für die Ersteller-Suche.")
        action("Shift", "zum Zurücksetzen")
    }
}

private fun sortItem(player: Player) = ViewIcon(ViewIconType.COG, ViewIconColor.YELLOW).build {
    displayName {
        buildPrimary("Sortieren")
    }

    val sort = player.guiState().currentSortOrDefault

    buildLore {
        section("Sortierung")
        selection(GuiState.Sorting.entries.map { it.label }, sort.ordinal)

        emptyLine()
        action("Linksklick", "nächste Sortierung")
        action("Rechtsklick", "vorherige Sortierung")
    }
}
