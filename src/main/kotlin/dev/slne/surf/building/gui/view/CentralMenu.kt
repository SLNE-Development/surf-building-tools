package dev.slne.surf.building.gui.view

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.GuiState
import dev.slne.surf.building.gui.dialog.searchWorldDialog
import dev.slne.surf.building.gui.guiState
import dev.slne.surf.building.gui.view.world.WorldView
import dev.slne.surf.building.plugin
import dev.slne.surf.building.service.BuildingWorldService
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player

object CentralMenu : View() {
    private val selectedSort = mutableState(GuiState.Sorting.CREATED_AT_DESC)

    override fun onInit(config: ViewConfigBuilder) {
        config.size(6).titleBuilder {
            variableValue("Bau-Welten Übersicht")
        }.layout(
            "OOOOOOOOO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "SFOPCNOOO"
        ).cancelInteractions()
    }

    private val paginationState = buildLazyPaginationState { context ->
        getBuildingWorlds(
            context.player.guiState().currentSortOrDefault,
            context.player.guiState().currentSearch
        ).toMutableList()
    }.elementFactory { context, builder, _, world ->
        builder.withItem(createWorldItem(world)).onClick { context ->
            context.playGeneralClickSound()

            if (context.player.canModifyBuildingWorld()) {
                if (context.isRightClick) {
                    context.openForPlayer(WorldView::class.java, mutableMapOf("world" to world))
                } else {
                    plugin.launch {
                        BuildingWorldService.joinAndOrLoadBuildingWorld(
                            context.player,
                            world.buildingWorldId
                        )
                    }

                }
                return@onClick
            }

            plugin.launch {
                BuildingWorldService.joinAndOrLoadBuildingWorld(
                    context.player,
                    world.buildingWorldId
                )
            }
        }
    }.layoutTarget('R').build()

    override fun onFirstRender(render: RenderContext) {
        val player = render.player
        val pagination = paginationState.get(render)

        selectedSort.set(player.guiState().currentSortOrDefault, render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('S')
            .updateOnClick()
            .renderWith { sortItem(player) }
            .onClick { context ->
                context.playGeneralClickSound()
                if (context.isRightClick) {
                    selectedSort.set(selectedSort.get(render).previous(), render)
                } else {
                    selectedSort.set(selectedSort.get(render).next(), render)
                }

                GuiState.setSort(context.player.uniqueId, selectedSort.get(render))
                render.openForPlayer(CentralMenu::class.java)
            }

        render.layoutSlot('F', searchItem(player)).onClick { context ->
            context.playGeneralClickSound()
            if (context.isShiftClick) {
                GuiState.setSearch(context.player.uniqueId, null)
                render.openForPlayer(CentralMenu::class.java)
                return@onClick
            }

            player.closeInventory()
            player.showDialog(searchWorldDialog(player.guiState().currentSearch ?: ""))
        }

        render
            .layoutSlot('P')
            .renderWith {
                if (pagination.canBack()) {
                    previousItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canBack()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.back()
            }

        render
            .layoutSlot('N')
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canAdvance()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.advance()
            }
    }

    private fun getBuildingWorlds(
        sortType: GuiState.Sorting,
        search: String?
    ): List<BuildingWorld> {
        val base = BuildingWorldService.findBuildingWorlds()

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

    private fun searchItem(player: Player) = buildItem(Material.BRUSH) {
        displayName {
            variableValue("Suchen")
        }

        val search = player.guiState().currentSearch

        buildLore {
            emptyLine()

            if (search != null) {
                line {
                    appendBlob()
                    spacer("Aktueller Suchbegriff: ".toSmallCaps())
                    variableValue(search)
                }

                emptyLine()
            }

            line {
                appendBlob()
                white("SHIFT".toSmallCaps())
                spacer(" zum resetten".toSmallCaps())
            }
        }
    }

    private fun sortItem(player: Player) = buildItem(Material.COMPARATOR) {
        displayName {
            variableValue("Sortieren")
        }

        val sort = player.guiState().currentSort ?: GuiState.Sorting.CREATED_AT_DESC

        buildLore {
            emptyLine()
            line { variableValue("Sortierung".toSmallCaps(), TextDecoration.BOLD) }

            GuiState.Sorting.entries.forEach {
                line {
                    if (it == sort) {
                        appendSpace()
                        spacer("-")
                        appendSpace()
                        append(it.displayName).decorate(TextDecoration.BOLD)
                    } else {
                        spacer("-")
                        appendSpace()
                        append(it.displayName)
                    }
                }
            }
        }
    }
}