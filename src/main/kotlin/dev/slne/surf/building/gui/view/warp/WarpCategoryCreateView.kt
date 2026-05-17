package dev.slne.surf.building.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.dialog.showWarpCategoryNameDialog
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.WarpCategory
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WarpCategoryCreateView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val categoryPathHolder = initialState<List<WarpCategory>>("categoryPath")
    private val editingCategoryHolder = initialState<WarpCategory>("editingCategory")
    private val nameHolder = initialState<String>("name")
    private val displayItemHolder = initialState<Material>("displayItem")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "ON     IO",
            "BOOOCOOOO"
        ).titleBuilder {
            primary("Kategorie erstellen / bearbeiten")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            val path = categoryPathHolder.get(click) ?: emptyList()
            val editing = editingCategoryHolder.get(click)
            if (editing != null) {
                click.openForPlayer(
                    WarpCategoryView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "category" to editing,
                        "categoryPath" to path
                    )
                )
            } else {
                click.openForPlayer(
                    WarpsView::class.java,
                    mutableMapOf("world" to worldHolder.get(click), "categoryPath" to path)
                )
            }
        }

        render.layoutSlot('N').renderWith { nameItem(nameHolder.get(render)) }.onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            val world = worldHolder.get(click)
            val path = categoryPathHolder.get(click)
            val editing = editingCategoryHolder.get(click)
            val displayItem = displayItemHolder.get(click)
            click.player.showDialog(
                showWarpCategoryNameDialog(editing) { name ->
                    viewFrame.open(
                        WarpCategoryCreateView::class.java, click.player,
                        mutableMapOf(
                            "world" to world,
                            "categoryPath" to path,
                            "editingCategory" to editing,
                            "name" to (name ?: nameHolder.get(click)),
                            "displayItem" to displayItem
                        )
                    )
                }
            )
        }

        render.layoutSlot('I').renderWith { displayItemSlot(displayItemHolder.get(render)) }
            .onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    WarpCategoryEditItemView::class.java,
                    mutableMapOf(
                        "world" to worldHolder.get(click),
                        "categoryPath" to categoryPathHolder.get(click),
                        "editingCategory" to editingCategoryHolder.get(click),
                        "name" to nameHolder.get(click),
                        "displayItem" to displayItemHolder.get(click)
                    )
                )
            }

        render.layoutSlot('C').renderWith {
            createItem(
                nameHolder.get(render),
                displayItemHolder.get(render),
                editingCategoryHolder.get(render)
            )
        }.onClick { click ->
            click.playGeneralClickSound()
            val name = nameHolder.get(click) ?: return@onClick
            val displayItem = displayItemHolder.get(click) ?: return@onClick
            val world = worldHolder.get(click)
            val path = categoryPathHolder.get(click) ?: emptyList()
            val editing = editingCategoryHolder.get(click)

            val newCategory = (editing ?: WarpCategory(name = name)).copy(
                name = name,
                displayItem = displayItem
            )

            val updatedWorld = if (editing != null) {
                WorldManager.updateCategoryAtPath(world, path, editing, newCategory)
            } else {
                WorldManager.addCategoryAtPath(world, path, newCategory)
            }

            click.openForPlayer(
                WarpsView::class.java,
                mutableMapOf("world" to updatedWorld, "categoryPath" to path)
            )
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
                white("Klicke, um den Namen der Kategorie festzulegen")
            }
        }
    }

    private fun displayItemSlot(current: Material?) = buildItem(current ?: Material.CHEST) {
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
                white("Klicke, um das Anzeigeitem der Kategorie festzulegen")
            }
        }
    }

    private fun createItem(name: String?, displayItem: Material?, editing: WarpCategory?) =
        buildItem(if (editing != null) Material.LIME_CONCRETE else Material.GREEN_CONCRETE) {
            displayName {
                primary(if (editing != null) "Kategorie speichern" else "Kategorie erstellen")
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
                        error("✘ Bitte alle Werte festlegen")
                    }
                } else {
                    emptyLine()
                    line {
                        success("✔ Klicke, um die Kategorie zu ${if (editing != null) "speichern" else "erstellen"}")
                    }
                }
            }
        }
}

