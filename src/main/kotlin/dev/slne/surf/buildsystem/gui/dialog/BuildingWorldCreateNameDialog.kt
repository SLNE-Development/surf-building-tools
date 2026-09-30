package dev.slne.surf.buildsystem.gui.dialog

import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.gui.view.world.worldCreateView
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.world.BuildingWorld
import org.bukkit.Material

@Suppress("UnstableApiUsage")
fun showBuildingWorldCreateNameDialog(
    name: String? = null,
    type: BuildingWorld.Type? = null,
    displayItem: Material? = null
) = dialog {
    base {
        title { buildPrimary("Bau-Welt benennen") }
        preventClosingWithEscape()
        body {
            plainMessage {
                info("Hier kannst du den Namen der neuen Bau-Welt festlegen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das der Name keine Leerzeichen enthalten darf!")
            }

            input {
                text("bworld_name") {
                    label { buildPrimary("Name der Bau-Welt:") }
                    width(300)
                    name?.let {
                        initial(it)
                    }
                    maxLength(64)
                }
            }
        }

        type {
            confirmation(actionButton {
                label { error("Abbrechen") }
                tooltip { info("Klicke, um zurück zu gelangen.") }
                width(200)

                action {
                    customPlayerClick { _, player ->
                        player.closeDialog()
                        worldCreateView.open(
                            player, mutableMapOf(
                                "name" to name,
                                "type" to type,
                                "displayItem" to displayItem
                            )
                        )
                    }
                }
            }, actionButton {
                label { success("Bestätigen") }
                tooltip { info("Klicke, um den Namen zu bestätigen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val name = response.getText("bworld_name")?.trim()?.replace(" ", "-")

                        player.closeDialog()
                        worldCreateView.open(
                            player, mutableMapOf(
                                "name" to name,
                                "type" to type,
                                "displayItem" to displayItem
                            )
                        )
                    }
                }
            })
        }
    }
}