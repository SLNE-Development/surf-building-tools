package dev.slne.surf.building.paper.util

import dev.slne.surf.building.paper.service.buildingWorldService
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

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
    buildingWorldService.buildingWorlds.find { buildingWorld ->
        buildingWorld.world?.uid == this.world.uid
    }

fun World.teleportToHighestSpawn(player: Player): CompletableFuture<Boolean> {
    val spawnX = this.spawnLocation.x.toInt()
    val spawnZ = this.spawnLocation.z.toInt()
    val highestY = this.getHighestBlockYAt(spawnX, spawnZ)

    return player.teleportAsync(
        this.getBlockAt(spawnX, highestY, spawnZ).location.add(
            0.5,
            1.0,
            0.5
        )
    )
}



