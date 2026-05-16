package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.showWarpNameDialog
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createWarpItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpEditView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val warpHolder = initialState<Warp>("warp")
    private val nameHolder = initialState<String?>("name")
    private val displayItemHolder = initialState<Material?>("displayItem")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOIOOOO",
            "ON     DO",
            "BOOOSOOOO"
        ).titleBuilder {
            variableValue("Warp bearbeiten")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val warp = warpHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createWarpItem(warp))
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                WarpView::class.java,
                mutableMapOf("world" to worldHolder.get(click), "warp" to warpHolder.get(click))
            )
        }
        render.layoutSlot('N').renderWith { nameItem(nameHolder.get(render) ?: warpHolder.get(render).name) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                val world = worldHolder.get(click)
                val originalWarp = warpHolder.get(click)
                val displayItem = displayItemHolder.get(click)
                click.player.showDialog(
                    showWarpNameDialog(world, originalWarp) { name ->
                        viewFrame.open(
                            WarpEditView::class.java, click.player,
                            mutableMapOf(
                                "world" to world,
                                "warp" to originalWarp,
                                "name" to (name ?: originalWarp.name),
                                "displayItem" to displayItem
                            )
                        )
                    }
                )
            }
        render.layoutSlot('D').renderWith { displayItemSlot(displayItemHolder.get(render) ?: warpHolder.get(render).displayItem) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WarpEditItemView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "warp" to warpHolder.get(click),
                        "name" to nameHolder.get(click),
                        "displayItem" to displayItemHolder.get(click)
                    )
                )
            }
        render.layoutSlot('S', saveItem).onClick { click ->
            click.playGeneralClickSound()
            val world = worldHolder.get(click)
            val originalWarp = warpHolder.get(click)
            val newName = nameHolder.get(click) ?: originalWarp.name
            val newDisplayItem = displayItemHolder.get(click) ?: originalWarp.displayItem

            val updatedWarp = originalWarp.copy(name = newName, displayItem = newDisplayItem)
            val updatedWorld = WorldManager.updateWarp(world, originalWarp, updatedWarp)
            click.openForPlayer(
                WarpView::class.java,
                mutableMapOf("world" to updatedWorld, "warp" to updatedWarp)
            )
        }
    }

    private fun nameItem(current: String) = buildItem(Material.NAME_TAG) {
        displayName {
            primary("Name: ")
            variableValue(current)
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um den Namen des Warps zu ändern")
            }
        }
    }

    private fun displayItemSlot(current: Material) = buildItem(current) {
        displayName {
            primary("Anzeigeitem: ")
            translatable(current.translationKey())
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um das Anzeigeitem des Warps zu ändern")
            }
        }
    }

    private val saveItem = buildItem(Material.GREEN_CONCRETE) {
        displayName {
            success("✔ Änderungen speichern")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um die Änderungen zu speichern")
            }
        }
    }
}
