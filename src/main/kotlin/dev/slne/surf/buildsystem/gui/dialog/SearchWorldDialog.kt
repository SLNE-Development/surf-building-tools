package dev.slne.surf.buildsystem.gui.dialog

import dev.slne.surf.api.paper.dialog.search.searchDialog
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.gui.GuiState
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.buildSecondary
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
fun searchWorldDialog(initial: String) = searchDialog(
    title = {
        buildPrimary("Bau-Welt suchen")
    },
    searchInput = {
        initialValue = initial
    },
    body = {
        plainMessage {
            buildSecondary("Gib den Namen einer Bau-Welt ein, um nach ihr zu suchen. Nutze @Spielername, um nach dem Ersteller einer Bau-Welt zu suchen.")
        }
    },
    onSearch = { player, query ->
        search(player, query)
    },
    onClose = { player, query ->
        search(player, query)
    }
)

private fun search(player: Player, query: String) {
    GuiState.setSearch(player.uniqueId, query)
    centralMenu.open(player)
}