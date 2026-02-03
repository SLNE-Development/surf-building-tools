package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.service.buildingWorldItemsService
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.currentBuildingWorld
import dev.slne.surf.building.paper.util.isBuildingWorld
import dev.slne.surf.surfapi.bukkit.api.scoreboard.ObsoleteScoreboardApi
import dev.slne.surf.surfapi.bukkit.api.scoreboard.SurfScoreboard
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.world.WorldSaveEvent
import java.util.*

@OptIn(ObsoleteScoreboardApi::class)
object PlayerWorldListener : Listener {
    private val _scoreboards = mutableObject2ObjectMapOf<UUID, SurfScoreboard>()

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        val toWorld = event.player.world

        hideScoreboard(player)

        if (toWorld.isBuildingWorld()) {
            showScoreboard(player)

            player.currentBuildingWorld()?.let {
                it.currentPlayers.add(player.uniqueId)

                buildingWorldItemsService.loadPlayerData(player, it)
            }
        } else {
            if (event.from.isBuildingWorld()) {
                buildingWorldService.getBuildingWorldByWorld(event.from)?.let {
                    buildingWorldItemsService.savePlayerData(player, it)
                }
            }

            buildingWorldService.buildingWorlds.forEach { it.currentPlayers.remove(player.uniqueId) }
        }
    }

    @EventHandler
    fun onSave(event: WorldSaveEvent) {
        event.world.players.forEach {
            val buildingWorld = it.currentBuildingWorld() ?: return@forEach
            buildingWorldItemsService.savePlayerData(it, buildingWorld)
        }

        buildingWorldService.buildingWorlds.forEach {
            buildingWorldService.saveBuildingWorld(it)
        }
    }

    private fun showScoreboard(player: Player) {
        _scoreboards[player.uniqueId] = surfBukkitApi.createScoreboard(buildText {
            primary("     Bau Server     ", TextDecoration.BOLD)
        })
            .addLine(buildText {
                info("Welt: ")
            })
            .addLine(buildText {
                variableValue(player.currentBuildingWorld()?.buildingWorldName ?: "Unbekannt")
            })
            .addEmptyLine()
            .addLine(buildText {
                info("Besitzer: ")
            })
            .addLine(buildText {
                variableValue(player.currentBuildingWorld()?.authorName ?: "/")
            })
            .addEmptyLine()
            .addLine(buildText {
                info("Status: ")
            })
            .addLine(buildText {
                variableValue(player.currentBuildingWorld()?.status?.displayName ?: "/")
            })
            .addEmptyLine()
            .build()

        _scoreboards[player.uniqueId]?.enable()
        _scoreboards[player.uniqueId]?.addViewer(player)
    }

    private fun hideScoreboard(player: Player) {
        _scoreboards[player.uniqueId]?.disable()
        _scoreboards.remove(player.uniqueId)
    }
}