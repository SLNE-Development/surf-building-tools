package dev.slne.surf.buildsystem.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.permission.PermissionRegistry

fun lobbyCommand() = commandTree("lobby") {
    withPermission(PermissionRegistry.COMMAND_LOBBY)

    playerExecutor { player, _ ->
        LobbyService.teleportToLobby(player).thenRun {
            player.sendText {
                appendSuccessPrefix()
                success("Du wurdest in die Bau-Server Lobby teleportiert!")
            }
        }
    }
}