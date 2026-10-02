package dev.slne.surf.buildsystem.gui.view

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.LoreBuilder
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.util.BukkitSound
import dev.slne.surf.api.paper.util.toOfflinePlayers
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.util.*
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

val validDisplayMaterials by lazy {
    Material.entries
        .filter { !it.isLegacy && it.isItem && !it.isAir }
        .sortedBy { it.name }
}

val backItem
    get() = ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
        displayName {
            error("Zurück")
        }
    }

val closeItem
    get() = ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
        displayName {
            error("Schließen")
        }
    }

val cancelItem
    get() = ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
        displayName {
            error("Abbrechen")
        }
    }

fun confirmItem(title: String) = ViewIcon(ViewIconType.CHECK, ViewIconColor.GREEN).build {
    displayName {
        success(title)
    }

    buildLore {
        emptyLine()
        line {
            appendBlob()
            error("Diese Aktion kann nicht rückgängig gemacht werden!".toSmallCaps())
        }
    }
}

fun LoreBuilder.section(title: String, background: TextColor = BUILD_PRIMARY) {
    emptyLine()
    line { appendBitmapTitle(title, background) }
}

fun LoreBuilder.entry(key: String, value: SurfComponentBuilder.() -> Unit) = line {
    spacer("-")
    appendSpace()
    buildPrimary("$key: ")
    value()
}

fun LoreBuilder.entry(key: String, value: Any) = entry(key) { buildSecondary(value) }

fun LoreBuilder.hint(text: String) = line {
    appendBlob()
    buildUseless(text.toSmallCaps())
}

fun LoreBuilder.action(key: String, text: String) = line {
    appendBlob()
    buildHighlight(key.toSmallCaps())
    buildUseless(" $text".toSmallCaps())
}

fun LoreBuilder.selection(entries: List<String>, selected: Int) =
    entries.forEachIndexed { index, entry ->
        line {
            if (index == selected) {
                appendSpace()
                spacer("-")
                appendSpace()
                buildHighlight(entry)
            } else {
                spacer("-")
                appendSpace()
                white(entry)
            }
        }
    }

fun statusIconColor(status: BuildingWorld.Status) = when (status) {
    BuildingWorld.Status.EDITING -> ViewIconColor.YELLOW
    BuildingWorld.Status.DONE -> ViewIconColor.BLUE
    BuildingWorld.Status.PUBLISHED -> ViewIconColor.GREEN
}

fun createWorldItem(buildingWorld: BuildingWorld, joinOrEdit: Boolean = false) =
    buildItem(buildingWorld.displayItem) {
        displayName {
            buildPrimary(buildingWorld.buildingWorldName)
        }

        buildLore {
            section("Status")
            line {
                append(buildingWorld.status.icon)
                appendSpace()
                buildSecondary(buildingWorld.status.displayName)
            }

            section("Informationen")
            entry("Ersteller", buildingWorld.authorName)
            entry("Mitglieder") {
                if (buildingWorld.members.isEmpty()) {
                    error("Keine")
                } else {
                    buildHighlight("(${buildingWorld.members.size}) ")
                    buildSecondary(
                        buildingWorld.members.toOfflinePlayers()
                            .joinToString(", ") { it.name ?: "#null" })
                }
            }
            entry("Warps") {
                if (buildingWorld.warps.isEmpty()) {
                    error("Keine")
                } else {
                    buildHighlight("(${buildingWorld.warps.size}) ")
                    buildSecondary(buildingWorld.warps.joinToString { it.name })
                }
            }
            entry("Typ", buildingWorld.type.displayName)
            entry("Erstellt am", buildingWorld.createdAt.format(dateTimeFormatter))

            if (joinOrEdit) {
                section("Aktionen")
                action("Linksklick", "zum Betreten")
                action("Rechtsklick", "zum Bearbeiten (nur Builder)")
            }

            emptyLine()
            line {
                darkSpacer("#${buildingWorld.buildingWorldId}")
            }
        }
    }

fun createWarpItem(warp: Warp, clickable: Boolean = false) = buildItem(warp.displayItem) {
    displayName {
        buildPrimary(warp.name)
    }

    buildLore {
        section("Position")
        entry("X", "%.1f".format(warp.x))
        entry("Y", "%.1f".format(warp.y))
        entry("Z", "%.1f".format(warp.z))

        if (clickable) {
            emptyLine()
            hint("Klicke, um den Warp anzusehen.")
        }
    }
}

fun createMemberItem(memberUuid: UUID, removable: Boolean = false) =
    buildItem(Material.PLAYER_HEAD) {
        val offlinePlayer = Bukkit.getOfflinePlayer(memberUuid)
        val name = offlinePlayer.name ?: memberUuid.toString()

        displayName {
            buildPrimary(name)
        }

        editMeta(SkullMeta::class.java) {
            it.owningPlayer = offlinePlayer
        }

        buildLore {
            if (removable) {
                emptyLine()
                hint("Klicke, um dieses Mitglied zu entfernen.")
            }

            emptyLine()
            line {
                darkSpacer(memberUuid.toString())
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

fun Player.canModifyBuildingWorld() = this.hasPermission(
    PermissionRegistry.BUILDER
)

fun Player.canManageWarps(buildingWorld: BuildingWorld) =
    canModifyBuildingWorld() || uniqueId in buildingWorld.members
