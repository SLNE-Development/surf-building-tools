package dev.slne.surf.building.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import kotlinx.coroutines.delay
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.time.Duration.Companion.seconds

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
        plugin.launch {
            event.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_PLING)
                pitch(1f)
            }

            delay(1.seconds)

            event.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_PLING)
                pitch(2f)
            }
        }

        event.player.sendActionBar(buildText {
            note("Willkommen zurück, ".toSmallCaps(), TextDecoration.BOLD)
            variableValue(event.player.name)
        })
    }
}