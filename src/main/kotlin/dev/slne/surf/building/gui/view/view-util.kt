package dev.slne.surf.building.gui.view

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.util.BukkitSound
import dev.slne.surf.api.paper.util.toOfflinePlayers
import dev.slne.surf.building.permission.PermissionRegistry
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.context.SlotClickContext
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

val View.backItem
    get() = buildItem(Material.BARRIER) {
        displayName {
            error("Zurück")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um zurückzugehen")
            }
        }
    }

val View.previousItem
    get() = buildItem(Material.ARROW) {
        displayName {
            white("← Vorherige Seite")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um zur vorherigen Seite zu wechseln")
            }
        }
    }

val View.nextItem
    get() = buildItem(Material.ARROW) {
        displayName {
            white("Nächste Seite →")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke, um zur nächsten Seite zu wechseln")
            }
        }
    }

fun createWorldItem(buildingWorld: BuildingWorld, joinOrEdit: Boolean = false) =
    buildItem(buildingWorld.displayItem) {
        displayName {
            variableValue(buildingWorld.buildingWorldName)
        }

        buildLore {
            emptyLine()
            line {
                if (buildingWorld.status.allowBuild) {
                    success("✔ Die Welt wird derzeit bearbeitet")
                } else {
                    error("✘ Die Welt wird derzeit nicht bearbeitet")
                }
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                variableKey("Ersteller: ")
                variableValue(buildingWorld.authorName)
            }

            line {
                spacer("»")
                appendSpace()
                variableKey("Mitglieder: ")

                if (buildingWorld.members.isEmpty()) {
                    error("Keine")
                } else {
                    spacer("(")
                    warning(buildingWorld.members.size)
                    spacer(") ")
                    variableValue(
                        buildingWorld.members.toOfflinePlayers()
                            .joinToString(", ") { it.name ?: "#null" })
                }
            }

            line {
                spacer("»")
                appendSpace()
                variableKey("Warps: ")

                if (buildingWorld.warps.isEmpty()) {
                    error("Keine")
                } else {
                    spacer("(")
                    warning(buildingWorld.warps.size)
                    spacer(") ")
                    variableValue(buildingWorld.warps.joinToString { it.name })
                }
            }

            line {
                spacer("»")
                appendSpace()
                variableKey("Typ: ")
                variableValue(
                    buildingWorld.type.name.lowercase().replaceFirstChar { it.uppercase() })
            }

            line {
                spacer("»")
                appendSpace()
                variableKey("Status: ")
                variableValue(buildingWorld.status.displayName)
            }
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                variableKey("Erstellt am: ")
                variableValue(buildingWorld.createdAt.format(dateTimeFormatter))
            }
            line {
                white("#${buildingWorld.buildingWorldId}")
            }

            if (joinOrEdit) {
                emptyLine()
                line {
                    spacer("»")
                    appendSpace()
                    variableValue("Linksklick: ")
                    white("Bearbeiten")
                    darkSpacer(" (nur für Ersteller und Builder)")
                }

                line {
                    spacer("»")
                    appendSpace()
                    variableValue("Rechtsklick: ")
                    white("Betreten")
                }
            }
        }
    }

fun View.createWarpItem(warp: Warp) = buildItem(warp.displayItem) {
    displayName {
        variableValue(warp.name)
    }

    buildLore {
        emptyLine()
        line {
            spacer("»")
            appendSpace()
            variableKey("X: ")
            variableValue(warp.x)
        }
        line {
            spacer("»")
            appendSpace()
            variableKey("Y: ")
            variableValue(warp.y)
        }
        line {
            spacer("»")
            appendSpace()
            variableKey("Z: ")
            variableValue(warp.z)
        }
    }
}

fun SurfComponentBuilder.appendBlob() = append {
    darkSpacer("▪")
    appendSpace()
}

fun SlotClickContext.playLockedSound() {
    player.playSound(true) {
        type(BukkitSound.BLOCK_CHEST_LOCKED)
    }
}

fun SlotClickContext.playGeneralClickSound() {
    player.playSound(true) {
        type(BukkitSound.UI_BUTTON_CLICK)
    }
}

fun SlotClickContext.playNewPageSound() {
    player.playSound(true) {
        type(BukkitSound.ENTITY_CHICKEN_EGG)
    }
}

fun Player.canModifyBuildingWorld() = this.hasPermission(
    PermissionRegistry.BUILDER
)

fun createMemberItem(memberUuid: UUID, removable: Boolean = false) =
    buildItem(Material.PLAYER_HEAD) {
        val offlinePlayer = Bukkit.getOfflinePlayer(memberUuid)
        val name = offlinePlayer.name ?: memberUuid.toString()

        displayName {
            variableValue(name)
        }

        editMeta(SkullMeta::class.java) {
            it.owningPlayer = offlinePlayer
        }

        buildLore {
            line {
                spacer("» $memberUuid")
            }

            if (removable) {
                emptyLine()
                line {
                    spacer("»")
                    appendSpace()
                    white("Klicke, um dieses Mitglied zu entfernen")
                }
            }
        }
    }
