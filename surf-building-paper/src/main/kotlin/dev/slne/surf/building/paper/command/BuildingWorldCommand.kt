package dev.slne.surf.building.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.building.paper.command.argument.buildingWorldArgument
import dev.slne.surf.building.paper.permission.PermissionRegistry
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun buildingWorldCommand() = commandTree("buildingworld") {
    withPermission(PermissionRegistry.COMMAND)
    literalArgument("create") {
        stringArgument("name") {
            playerExecutor { player, args ->
                val name: String by args

                val success =
                    buildingWorldService.createBuildingWorld(name, player.name, player.uniqueId)

                if (success) {
                    player.sendText {
                        appendPrefix()
                        success("Die Bau-Welt wurde erfolgreich erstellt!")
                    }
                } else {
                    player.sendText {
                        appendPrefix()
                        error("Es ist ein Fehler bei der Erstellung der Bau-Welt aufgetreten!")
                    }
                }
            }
        }
    }

    literalArgument("join") {
        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                plugin.launch {
                    val success =
                        buildingWorldService.joinBuildingWorld(player, bWorld.buildingWorldId)

                    if (success) {
                        player.sendText {
                            appendPrefix()
                            success("Du wurdest erfolgreich in die Bau-Welt teleportiert!")
                        }
                    } else {
                        player.sendText {
                            appendPrefix()
                            error("Es ist ein Fehler bei der Teleportation in die Bau-Welt aufgetreten!")
                        }
                    }
                }
            }
        }
    }

    literalArgument("delete") {
        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                plugin.launch {
                    val success = buildingWorldService.deleteBuildingWorld(bWorld.buildingWorldId)

                    if (success) {
                        player.sendText {
                            appendPrefix()
                            success("Die Bau-Welt wurde erfolgreich gelöscht!")
                        }
                    } else {
                        player.sendText {
                            appendPrefix()
                            error("Es ist ein Fehler bei der Löschung der Bau-Welt aufgetreten!")
                        }
                    }
                }
            }
        }
    }

    literalArgument("load") {
        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                plugin.launch {
                    val success = buildingWorldService.joinAndOrLoadBuildingWorld(
                        player,
                        bWorld.buildingWorldId
                    )

                    if (success) {
                        player.sendText {
                            appendPrefix()
                            success("Die Bau-Welt wurde erfolgreich geladen!")
                        }
                    } else {
                        player.sendText {
                            appendPrefix()
                            error("Es ist ein Fehler beim Laden der Bau-Welt aufgetreten!")
                        }
                    }
                }
            }
        }
    }

    literalArgument("debug") {
        playerExecutor { player, args ->
            plugin.launch {
                buildingWorldService.buildingWorlds.forEach {
                    player.sendMessage("BuildingWorld: ${it.buildingWorldName} (${it.buildingWorldId}) - World: ${it.worldName} (${it.worldUuid}) - Author: ${it.authorName} (${it.authorUuid}) - CreatedAt: ${it.createdAt}")
                }
            }
        }
    }
}