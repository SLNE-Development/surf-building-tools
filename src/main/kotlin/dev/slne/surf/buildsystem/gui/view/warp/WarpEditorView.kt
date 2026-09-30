package dev.slne.surf.buildsystem.gui.view.warp

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.open
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
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.buildsystem.gui.dialog.showWarpNameDialog
import dev.slne.surf.buildsystem.gui.view.*
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.buildSecondary
import dev.slne.surf.buildsystem.util.translatable
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp
import me.devnatan.inventoryframework.context.SlotClickContext
import org.bukkit.Material

fun warpEditorData(
    world: BuildingWorld,
    warp: Warp?,
    name: String? = warp?.name,
    displayItem: Material? = warp?.displayItem
) = mapOf(
    "world" to world,
    "warp" to warp,
    "name" to name,
    "displayItem" to displayItem
)

val warpEditorView: AbstractSurfView = surfView("Warp bearbeiten") {
    val worldState = initialState<BuildingWorld>("world")
    val warpState = initialState<Warp?>("warp")
    val nameState = initialState<String?>("name")
    val displayItemState = initialState<Material?>("displayItem")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onFirstRender {
        fun data(
            context: SlotClickContext,
            name: String? = nameState[context],
            displayItem: Material? = displayItemState[context]
        ) = warpEditorData(worldState[context], warpState[context], name, displayItem)

        slot(2, 4, nameItem(nameState[this])).onClick { click ->
            click.playGeneralClickSound()

            val player = click.player
            val world = worldState[click]
            val warp = warpState[click]
            val currentName = nameState[click]
            val displayItem = displayItemState[click]

            click.closeForPlayer()
            player.showDialog(
                showWarpNameDialog(world, warp) { name ->
                    warpEditorView.open(
                        player,
                        warpEditorData(world, warp, name ?: currentName, displayItem)
                    )
                }
            )
        }

        slot(2, 6, displayItemSlot(displayItemState[this])).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                materialSelectView::class.java,
                mapOf<String, Any>(
                    "onSelect" to { context: SlotClickContext, material: Material ->
                        context.openForPlayer(warpEditorView::class.java, data(click, displayItem = material))
                    },
                    "onBack" to { context: SlotClickContext ->
                        context.openForPlayer(warpEditorView::class.java, data(click))
                    }
                )
            )
        }

        slot(3, 1, backItem).onClick { click ->
            click.playGeneralClickSound()

            val warp = warpState[click]
            if (warp != null) {
                click.openForPlayer(warpView::class.java, mapOf("world" to worldState[click], "warp" to warp))
            } else {
                click.openForPlayer(warpsView::class.java, mapOf("world" to worldState[click]))
            }
        }

        slot(3, 9, saveItem(warpState[this], nameState[this], displayItemState[this])).onClick { click ->
            val world = worldState[click]
            val originalWarp = warpState[click]
            val name = nameState[click]
            val displayItem = displayItemState[click]

            if (name == null || displayItem == null) {
                click.playLockedSound()
                return@onClick
            }

            click.playGeneralClickSound()

            if (originalWarp == null) {
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

                val updated = WorldManager.addWarp(world, warp)
                click.openForPlayer(warpsView::class.java, mapOf("world" to updated))
            } else {
                val updatedWarp = originalWarp.copy(name = name, displayItem = displayItem)
                val updated = WorldManager.updateWarp(world, originalWarp, updatedWarp)
                click.openForPlayer(warpView::class.java, mapOf("world" to updated, "warp" to updatedWarp))
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
        hint("Klicke, um den Namen des Warps festzulegen.")
    }
}

private fun displayItemSlot(current: Material?) = buildItem(current ?: Material.ENDER_EYE) {
    displayName {
        buildPrimary("Anzeigeitem: ")
        if (current != null) {
            translatable(current.translationKey())
        } else {
            buildSecondary("Nicht gesetzt")
        }
    }

    buildLore {
        emptyLine()
        hint("Klicke, um das Anzeigeitem des Warps festzulegen.")
    }
}

private fun saveItem(warp: Warp?, name: String?, displayItem: Material?) =
    ViewIcon(if (warp == null) ViewIconType.PLUS else ViewIconType.CHECK, ViewIconColor.GREEN).build {
        displayName {
            success(if (warp == null) "Warp erstellen" else "Änderungen speichern")
        }

        buildLore {
            section("Zusammenfassung")
            entry("Name", name ?: "Nicht gesetzt")
            entry("Anzeigeitem") {
                if (displayItem != null) {
                    translatable(displayItem.translationKey())
                } else {
                    buildSecondary("Nicht gesetzt")
                }
            }

            emptyLine()
            when {
                name == null || displayItem == null ->
                    line { error("✘ Bitte alle Werte festlegen") }

                warp == null -> hint("Klicke, um den Warp an deiner Position zu erstellen.")
                else -> hint("Klicke, um die Änderungen zu speichern.")
            }
        }
    }
