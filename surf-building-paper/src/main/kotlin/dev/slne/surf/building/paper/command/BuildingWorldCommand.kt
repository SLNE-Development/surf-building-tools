package dev.slne.surf.building.paper.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.building.paper.command.argument.buildingWorldArgument
import dev.slne.surf.building.paper.menu.showBuildingWorldMenu
import dev.slne.surf.building.paper.permission.PermissionRegistry
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit

fun buildingWorldCommand() = commandTree("buildingworld") {
    withAliases("bWorld", "bw")
    withPermission(PermissionRegistry.COMMAND)

    playerExecutor { player, _ ->
        showBuildingWorldMenu(player)
    }

    literalArgument("create") {
        stringArgument("name") {
            playerExecutor { player, args ->
                val name: String by args

                val success =
                    buildingWorldService.createBuildingWorld(
                        name, player.name, player.uniqueId,
                        BuildingWorld.Type.VOID
                    )

                if (success != null) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich erstellt!")
                        append {
                            spacer(" [")
                            success("Beitreten")
                            spacer("]")
                            clickEvent(ClickEvent.callback {
                                buildingWorldService.joinAndOrLoadBuildingWorld(
                                    player,
                                    success.buildingWorldId
                                )
                            })
                        }
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
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

                val success =
                    buildingWorldService.joinAndOrLoadBuildingWorld(player, bWorld.buildingWorldId)

                if (success) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du wurdest erfolgreich in die Bau-Welt teleportiert!")
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Es ist ein Fehler bei der Teleportation in die Bau-Welt aufgetreten!")
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
                            appendSuccessPrefix()
                            success("Die Bau-Welt wurde erfolgreich gelöscht!")
                        }
                    } else {
                        player.sendText {
                            appendErrorPrefix()
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

                val success = buildingWorldService.loadBuildingWorld(
                    bWorld.buildingWorldId
                )

                if (success) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich geladen!")
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Es ist ein Fehler beim Laden der Bau-Welt aufgetreten!")
                    }
                }
            }
        }
    }

    literalArgument("done") {
        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                val success = buildingWorldService.changeStatus(
                    bWorld, BuildingWorld.Status.DONE
                )

                if (success) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich als ")
                        variableValue("'Fertiggestellt'")
                        success(" markiert.")
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Bau Welt ist bereits als 'Fertiggestellt' markiert!")
                    }
                }
            }
        }
    }

    literalArgument("published") {
        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                val success = buildingWorldService.changeStatus(
                    bWorld, BuildingWorld.Status.PUBLISHED
                )

                if (success) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Bau-Welt wurde erfolgreich als ")
                        variableValue("'Veröffentlicht'")
                        success(" markiert.")
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Die Bau Welt ist bereits als 'Veröffentlicht' markiert!")
                    }
                }
            }
        }
    }

    literalArgument("lobby") {
        playerExecutor { player, _ ->
            player.teleportAsync(
                Bukkit.getWorld(buildingConfig.lobbyWorldName)?.spawnLocation
                    ?: error("Lobby world not found")
            ).thenRun {
                player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest erfolgreich in Bau-Server Lobby teleportiert!")
                }
            }
        }
    }
}