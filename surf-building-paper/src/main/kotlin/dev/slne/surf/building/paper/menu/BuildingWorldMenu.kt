package dev.slne.surf.building.paper.menu

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 4

fun showBuildingWorldMenu(player: HumanEntity) = menu(buildText { spacer("Bauwelten") }, height) {
    withOutline(width, height)
    withOutClicks()

    val contentPane = PaginatedPane(1, 1, width - 2, height - 2).apply {
        populateWithItemStacks(buildingWorldService.buildingWorlds.map { buildBuildingWorldItem(it) })
    }

    addPane(StaticPane(0, height - 1, 7, 1).apply {
        if (contentPane.page > 1) {
            addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Vorherige Seite".toSmallCaps())
                    }
                }) {
                    contentPane.page -= 1
                    update()
                }, 0, 0
            )

            addItem(GuiItem(MenuHeads.CREATE_BUTTON.apply {
                displayName {
                    primary("Neue Bauwelt erstellen")
                }
            }), 3, 0)
        }

        if (contentPane.page < contentPane.pages - 1) {
            addItem(
                GuiItem(buildItem(Material.ARROW) {
                    displayName {
                        variableValue("Nächste Seite".toSmallCaps())
                    }
                }) {
                    contentPane.page += 1
                    update()
                }, 6, 0
            )
        }
    })

    addPane(contentPane)
    show(player)
}

private fun buildBuildingWorldItem(buildingWorld: BuildingWorld) =
    buildItem(buildingWorld.status.material) {
        displayName {
            primary(buildingWorld.buildingWorldName)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Besitzer: ".toSmallCaps())
                variableValue(buildingWorld.authorName)
            }
            emptyLine()

            line {
                spacer("Status: ".toSmallCaps())
                variableValue(buildingWorld.status.displayName)
            }
            emptyLine()
        }


    }