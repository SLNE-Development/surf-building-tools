package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.showTitle
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Suppress("UnstableApiUsage")
object ConnectionListener : Listener {
    @EventHandler
    fun onSpawnLocation(event: AsyncPlayerSpawnLocationEvent) {
        event.spawnLocation =
            Bukkit.getWorld(buildingConfig.lobbyWorldName)?.spawnLocation
                ?: error("Lobby world not found")
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.showTitle {
            title {
                note("Willkommen zurück, ".toSmallCaps(), TextDecoration.BOLD)
            }

            subtitle {
                variableValue(event.player.name)
            }

            times {
                fadeIn(10)
                stay(40)
                fadeOut(20)
            }
        }
    }
}