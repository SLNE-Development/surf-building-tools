package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.plugin
import dev.slne.surf.building.paper.service.buildingWorldPlayerDataService
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.currentBuildingWorld
import dev.slne.surf.building.paper.util.infoColored
import dev.slne.surf.building.paper.util.isBuildingWorld
import dev.slne.surf.building.paper.util.primaryColored
import dev.slne.surf.surfapi.bukkit.api.scoreboard.ObsoleteScoreboardApi
import dev.slne.surf.surfapi.bukkit.api.scoreboard.SurfScoreboard
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
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
        buildingWorldService.buildingWorlds.forEach { it.currentPlayers.remove(player.uniqueId) }

        if (event.from.isBuildingWorld()) {
            buildingWorldService.getBuildingWorldByWorld(event.from)?.let {
                buildingWorldPlayerDataService.savePlayerData(player, it)
            }
        }

        if (toWorld.isBuildingWorld()) {
            showScoreboard(player)

            player.currentBuildingWorld()?.let {
                it.currentPlayers.add(player.uniqueId)

                buildingWorldPlayerDataService.loadPlayerData(player, it)
            }
        } else {
            event.player.inventory.clear()
            event.player.inventory.heldItemSlot = 4
            event.player.inventory.setItem(4, plugin.menuItem)
        }
    }

    @EventHandler
    fun onSave(event: WorldSaveEvent) {
        event.world.players.forEach {
            val buildingWorld = it.currentBuildingWorld() ?: return@forEach
            buildingWorldPlayerDataService.savePlayerData(it, buildingWorld)
        }

        buildingWorldService.buildingWorlds.forEach {
            buildingWorldService.saveBuildingWorld(it)
        }
    }

    private fun showScoreboard(player: Player) {
        _scoreboards[player.uniqueId] = surfBukkitApi.createScoreboard(buildText {
            primaryColored("     Bau Server     ".toSmallCaps(), TextDecoration.BOLD)
        })
            .addEmptyLine()
            .addLine(buildText {
                infoColored("Welt: ".toSmallCaps())
            })
            .addLine(buildText {
                variableValue(player.currentBuildingWorld()?.buildingWorldName ?: "Unbekannt")
            })
            .addEmptyLine()
            .addLine(buildText {
                infoColored("Besitzer: ".toSmallCaps())
            })
            .addUpdatableLine {
                buildText {
                    variableValue(player.currentBuildingWorld()?.authorName ?: "/")
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                infoColored("Status: ".toSmallCaps())
            })
            .addUpdatableLine {
                buildText {
                    variableValue(player.currentBuildingWorld()?.status?.displayName ?: "/")
                }
            }
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