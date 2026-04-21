package dev.slne.surf.building.gui.dialog

import dev.slne.surf.api.paper.dialog.search.searchDialog
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.GuiState
import dev.slne.surf.building.gui.view.CentralMenu
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
fun searchWorldDialog(initial: String) = searchDialog(
    title = {
        info("Suche ein Item...")
    },
    searchInput = {
        initialValue = initial
    },
    body = {
        plainMessage {
            info("Gib den Namen eines Items ein, um nach Shops zu suchen. Suche nach Verzauberungsnamen, z.b. \"Mending\" oder \"Soulbound\". Nutze @Spielername um nach einem Verkäufer zu suchen.")
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
    viewFrame.open(CentralMenu::class.java, player)
}