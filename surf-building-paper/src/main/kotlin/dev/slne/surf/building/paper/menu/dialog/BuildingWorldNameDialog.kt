package dev.slne.surf.building.paper.menu.dialog

import dev.slne.surf.building.paper.menu.sub.showBuildingWorldCreateMenu
import dev.slne.surf.building.paper.util.primaryColored
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline

@Suppress("UnstableApiUsage")
fun showBuildingWorldNameDialog(
    name: String? = null,
    type: BuildingWorld.Type? = null
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
                        showBuildingWorldCreateMenu(player, name, type)
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
                        showBuildingWorldCreateMenu(
                            player,
                            if (name == "" || name == "-") null else name,
                            type
                        )
                    }
                }
            })
        }
    }
}