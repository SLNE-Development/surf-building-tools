package dev.slne.surf.building.gui.dialog

import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.building.util.primaryColored
import dev.slne.surf.building.world.WarpCategory
import org.bukkit.Sound

@Suppress("UnstableApiUsage")
fun showWarpCategoryNameDialog(
    currentCategory: WarpCategory?,
    onComplete: (String?) -> Unit
) = dialog {
    base {
        title { primaryColored(if (currentCategory == null) "Kategorie erstellen" else "Kategorie umbenennen") }
        body {
            plainMessage {
                info("Hier kannst du den Namen der Kategorie festlegen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, dass der Name keine Leerzeichen enthalten darf!")
            }

            input {
                text("category_name") {
                    label { primaryColored("Name der Kategorie:") }
                    width(300)
                    initial(currentCategory?.name ?: "")
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
                        val name = response.getText("category_name")?.trim()?.replace(" ", "-")

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

