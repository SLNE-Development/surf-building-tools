package dev.slne.surf.building.paper.menu.dialog

import dev.slne.surf.building.paper.menu.sub.showBuildingWorldEditMenu
import dev.slne.surf.building.paper.util.primaryColored
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Sound

@Suppress("UnstableApiUsage")
fun showBuildingWorldEditNameDialog(
    buildingWorld: BuildingWorld
) = dialog {
    base {
        title { primaryColored("Bau-Welt umbenennen") }
        body {
            plainMessage {
                info("Hier kannst du den neuen Namen der Bau-Welt festlegen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das der Name keine Leerzeichen enthalten darf!")
            }

            input {
                text("bworld_name") {
                    label { primaryColored("Name der Bau-Welt:") }
                    width(300)
                    initial(buildingWorld.buildingWorldName)
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
                        showBuildingWorldEditMenu(player, buildingWorld)
                    }
                }
            }, actionButton {
                label { success("Bestätigen") }
                tooltip { info("Klicke, um den Namen zu bestätigen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val name = response.getText("bworld_name")?.trim()?.replace(" ", "-")

                        if (name.isNullOrEmpty() || name == "-") {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der eingegebene Name ist ungültig!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                        }

                        player.closeDialog()
                        showBuildingWorldEditMenu(
                            player,
                            buildingWorld.copy(
                                buildingWorldName = name ?: buildingWorld.buildingWorldName
                            )
                        )
                    }
                }
            })
        }
    }
}