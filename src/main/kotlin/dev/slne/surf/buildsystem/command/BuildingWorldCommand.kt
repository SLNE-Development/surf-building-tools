package dev.slne.surf.buildsystem.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.command.argument.buildingWorldArgument
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.world.BuildingWorld
import net.kyori.adventure.text.event.ClickEvent

fun buildingWorldCommand() = commandTree("buildingworld") {
    withAliases("bWorld", "bw")
    withPermission(PermissionRegistry.COMMAND)

    playerExecutor { player, _ ->
        centralMenu.open(player)
    }

    literalArgument("create") {
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_CREATE)

        stringArgument("name") {
            playerExecutorSuspend { player, args ->
                val name: String by args

                val success =
                    WorldManager.createWorld(
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
                                plugin.launch {
                                    WorldManager.joinAndOrLoadBuildingWorld(
                                        player,
                                        success.buildingWorldId
                                    )
                                }
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
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_JOIN)

        buildingWorldArgument("bWorld") {
            playerExecutorSuspend { player, args ->
                val bWorld: BuildingWorld by args

                val success =
                    WorldManager.joinAndOrLoadBuildingWorld(player, bWorld.buildingWorldId)

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
        withPermission(PermissionRegistry.WORLD_DELETE)

        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                plugin.launch {
                    val success = WorldManager.deleteBuildingWorld(bWorld.buildingWorldId)

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
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_LOAD)

        buildingWorldArgument("bWorld") {
            playerExecutorSuspend { player, args ->
                val bWorld: BuildingWorld by args

                val success = WorldManager.loadBuildingWorld(
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
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_DONE)

        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                val success = WorldManager.changeStatus(
                    bWorld, BuildingWorld.Status.DONE
                )

                if (success != null) {
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
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_PUBLISHED)

        buildingWorldArgument("bWorld") {
            playerExecutor { player, args ->
                val bWorld: BuildingWorld by args

                val success = WorldManager.changeStatus(
                    bWorld, BuildingWorld.Status.PUBLISHED
                )

                if (success != null) {
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

    literalArgument("import") {
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_IMPORT)


        stringArgument("folder") {
            replaceSuggestions(ArgumentSuggestions.stringCollection {
                WorldManager.findImportableWorldFolders()
            })

            playerExecutorSuspend { player, args ->
                val folder: String by args

                if (folder !in WorldManager.findImportableWorldFolders()) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Der Ordner ")
                        variableValue(folder)
                        error(" existiert nicht oder wurde bereits importiert!")
                    }
                    return@playerExecutorSuspend
                }

                val bWorld = WorldManager.importWorld(folder, player.name, player.uniqueId)

                if (bWorld != null) {
                    player.sendText {
                        appendSuccessPrefix()
                        success("Die Welt ")
                        variableValue(folder)
                        success(" wurde erfolgreich als Bau-Welt importiert!")
                        append {
                            spacer(" [")
                            success("Beitreten")
                            spacer("]")
                            clickEvent(ClickEvent.callback {
                                plugin.launch {
                                    WorldManager.joinAndOrLoadBuildingWorld(
                                        player,
                                        bWorld.buildingWorldId
                                    )
                                }
                            })
                        }
                    }
                } else {
                    player.sendText {
                        appendErrorPrefix()
                        error("Es ist ein Fehler beim Importieren der Welt aufgetreten!")
                    }
                }
            }
        }
    }

    literalArgument("lobby") {
        withPermission(PermissionRegistry.COMMAND_BUILDING_WORLD_LOBBY)

        playerExecutor { player, _ ->
            LobbyService.teleportToLobby(player).thenRun {
                player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest erfolgreich in die Bau-Server Lobby teleportiert!")
                }
            }
        }
    }
}