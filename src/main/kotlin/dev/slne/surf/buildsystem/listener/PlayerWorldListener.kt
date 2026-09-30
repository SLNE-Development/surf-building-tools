package dev.slne.surf.buildsystem.listener

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.paper.scoreboard.SurfScoreboard
import dev.slne.surf.api.paper.scoreboard.SurfScoreboardApi
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.service.WorldPlayerDataManager
import dev.slne.surf.buildsystem.util.currentBuildingWorld
import dev.slne.surf.buildsystem.util.appendBitmapTitle
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.buildSecondary
import dev.slne.surf.buildsystem.util.isBuildingWorld
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.world.WorldSaveEvent
import java.util.*

object PlayerWorldListener : Listener {
    private val _scoreboards = mutableObject2ObjectMapOf<UUID, SurfScoreboard>()

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        val toWorld = event.player.world

        hideScoreboard(player)
        WorldManager.buildingWorlds.forEach { it.currentPlayers.remove(player.uniqueId) }

        if (event.from.isBuildingWorld()) {
            WorldManager.getBuildingWorldByWorld(event.from)?.let {
                WorldPlayerDataManager.savePlayerData(player, it)
            }
        }

        if (toWorld.isBuildingWorld()) {
            showScoreboard(player)

            player.currentBuildingWorld()?.let {
                it.currentPlayers.add(player.uniqueId)

                WorldPlayerDataManager.loadPlayerData(player, it)
            }
        } else if (LobbyService.isLobby(toWorld)) {
            LobbyService.prepare(player)
        }
    }

    @EventHandler
    fun onSave(event: WorldSaveEvent) {
        event.world.players.forEach {
            val buildingWorld = it.currentBuildingWorld() ?: return@forEach
            WorldPlayerDataManager.savePlayerData(it, buildingWorld)
        }

        WorldManager.buildingWorlds.forEach {
            WorldManager.saveBuildingWorld(it)
        }
    }

    private fun showScoreboard(player: Player) {
        _scoreboards[player.uniqueId] = SurfScoreboardApi.createScoreboard(buildText {
            buildPrimary("     Bau Server     ".toSmallCaps(), TextDecoration.BOLD)
        })
            .addEmptyLine()
            .addLine(buildText {
                appendBitmapTitle("Welt")
            })
            .addUpdatableLine {
                buildText {
                    buildSecondary(player.currentBuildingWorld()?.buildingWorldName ?: "Unbekannt")
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                appendBitmapTitle("Besitzer")
            })
            .addUpdatableLine {
                buildText {
                    buildSecondary(player.currentBuildingWorld()?.authorName ?: "/")
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                appendBitmapTitle("Status")
            })
            .addUpdatableLine {
                buildText {
                    buildSecondary(player.currentBuildingWorld()?.status?.displayName ?: "/")
                }
            }
            .addEmptyLine()
            .buildAutoUpdatable()

        _scoreboards[player.uniqueId]?.enable()
        _scoreboards[player.uniqueId]?.addViewer(player)
    }

    private fun hideScoreboard(player: Player) {
        _scoreboards[player.uniqueId]?.disable()
        _scoreboards.remove(player.uniqueId)
    }
}