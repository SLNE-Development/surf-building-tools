package dev.slne.surf.building.gui.dialog

import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.building.gui.view.world.WorldCreateView
import dev.slne.surf.building.util.primaryColored
import dev.slne.surf.building.world.BuildingWorld
import org.bukkit.Material

@Suppress("UnstableApiUsage")
fun showBuildingWorldCreateNameDialog(
    name: String? = null,
    type: BuildingWorld.Type? = null,
    displayItem: Material? = null
) = dialog {
    base {
        title { primaryColored("Bau-Welt benennen") }
        body {
            plainMessage {
                info("Hier kannst du den Namen der neuen Bau-Welt festlegen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das der Name keine Leerzeichen enthalten darf!")
            }

            input {
                text("bworld_name") {
                    label { primaryColored("Name der Bau-Welt:") }
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
                        viewFrame.open(
                            WorldCreateView::class.java, player, mutableMapOf(
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
                        viewFrame.open(
                            WorldCreateView::class.java, player, mutableMapOf(
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