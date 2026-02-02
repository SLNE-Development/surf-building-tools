package dev.slne.surf.building.paper.util

import dev.slne.surf.building.paper.service.buildingWorldService
import org.bukkit.World
import org.bukkit.entity.Player

fun generateBuildingWorldId(): String {
    val charset = ('a'..'z') + ('0'..'9')

    while (true) {
        val id = (1..8)
            .map { charset.random() }
            .joinToString("")

        val exists = buildingWorldService.buildingWorlds
            .any { it.buildingWorldId == id }

        if (!exists) {
            return id
        }
    }
}

fun World.isBuildingWorld(): Boolean {
    return buildingWorldService.buildingWorlds.any { it.worldUuid == this.uid }
}

fun Player.currentBuildingWorld() =
    buildingWorldService.buildingWorlds.find { it.currentPlayers.contains(this.uniqueId) }

