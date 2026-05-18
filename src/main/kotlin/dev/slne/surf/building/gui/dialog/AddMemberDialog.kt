package dev.slne.surf.building.gui.dialog

import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.building.util.primaryColored
import dev.slne.surf.building.world.BuildingWorld
import org.bukkit.Bukkit
import org.bukkit.Sound
import java.util.UUID

@Suppress("UnstableApiUsage")
fun addMemberDialog(
    buildingWorld: BuildingWorld,
    onComplete: (UUID?) -> Unit
) = dialog {
    base {
        title { primaryColored("Mitglied hinzufügen") }
        body {
            plainMessage {
                info("Gib den Namen des Spielers ein, den du als Mitglied hinzufügen möchtest.")
                appendNewline(2)
                appendWarningPrefix()
                error("Der Spieler muss sich mindestens einmal auf dem Server eingeloggt haben!")
            }

            input {
                text("player_name") {
                    label { primaryColored("Spielername:") }
                    width(300)
                    initial("")
                    maxLength(16)
                }
            }
        }

        type {
            confirmation(actionButton {
                label { error("Abbrechen") }
                tooltip { info("Klicke, um abzubrechen.") }
                width(200)

                action {
                    customPlayerClick { _, player ->
                        player.closeDialog()
                        onComplete(null)
                    }
                }
            }, actionButton {
                label { success("Hinzufügen") }
                tooltip { info("Klicke, um den Spieler als Mitglied hinzuzufügen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val playerName = response.getText("player_name")?.trim()

                        if (playerName.isNullOrEmpty()) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Bitte gib einen gültigen Spielernamen ein!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                            player.closeDialog()
                            onComplete(null)
                            return@customPlayerClick
                        }

                        @Suppress("DEPRECATION")
                        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)

                        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Spieler '$playerName' wurde nicht gefunden!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                            player.closeDialog()
                            onComplete(null)
                            return@customPlayerClick
                        }

                        if (offlinePlayer.uniqueId == buildingWorld.authorUuid) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Ersteller der Welt kann nicht als Mitglied hinzugefügt werden!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                            player.closeDialog()
                            onComplete(null)
                            return@customPlayerClick
                        }

                        if (offlinePlayer.uniqueId in buildingWorld.members) {
                            player.sendText {
                                appendErrorPrefix()
                                error("'$playerName' ist bereits Mitglied dieser Welt!")
                            }
                            player.playSound(true) {
                                type(Sound.ENTITY_VILLAGER_NO)
                            }
                            player.closeDialog()
                            onComplete(null)
                            return@customPlayerClick
                        }

                        player.closeDialog()
                        onComplete(offlinePlayer.uniqueId)
                    }
                }
            })
        }
    }
}

