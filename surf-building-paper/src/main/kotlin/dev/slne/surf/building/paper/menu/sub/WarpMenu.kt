package dev.slne.surf.building.paper.menu.sub

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.slne.surf.building.paper.menu.util.MenuHeads
import dev.slne.surf.building.paper.menu.util.withHomeButton
import dev.slne.surf.building.paper.menu.util.withOutClicks
import dev.slne.surf.building.paper.menu.util.withOutline
import dev.slne.surf.building.paper.util.displayKey
import dev.slne.surf.building.paper.util.playClickSound
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestGui
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

private const val width = 9
private const val height = 6

fun showWarpMenu(player: HumanEntity, buildingWorld: BuildingWorld): SurfChestGui =
    menu(buildText { spacer("Warps - ${buildingWorld.buildingWorldName}") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        val contentPane = PaginatedPane(1, 1, width - 2, height - 2)
        val navBar = StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGHEST)

        val isOwner = buildingWorld.authorUuid == player.uniqueId

        // Add create warp button if player is owner
        if (isOwner) {
            addPane(StaticPane(0, height - 1, 7, 1, Pane.Priority.HIGH).apply {
                addItem(GuiItem(MenuHeads.CREATE_BUTTON.apply {
                    displayName {
                        primary("Neuen Warp erstellen")
                    }
                }) {
                    it.whoClicked.playClickSound()
                    showWarpCreateMenu(it.whoClicked, buildingWorld)
                }, 4, 0)
            })
        }

        val warpItems = buildingWorld.warps.map { warp ->
            buildWarpItem(warp, buildingWorld, player, isOwner)
        }

        contentPane.populateWithGuiItems(warpItems)

        fun updatePagination() {
            navBar.clear()
            if (contentPane.page > 0) {
                navBar.addItem(
                    GuiItem(buildItem(Material.ARROW) {
                        displayName {
                            variableValue("Vorherige Seite")
                        }
                    }) {
                        contentPane.page = (contentPane.page - 1)
                        updatePagination()
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
                        updatePagination()
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

private fun buildWarpItem(warp: Warp, buildingWorld: BuildingWorld, player: HumanEntity, isOwner: Boolean) =
    GuiItem(
        buildItem(warp.displayItem) {
            displayName {
                primary(warp.name)
            }

            buildLore {
                emptyLine()
                line {
                    spacer("Position:")
                }
                line {
                    variableValue("X: %.1f Y: %.1f Z: %.1f".format(warp.x, warp.y, warp.z))
                }
                emptyLine()

                line {
                    spacer("Nutze ")
                    displayKey("mouse.left")
                    spacer(" zum Teleportieren")
                }

                if (isOwner) {
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
        if (it.click == ClickType.SHIFT_LEFT && isOwner) {
            // Delete warp
            it.whoClicked.playClickSound()
            showWarpDeleteConfirmMenu(it.whoClicked, buildingWorld, warp)
        } else if (it.click == ClickType.LEFT) {
            // Teleport to warp
            if (it.whoClicked is Player) {
                val world = buildingWorld.world
                if (world != null) {
                    val location = Location(world, warp.x, warp.y, warp.z, warp.yaw, warp.pitch)
                    (it.whoClicked as Player).teleportAsync(location)
                    it.whoClicked.sendText {
                        appendSuccessPrefix()
                        success("Du wurdest zum Warp ")
                        variableValue(warp.name)
                        success(" teleportiert.")
                    }
                    it.whoClicked.playClickSound()
                } else {
                    it.whoClicked.sendText {
                        appendErrorPrefix()
                        error("Die Welt konnte nicht gefunden werden.")
                    }
                }
            }
        } else if (it.click == ClickType.RIGHT && isOwner) {
            // Edit warp
            it.whoClicked.playClickSound()
            showWarpEditMenu(it.whoClicked, buildingWorld, warp)
        }
    }
