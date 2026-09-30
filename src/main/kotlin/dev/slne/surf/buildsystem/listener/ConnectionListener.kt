package dev.slne.surf.buildsystem.listener

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.showTitle
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.service.WorldPlayerDataManager
import dev.slne.surf.buildsystem.util.appendBitmapTitle
import dev.slne.surf.buildsystem.util.buildSecondary
import dev.slne.surf.buildsystem.util.currentBuildingWorld
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
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
        event.spawnLocation = LobbyService.spawnLocation
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.showTitle {
            title {
                appendBitmapTitle("Willkommen")
            }

            subtitle {
                buildSecondary(player.name)
            }

            times {
                fadeIn(10)
                stay(40)
                fadeOut(20)
            }
        }

        LobbyService.prepare(player)
        playWelcomeSound(player)
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
