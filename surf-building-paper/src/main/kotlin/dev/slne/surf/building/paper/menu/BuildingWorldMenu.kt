package dev.slne.surf.building.paper.menu

import com.github.stefvanschie.inventoryframework.gui.GuiItem
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
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType

private const val width = 9
private const val height = 6

fun showBuildingWorldMenu(player: HumanEntity) = menu(buildText { spacer("Bauwelten") }, height) {
    withOutline(width, height)
    withOutClicks()

    val contentPane = PaginatedPane(1, 1, width - 2, height - 2).apply {
        populateWithGuiItems(
            buildingWorldService.buildingWorlds
                .sortedWith(compareBy<BuildingWorld> { it.status }.thenBy { it.authorName })
                .map { buildBuildingWorldItem(it, player) }
        )
    }

    addPane(StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST).apply {
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
        }

        addItem(GuiItem(MenuHeads.CREATE_BUTTON.apply {
            displayName {
                primary("Neue Bauwelt erstellen")
            }
        }) {
            showBuildingWorldCreateMenu(player)
        }, 4, 0)

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