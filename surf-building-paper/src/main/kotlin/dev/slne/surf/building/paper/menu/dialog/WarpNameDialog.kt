package dev.slne.surf.building.paper.menu.dialog

import dev.slne.surf.building.paper.util.primaryColored
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.Warp
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Sound

@Suppress("UnstableApiUsage")
fun showWarpNameDialog(
    buildingWorld: BuildingWorld,
    currentWarp: Warp?,
    onComplete: (String?) -> Unit
) = dialog {
    base {
        title { primaryColored(if (currentWarp == null) "Warp erstellen" else "Warp umbenennen") }
        body {
            plainMessage {
                info("Hier kannst du den Namen des Warps festlegen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, dass der Name keine Leerzeichen enthalten darf!")
            }

            input {
                text("warp_name") {
                    label { primaryColored("Name des Warps:") }
                    width(300)
                    initial(currentWarp?.name ?: "")
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
                        onComplete(null)
                    }
                }
            }, actionButton {
                label { success("Bestätigen") }
                tooltip { info("Klicke, um den Namen zu bestätigen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val name = response.getText("warp_name")?.trim()?.replace(" ", "-")

                        if (name.isNullOrEmpty() || name == "-") {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der eingegebene Name ist ungültig!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                            player.closeDialog()
                            onComplete(null)
                            return@customPlayerClick
                        }

                        player.closeDialog()
                        onComplete(name)
                    }
                }
            })
        }
    }
}
