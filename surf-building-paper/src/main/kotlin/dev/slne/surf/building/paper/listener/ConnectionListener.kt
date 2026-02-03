package dev.slne.surf.building.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.building.paper.service.buildingWorldPlayerDataService
import dev.slne.surf.building.paper.util.currentBuildingWorld
import dev.slne.surf.building.paper.util.isBuildingWorld
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.showTitle
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import kotlinx.coroutines.delay
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.time.Duration.Companion.milliseconds

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

        plugin.launch {
            playWelcomeSound(event.player)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (event.player.world.isBuildingWorld()) {
            event.player.currentBuildingWorld()?.let {
                buildingWorldPlayerDataService.savePlayerData(event.player, it)
            }
        }
    }

    suspend fun playWelcomeSound(player: Player) {
        player.playSound(true) {
            type(Sound.BLOCK_NOTE_BLOCK_BELL)
            pitch(1.0f) // Pling
            volume(1f)
        }

        delay(150.milliseconds)

        player.playSound(true) {
            type(Sound.BLOCK_NOTE_BLOCK_BELL)
            pitch(1.3348f) // Pong (höherer Ton)
            volume(1f)
        }

        delay(120.milliseconds)

        player.playSound(true) {
            type(Sound.BLOCK_NOTE_BLOCK_BELL)
            pitch(1.1225f) // Ping (mittlerer Ton)
            volume(1f)
        }

        delay(180.milliseconds)

        player.playSound(true) {
            type(Sound.BLOCK_NOTE_BLOCK_BELL)
            pitch(1.0f) // Pling (wieder Grundton)
            volume(1f)
        }
    }


}