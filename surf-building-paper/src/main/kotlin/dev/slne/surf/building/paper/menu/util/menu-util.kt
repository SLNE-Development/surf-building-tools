package dev.slne.surf.building.paper.menu.util

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.showBuildingWorldMenu
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import org.bukkit.Material

private val borderItem = GuiItem(buildItem(Material.GRAY_STAINED_GLASS_PANE) {
    displayName {
        text(" ")
    }
})

fun ChestGui.withOutline(width: Int, height: Int): StaticPane {
    val staticPane = StaticPane(0, 0, width, height).apply {
        for (y in 1 until height - 1) {
            addItem(borderItem, 0, y)
            addItem(borderItem, width - 1, y)
        }

        for (x in 0 until width) {
            addItem(borderItem, x, 0)
            addItem(borderItem, x, height - 1)
        }
    }
    addPane(staticPane)
    return staticPane
}

fun ChestGui.pagination(paginatedPane: PaginatedPane, height: Int) = apply {
    addPane(StaticPane(1, height - 1, 7, 1, Pane.Priority.HIGHEST).apply {
        if (paginatedPane.page > 0) {
            addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Vorherige Seite")
                    }
                }) {
                    paginatedPane.page = (paginatedPane.page - 1).coerceAtLeast(1)
                    update()
                }, 1, 0
            )
        }

        if (paginatedPane.page + 1 < paginatedPane.pages) {
            addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Nächste Seite")
                    }
                }) {
                    paginatedPane.page = (paginatedPane.page + 1).coerceAtMost(paginatedPane.pages)
                    update()
                }, 5, 0
            )
        }
    })
}

fun ChestGui.withHomeButton(height: Int): ChestGui = apply {
    addPane(
        StaticPane(0, 0, 9, height, Pane.Priority.HIGHEST).apply {
            addItem(GuiItem(buildItem(Material.BARRIER) {
                displayName {
                    error("Zurück")
                }
            }) {
                showBuildingWorldMenu(it.whoClicked)
            }, 4, height - 1)
        }
    )
}

fun ChestGui.withOutClicks() = apply {
    setOnGlobalClick { it.isCancelled = true }
    setOnGlobalDrag { it.isCancelled = true }
}