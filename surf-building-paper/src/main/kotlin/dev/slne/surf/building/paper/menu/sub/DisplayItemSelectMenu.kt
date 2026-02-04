package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.util.toDisplayName
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.util.infoColored
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 6

private val validMaterials = Material.entries.filter { it.isBlock && it.isItem && !it.isAir }
    .sortedBy { it.name }

fun showDisplayItemSelectMenuForCreate(
    player: HumanEntity,
    name: String?,
    type: BuildingWorld.Type?,
    currentDisplayItem: Material?,
    onSelect: (Material) -> Unit
): SurfChestGui =
    menu(buildText { spacer("Display-Item auswählen") }, height) {
        withOutline(width, height)
        withOutClicks()

        val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
        val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

        val items = validMaterials.map { material ->
            GuiItem(buildItem(material) {
                displayName {
                    if (material == currentDisplayItem) {
                        success(material.toDisplayName())
                        spacer(" ")
                        success("(Ausgewählt)")
                    } else {
                        infoColored(material.toDisplayName())
                    }
                }
            }) { event ->
                event.whoClicked.playClickSound()
                event.whoClicked.closeInventory()
                onSelect(material)
            }
        }

        contentPane.populateWithGuiItems(items)

        // Add navigation buttons
        val updatePagination = fun() {
            navBar.clear()
            if (contentPane.page > 0) {
                navBar.addItem(
                    GuiItem(buildItem(Material.ARROW) {
                        displayName {
                            variableValue("Vorherige Seite")
                        }
                    }) {
                        contentPane.page = (contentPane.page - 1)
                        update()

                        it.whoClicked.playSound(true) {
                            type(Sound.ENTITY_CHICKEN_EGG)
                        }
                    }, 2, 0
                )
            }

            if (contentPane.page + 1 < contentPane.pages) {
                navBar.addItem(
                    GuiItem(buildItem(Material.ARROW) {
                        displayName {
                            variableValue("Nächste Seite")
                        }
                    }) {
                        contentPane.page = (contentPane.page + 1)
                        update()

                        it.whoClicked.playSound(true) {
                            type(Sound.ENTITY_CHICKEN_EGG)
                        }
                    }, 6, 0
                )
            }
        }

        updatePagination()

        addPane(contentPane)
        addPane(navBar)
        show(player)
    }

fun showDisplayItemSelectMenuForEdit(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    onSelect: (Material) -> Unit
): SurfChestGui =
    menu(buildText { spacer("Display-Item auswählen") }, height) {
        withOutline(width, height)
        withOutClicks()

        val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
        val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

        val items = validMaterials.map { material ->
            GuiItem(buildItem(material) {
                displayName {
                    if (material == buildingWorld.displayItem) {
                        success(material.toDisplayName())
                        spacer(" ")
                        success("(Ausgewählt)")
                    } else {
                        infoColored(material.toDisplayName())
                    }
                }
            }) { event ->
                event.whoClicked.playClickSound()
                event.whoClicked.closeInventory()
                onSelect(material)
            }
        }

        contentPane.populateWithGuiItems(items)

        // Add navigation buttons
        val updatePagination = fun() {
            navBar.clear()
            if (contentPane.page > 0) {
                navBar.addItem(
                    GuiItem(buildItem(Material.ARROW) {
                        displayName {
                            variableValue("Vorherige Seite")
                        }
                    }) {
                        contentPane.page = (contentPane.page - 1)
                        update()

                        it.whoClicked.playSound(true) {
                            type(Sound.ENTITY_CHICKEN_EGG)
                        }
                    }, 2, 0
                )
            }

            if (contentPane.page + 1 < contentPane.pages) {
                navBar.addItem(
                    GuiItem(buildItem(Material.ARROW) {
                        displayName {
                            variableValue("Nächste Seite")
                        }
                    }) {
                        contentPane.page = (contentPane.page + 1)
                        update()

                        it.whoClicked.playSound(true) {
                            type(Sound.ENTITY_CHICKEN_EGG)
                        }
                    }, 6, 0
                )
            }
        }

        updatePagination()

        addPane(contentPane)
        addPane(navBar)
        show(player)
    }
