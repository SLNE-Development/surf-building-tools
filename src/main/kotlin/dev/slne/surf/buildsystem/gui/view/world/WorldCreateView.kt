package dev.slne.surf.buildsystem.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.buildsystem.gui.dialog.showBuildingWorldCreateNameDialog
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.buildSecondary
import dev.slne.surf.buildsystem.util.translatable
import dev.slne.surf.buildsystem.world.BuildingWorld
import me.devnatan.inventoryframework.context.SlotClickContext
import org.bukkit.Material

val worldCreateView: AbstractSurfView = surfView("Bau-Welt erstellen") {
    val nameState = initialState<String?>("name")
    val typeState = initialState<BuildingWorld.Type?>("type")
    val displayItemState = initialState<Material?>("displayItem")
    val selectedTypeState = mutableState(BuildingWorld.Type.VOID)

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        selectedTypeState[this] = typeState[this] ?: BuildingWorld.Type.VOID

        fun data(context: SlotClickContext, displayItem: Material? = displayItemState[context]) = mapOf(
            "name" to nameState[context],
            "type" to selectedTypeState[context],
            "displayItem" to displayItem
        )

        slot(2, 3, nameItem(nameState[this])).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            click.player.showDialog(
                showBuildingWorldCreateNameDialog(
                    nameState[click],
                    selectedTypeState[click],
                    displayItemState[click]
                )
            )
        }

        slot(2, 5)
            .renderWith { typeItem(selectedTypeState[this]) }
            .onClick { click ->
                click.playGeneralClickSound()

                val current = selectedTypeState[click]
                selectedTypeState[click] = if (click.isRightClick) current.previous() else current.next()
                click.update()
            }

        slot(2, 7, displayItem(displayItemState[this])).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                materialSelectView::class.java,
                mapOf<String, Any>(
                    "onSelect" to { context: SlotClickContext, material: Material ->
                        context.openForPlayer(worldCreateView::class.java, data(click, material))
                    },
                    "onBack" to { context: SlotClickContext ->
                        context.openForPlayer(worldCreateView::class.java, data(click))
                    }
                )
            )
        }

        slot(3, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(centralMenu::class.java)
        }

        slot(3, 9)
            .renderWith { createItem(nameState[this], selectedTypeState[this], displayItemState[this]) }
            .onClick { click ->
                click.playGeneralClickSound()

                val name = nameState[click]
                val type = selectedTypeState[click]
                val displayItem = displayItemState[click]

                if (name == null || displayItem == null) {
                    click.playLockedSound()
                    return@onClick
                }

                val player = click.player
                click.openForPlayer(centralMenu::class.java)
                player.sendText {
                    appendInfoPrefix()
                    info("Die Bau-Welt wird erstellt...")
                }

                plugin.launch {
                    val world = WorldManager.createWorld(
                        name,
                        player.name,
                        player.uniqueId,
                        type,
                        displayItem
                    )

                    player.sendText {
                        if (world == null) {
                            appendErrorPrefix()
                            error("Die Bau-Welt konnte nicht erstellt werden. Bitte versuche es später erneut.")
                        } else {
                            appendSuccessPrefix()
                            success("Die Bau-Welt wurde erfolgreich erstellt!")
                        }
                    }
                }
            }
    }
}

private fun nameItem(current: String?) = buildItem(Material.NAME_TAG) {
    displayName {
        buildPrimary("Name: ")
        buildSecondary(current ?: "Nicht gesetzt")
    }

    buildLore {
        emptyLine()
        hint("Klicke, um den Namen der Bau-Welt festzulegen.")
    }
}

private fun typeItem(current: BuildingWorld.Type) = ViewIcon(ViewIconType.EQUALS, ViewIconColor.BLUE).build {
    displayName {
        buildPrimary("Typ: ")
        buildSecondary(current.displayName)
    }

    buildLore {
        section("Typ")
        selection(BuildingWorld.Type.entries.map { it.displayName }, current.ordinal)

        emptyLine()
        action("Linksklick", "nächster Typ")
        action("Rechtsklick", "vorheriger Typ")
    }
}

private fun displayItem(current: Material?) = buildItem(current ?: Material.GRASS_BLOCK) {
    displayName {
        buildPrimary("Anzeigeblock: ")
        if (current != null) {
            translatable(current.translationKey())
        } else {
            buildSecondary("Nicht gesetzt")
        }
    }

    buildLore {
        emptyLine()
        hint("Klicke, um den Anzeigeblock der Bau-Welt festzulegen.")
    }
}

private fun createItem(name: String?, type: BuildingWorld.Type, displayItem: Material?) =
    ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
        displayName {
            success("Bau-Welt erstellen")
        }

        buildLore {
            section("Zusammenfassung")
            entry("Name", name ?: "Nicht gesetzt")
            entry("Typ", type.displayName)
            entry("Anzeigeblock") {
                if (displayItem != null) {
                    translatable(displayItem.translationKey())
                } else {
                    buildSecondary("Nicht gesetzt")
                }
            }

            emptyLine()
            if (name == null || displayItem == null) {
                line { error("✘ Bitte alle Werte festlegen, um die Bau-Welt erstellen zu können") }
            } else {
                hint("Klicke, um die Bau-Welt zu erstellen.")
            }
        }
    }
