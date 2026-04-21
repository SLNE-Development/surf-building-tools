package dev.slne.surf.building.gui

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.building.world.BuildingWorld
import org.bukkit.entity.Player
import java.util.*

data class GuiState(
    val currentSort: Sorting?,
    val currentSearch: String?
) {
    val currentSortOrDefault get() = currentSort ?: Sorting.CREATED_AT_DESC

    companion object {
        val sortingMap = mutableObject2ObjectMapOf<UUID, Sorting>()
        val searchMap = mutableObject2ObjectMapOf<UUID, String>()

        fun setSort(playerId: UUID, sort: Sorting) {
            sortingMap[playerId] = sort
        }

        fun setSearch(playerId: UUID, search: String?) {
            searchMap[playerId] = search
        }
    }

    enum class Sorting(name: String, val sortList: List<BuildingWorld>.() -> List<BuildingWorld>) {
        CREATOR("Ersteller", { sortedBy { it.authorName } }),
        MEMBER_COUNT("Mitglieder Anzahl", { sortedByDescending { it.members.size } }),
        CREATED_AT_DESC("Erstellt am: Absteigend", { sortedByDescending { it.createdAt } }),
        CREATED_AT_ASC("Erstellt am: Aufsteigend", { sortedBy { it.createdAt } }),
        NAME("Weltenname", { sortedBy { it.buildingWorldName } });

        val displayName = buildText {
            variableValue(name)
        }

        fun previous(): Sorting = entries[(ordinal - 1 + entries.size) % entries.size]
        fun next(): Sorting = entries[(ordinal + 1) % entries.size]

        fun sort(worlds: List<BuildingWorld>): List<BuildingWorld> = sortList(worlds)

        companion object {
            fun sort(list: Collection<BuildingWorld>, sort: Sorting?): List<BuildingWorld> {
                if (sort == null) return list.toList()
                return sort.sort(list.toList())
            }
        }
    }
}

fun Player.guiState() = GuiState(
    currentSort = GuiState.sortingMap[this.uniqueId],
    currentSearch = GuiState.searchMap[this.uniqueId]
)