package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.util.translatable
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
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
) {
    DisplayItemSelectMenuForCreate(player, currentDisplayItem, onSelect).show(player)
}

private class DisplayItemSelectMenuForCreate(
    val player: HumanEntity,
    val currentDisplayItem: Material?,
    val onSelect: (Material) -> Unit
) : ChestGui(
    height,
    ComponentHolder.of(buildText { spacer("Display-Item auswählen") })
) {
    val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
    val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

    init {
        withOutClicks()
        withOutline(width, height)

        val items = validMaterials.map { material ->
            GuiItem(buildItem(material) {
                displayName {
                    if (material == currentDisplayItem) {
                        success("")
                        translatable(material.translationKey())
                        spacer(" ")
                        success("(Ausgewählt)")
                    } else {
                        translatable(material.translationKey())
                    }
                }
            }) { event ->
                event.whoClicked.playClickSound()
                event.whoClicked.closeInventory()
                onSelect(material)
            }
        }

        contentPane.populateWithGuiItems(items)

        addPane(contentPane)
        addPane(navBar)
        update()
        show(player)
    }

    override fun update() {
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

                    it.whoClicked.playSound(true) {
                        type(Sound.ENTITY_CHICKEN_EGG)
                    }
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

                    it.whoClicked.playSound(true) {
                        type(Sound.ENTITY_CHICKEN_EGG)
                    }
                }, 6, 0
            )
        }
    }
}

fun showDisplayItemSelectMenuForEdit(
    player: HumanEntity,
    buildingWorld: BuildingWorld,
    onSelect: (Material) -> Unit
) {
    DisplayItemSelectMenuForEdit(player, buildingWorld, onSelect).show(player)
}

private class DisplayItemSelectMenuForEdit(
    val player: HumanEntity,
    val buildingWorld: BuildingWorld,
    val onSelect: (Material) -> Unit
) : ChestGui(
    height,
    ComponentHolder.of(buildText { spacer("Display-Item auswählen") })
) {
    val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
    val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

    init {
        withOutClicks()
        withOutline(width, height)

        val items = validMaterials.map { material ->
            GuiItem(buildItem(material) {
                displayName {
                    if (material == buildingWorld.displayItem) {
                        success("")
                        translatable(material.translationKey())
                        spacer(" ")
                        success("(Ausgewählt)")
                    } else {
                        translatable(material.translationKey())
                    }
                }
            }) { event ->
                event.whoClicked.playClickSound()
                event.whoClicked.closeInventory()
                onSelect(material)
            }
        }

        contentPane.populateWithGuiItems(items)

        addPane(contentPane)
        addPane(navBar)
        update()
        show(player)
    }

    override fun update() {
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

                    it.whoClicked.playSound(true) {
                        type(Sound.ENTITY_CHICKEN_EGG)
                    }
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

                    it.whoClicked.playSound(true) {
                        type(Sound.ENTITY_CHICKEN_EGG)
                    }
                }, 6, 0
            )
        }
    }
}
