package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.building.paper.service.buildingWorldItemsService
import dev.slne.surf.building.paper.util.currentBuildingWorld
import dev.slne.surf.building.paper.util.isBuildingWorld
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.showTitle
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

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
                yellow("Willkommen zurück, ".toSmallCaps(), TextDecoration.BOLD)
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

        event.player.inventory.clear()
        event.player.inventory.heldItemSlot = 4
        event.player.inventory.setItem(4, plugin.menuItem)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.world.isBuildingWorld()) {
            event.player.currentBuildingWorld()?.let {
                buildingWorldItemsService.savePlayerData(event.player, it)
            }
        }
    }
}