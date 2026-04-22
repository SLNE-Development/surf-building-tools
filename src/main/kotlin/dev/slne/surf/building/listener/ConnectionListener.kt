package dev.slne.surf.building.listener

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.showTitle
import dev.slne.surf.building.buildingConfig
import dev.slne.surf.building.plugin
import dev.slne.surf.building.service.WorldPlayerDataManager
import dev.slne.surf.building.util.currentBuildingWorld
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
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

        playWelcomeSound(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        event.player.currentBuildingWorld()?.let {
            WorldPlayerDataManager.savePlayerData(event.player, it)
        }
    }

    fun playWelcomeSound(player: Player) {
        player.playSound(true) {
            type(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE)
        }

        player.playSound(true) {
            type(Sound.ENTITY_FIREWORK_ROCKET_BLAST)
        }
    }
}