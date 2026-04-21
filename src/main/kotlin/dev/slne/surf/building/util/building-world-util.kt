package dev.slne.surf.building.util

import dev.slne.surf.building.service.BuildingWorldService
import dev.slne.surf.building.world.BuildingWorld
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

fun generateBuildingWorldId(): String {
    val charset = ('a'..'z') + ('0'..'9')

    while (true) {
        val id = (1..8)
            .map { charset.random() }
            .joinToString("")

        val exists = BuildingWorldService.buildingWorlds
            .any { it.buildingWorldId == id }

        if (!exists) {
            return id
        }
    }
}

fun parseWorldType(worldTypeString: String): BuildingWorld.Type {
    return if (worldTypeString.isNotEmpty()) {
        try {
            BuildingWorld.Type.valueOf(worldTypeString)
        } catch (e: IllegalArgumentException) {
            BuildingWorld.Type.FLAT
        }
    } else {
        BuildingWorld.Type.FLAT
    }
}

fun World.isBuildingWorld(): Boolean {
    return BuildingWorldService.buildingWorlds.any { it.worldUuid == this.uid }
}

fun Player.currentBuildingWorld() =
    BuildingWorldService.buildingWorlds.find { buildingWorld ->
        buildingWorld.worldOrNull?.uid == this.world.uid
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

val World.buildingWorld get() = BuildingWorldService.getBuildingWorldByWorld(this)



