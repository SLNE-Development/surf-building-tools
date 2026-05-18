package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.showWarpNameDialog
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpCreateView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val nameHolder = initialState<String?>("name")
    private val displayItemHolder = initialState<Material?>("displayItem")
    private val categoryPathHolder = initialState<List<WarpCategory>?>("categoryPath")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "ON     IO",
            "BOOOCOOOO"
        ).titleBuilder {
            primary("Warp erstellen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            val path = categoryPathHolder.get(click) ?: emptyList()
            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf("world" to worldHolder.get(click), "categoryPath" to path)
            )
        }
        render.layoutSlot('N').renderWith { nameItem(nameHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            val world = worldHolder.get(click)
            val displayItem = displayItemHolder.get(click)
            val path = categoryPathHolder.get(click)
            click.player.showDialog(
                showWarpNameDialog(world, null) { name ->
                    viewFrame.open(
                        WarpCreateView::class.java, click.player,
                        mutableMapOf(
                            "world" to world,
                            "name" to name,
                            "displayItem" to displayItem,
                            "categoryPath" to path
                        )
                    )
                }
            )
        }
        render.layoutSlot('I').renderWith { displayItemSlot(displayItemHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WarpEditItemView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "warp" to null,
                        "name" to nameHolder.get(click),
                        "displayItem" to displayItemHolder.get(click),
                        "categoryPath" to categoryPathHolder.get(click)
                    )
                )
            }
        render.layoutSlot('C').renderWith {
            createItem(nameHolder.get(render), displayItemHolder.get(render))
        }.onClick { click ->
            click.playGeneralClickSound()
            val name = nameHolder.get(click) ?: return@onClick
            val displayItem = displayItemHolder.get(click) ?: return@onClick
            val world = worldHolder.get(click)

            val location = click.player.location
            val warp = Warp(
                name = name,
                x = location.x,
                y = location.y,
                z = location.z,
                pitch = location.pitch,
                yaw = location.yaw,
                displayItem = displayItem
            )
            val path = categoryPathHolder.get(click) ?: emptyList()
            val updated = WorldManager.addWarpAtPath(world, path, warp)
            click.openForPlayer(WarpsView::class.java, mutableMapOf("world" to updated, "categoryPath" to path))
        }
    }

    private fun nameItem(current: String?) = buildItem(Material.NAME_TAG) {
        displayName {
            primary("Name: ")
            info(current ?: "Nicht gesetzt")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um den Namen des Warps festzulegen")
            }
        }
    }

    private fun displayItemSlot(current: Material?) = buildItem(current ?: Material.COMPASS) {
        displayName {
            primary("Anzeigeitem: ")
            if (current != null) {
                translatable(current.translationKey())
            } else {
                info("Nicht gesetzt")
            }
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um das Anzeigeitem des Warps festzulegen")
            }
        }
    }

    private fun createItem(name: String?, displayItem: Material?) =
        buildItem(Material.GREEN_CONCRETE) {
            displayName {
                primary("Warp erstellen")
            }

            buildLore {
                emptyLine()
                line {
                    spacer("»")
                    appendSpace()
                    variableKey("Name: ")
                    variableValue(name ?: "Nicht gesetzt")
                }
                line {
                    spacer("»")
                    appendSpace()
                    variableKey("Anzeigeitem: ")
                    if (displayItem != null) {
                        translatable(displayItem.translationKey())
                    } else {
                        variableValue("Nicht gesetzt")
                    }
                }

                if (name == null || displayItem == null) {
                    emptyLine()
                    line {
                        error("✘ Bitte alle Werte festlegen, um den Warp erstellen zu können")
                    }
                } else {
                    emptyLine()
                    line {
                        success("✔ Klicke, um den Warp an deiner aktuellen Position zu erstellen")
                    }
                }
            }
        }
}
