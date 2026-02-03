package dev.slne.surf.building.paper.menu

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.sub.showBuildingWorldCreateMenu
import dev.slne.surf.building.paper.menu.sub.showBuildingWorldEditMenu
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.displayKey
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType

private const val width = 9
private const val height = 6

fun showBuildingWorldMenu(player: HumanEntity) {
    BuildingWorldMenu(player).show(player)
}

private class BuildingWorldMenu(val player: HumanEntity) : ChestGui(
    height,
    ComponentHolder.of(buildText { spacer("Bauwelten") })
) {
    val contentPane = PaginatedPane(1, 1, width - 2, height - 2).apply {
        populateWithGuiItems(
            buildingWorldService.buildingWorlds
                .sortedWith(compareBy<BuildingWorld> { it.status }.thenBy { it.authorName }
                    .thenBy { it.buildingWorldName })
                .map { buildBuildingWorldItem(it, player) }
        )
    }

    val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

    init {
        withOutClicks()
        withOutline(width, height)

        addPane(StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGH).apply {
            addItem(GuiItem(MenuHeads.CREATE_BUTTON.apply {
                displayName {
                    primary("Neue Bauwelt erstellen")
                }
            }) {
                showBuildingWorldCreateMenu(player)
            }, 4, 0)
        })

        addPane(contentPane)
        addPane(navBar)
        show(player)

        update()
    }

    override fun update() {
        contentPane.clear()
        contentPane.populateWithGuiItems(
            buildingWorldService.buildingWorlds
                .sortedWith(compareBy<BuildingWorld> { it.status }.thenBy { it.authorName }
                    .thenBy { it.buildingWorldName })
                .map { buildBuildingWorldItem(it, player) }
        )

        updatePagination(navBar, contentPane)
        super.update()
    }

    private fun updatePagination(
        outlinePane: StaticPane,
        pages: PaginatedPane
    ) {
        outlinePane.clear()
        if (pages.page > 0) {
            outlinePane.addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Vorherige Seite")
                    }
                }) {
                    pages.page = (pages.page - 1)
                    update()
                }, 2, 0
            )
        }

        if (pages.page + 1 < pages.pages) {
            outlinePane.addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Nächste Seite")
                    }
                }) {
                    pages.page = (pages.page + 1)
                    update()
                }, 6, 0
            )
        }
    }
}

private fun buildBuildingWorldItem(buildingWorld: BuildingWorld, player: HumanEntity) = GuiItem(
    buildItem(buildingWorld.status.material) {
        displayName {
            primary(buildingWorld.buildingWorldName)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Besitzer: ".toSmallCaps())
            }
            line {
                variableValue(buildingWorld.authorName)
            }
            emptyLine()

            line {
                spacer("Status: ".toSmallCaps())

            }
            line {
                variableValue(buildingWorld.status.displayName)
            }
            emptyLine()

            line {
                spacer("Nutze ")
                displayKey("mouse.left")
                spacer(" zum Beitreten")
            }

            if (buildingWorld.authorUuid == player.uniqueId) {
                line {
                    spacer("Nutze ")
                    displayKey("mouse.right")
                    spacer(" zum bearbeiten")
                }
            }
        }
    }) {
    if (it.click == ClickType.LEFT) {
        buildingWorldService.joinAndOrLoadBuildingWorld(
            it.whoClicked,
            buildingWorld.buildingWorldId
        )
    } else {
        if (buildingWorld.authorUuid == player.uniqueId) {
            showBuildingWorldEditMenu(it.whoClicked, buildingWorld)
        }
    }
}