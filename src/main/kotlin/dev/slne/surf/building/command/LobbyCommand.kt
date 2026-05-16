package dev.slne.surf.building.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.building.buildingConfig
import dev.slne.surf.building.permission.PermissionRegistry
import org.bukkit.Bukkit

fun lobbyCommand() = commandTree("lobby") {
    withPermission(PermissionRegistry.COMMAND_LOBBY)

    playerExecutor { player, _ ->
        player.teleportAsync(
            Bukkit.getWorld(buildingConfig.lobbyWorldName)?.spawnLocation
                ?: error("Lobby world not found")
        ).thenRun {
            player.sendText {
                appendSuccessPrefix()
                success("Du wurdest in die Bau-Server Lobby teleportiert!")
            }
        }
    }
}