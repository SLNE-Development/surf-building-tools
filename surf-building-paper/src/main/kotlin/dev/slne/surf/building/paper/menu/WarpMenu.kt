package dev.slne.surf.building.paper.menu

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.sub.showWarpCreateMenu
import dev.slne.surf.building.paper.menu.sub.showWarpDeleteConfirmMenu
import dev.slne.surf.building.paper.menu.sub.showWarpEditMenu
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.util.displayKey
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.WorldCreator
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.ClickType

private const val width = 9
private const val height = 3

fun showWarpMenu(player: HumanEntity, buildingWorld: BuildingWorld) {
    WarpMenu(player, buildingWorld).show(player)
}

private class WarpMenu(val player: HumanEntity, val buildingWorld: BuildingWorld) : ChestGui(
    height,
    ComponentHolder.of(buildText { spacer("Warps") })
) {
    val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
    val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

    init {
        withOutClicks()
        withOutline(width, height)

        // Home button (back to main menu)
        addPane(StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST).apply {
            addItem(GuiItem(buildItem(Material.BARRIER) {
                displayName {
                    error("Zurück")
                }
            }) {
                it.whoClicked.playClickSound()
                showBuildingWorldMenu(it.whoClicked)
            }, 3, 0)
        })

        if (buildingWorld.authorUuid == player.uniqueId) {
            addPane(StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGH).apply {
                addItem(GuiItem(MenuHeads.CREATE_BUTTON.clone().apply {
                    displayName {
                        primary("Neuen Warp erstellen")
                    }
                }) {
                    showWarpCreateMenu(player, buildingWorld)
                }, 5, 0)
            })
        }

        addPane(contentPane)
        addPane(navBar)
        update()
        show(player)
    }

    override fun update() {
        contentPane.clear()
        contentPane.populateWithGuiItems(
            buildingWorld.warps
                .sortedBy { it.name }
                .map { buildWarpItem(it, player, buildingWorld) }
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
                GuiItem(buildItem(org.bukkit.Material.ARROW) {
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
                GuiItem(buildItem(org.bukkit.Material.ARROW) {
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

private fun buildWarpItem(warp: Warp, player: HumanEntity, buildingWorld: BuildingWorld) = GuiItem(
    buildItem(warp.displayItem) {
        displayName {
            primary(warp.name)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Position: ".toSmallCaps())
            }
            line {
                variableValue("X: ${warp.x.toInt()}, Y: ${warp.y.toInt()}, Z: ${warp.z.toInt()}")
            }
            emptyLine()

            line {
                spacer("Nutze ")
                displayKey("mouse.left")
                spacer(" zum Teleportieren")
            }

            if (buildingWorld.authorUuid == player.uniqueId) {
                line {
                    spacer("Nutze ")
                    displayKey("mouse.right")
                    spacer(" zum Bearbeiten")
                }
                line {
                    spacer("Nutze ")
                    displayKey("sneak")
                    spacer(" + ")
                    displayKey("mouse.left")
                    spacer(" zum Löschen")
                }
            }
        }
    }) {
    if (it.click == ClickType.SHIFT_LEFT) {
        if (buildingWorld.authorUuid == player.uniqueId) {
            showWarpDeleteConfirmMenu(it.whoClicked, buildingWorld, warp)
            it.whoClicked.playClickSound()
        }
    } else if (it.click == ClickType.LEFT) {
        // Load world if not loaded
        val world = buildingWorld.world ?: run {
            val worldCreator = WorldCreator.name(buildingWorld.worldName)
            if (buildingWorld.type == BuildingWorld.Type.VOID) {
                worldCreator.generator(dev.slne.surf.building.paper.world.generator.BuildingWorldGenerator)
            }
            org.bukkit.Bukkit.createWorld(worldCreator)
        }
        
        if (world != null) {
            val location = Location(world, warp.x, warp.y, warp.z, warp.yaw, warp.pitch)
            it.whoClicked.teleportAsync(location)
            it.whoClicked.closeInventory()
            it.whoClicked.sendText {
                appendSuccessPrefix()
                success("Du wurdest zu ")
                variableValue(warp.name)
                success(" teleportiert.")
            }
            it.whoClicked.playClickSound()
        } else {
            it.whoClicked.sendText {
                appendErrorPrefix()
                error("Die Welt konnte nicht geladen werden.")
            }
        }
    } else if (it.click == ClickType.RIGHT) {
        if (buildingWorld.authorUuid == player.uniqueId) {
            showWarpEditMenu(it.whoClicked, buildingWorld, warp)
            it.whoClicked.playClickSound()
        }
    }
}
