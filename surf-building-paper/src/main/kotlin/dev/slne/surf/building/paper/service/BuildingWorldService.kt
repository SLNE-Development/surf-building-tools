package dev.slne.surf.building.paper.service

import dev.slne.surf.building.paper.util.generateBuildingWorldId
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.building.paper.world.generator.BuildingWorldGenerator
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.WorldCreator
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import java.util.*

val buildingWorldService = BuildingWorldService()

class BuildingWorldService {
    val buildingWorlds = mutableObjectSetOf<BuildingWorld>()

    suspend fun createBuildingWorld(
        buildingWorldName: String,
        authorName: String,
        authorUuid: UUID
    ) {
        val id = generateBuildingWorldId()
        val world = WorldCreator.name("bw-$id").generator(BuildingWorldGenerator).createWorld()
            ?: error("Failed to create building world for id $id and author $authorName ($authorUuid)")

        world.setSpawnLocation(0, 0, 0)
        world.setBlockData(0, -1, 0, BlockType.BEDROCK.createBlockData())
        world.setGameRule<Boolean>(GameRules.ADVANCE_TIME, false)
        world.setGameRule<Boolean>(GameRules.ADVANCE_WEATHER, false)
        world.setGameRule<Boolean>(GameRules.MOB_GRIEFING, false)
        world.setGameRule<Boolean>(GameRules.SPAWN_MOBS, false)
        world.setGameRule<Boolean>(GameRules.SPAWN_PATROLS, false)
        world.setGameRule<Boolean>(GameRules.SPAWN_WARDENS, false)
        world.setGameRule<Boolean>(GameRules.SPAWN_MONSTERS, false)
        world.setGameRule<Boolean>(GameRules.SPAWN_PHANTOMS, false)
        world.setGameRule<Int>(GameRules.RESPAWN_RADIUS, 0)
        world.setGameRule<Boolean>(GameRules.TNT_EXPLODES, false)
        world.setGameRule<Boolean>(GameRules.SPAWNER_BLOCKS_WORK, false)
        world.setGameRule<Int>(GameRules.RANDOM_TICK_SPEED, 0)

        withContext(Dispatchers.IO) {
            world.save()
        }
    }

    fun joinBuildingWorld(player: Player, buildingWorldId: String) {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId }
            ?: error("Building world with id $buildingWorldId not found")

        val world = Bukkit.getWorld(buildingWorld.worldName)
            ?: error("Building world with id $buildingWorldId is not loaded")

        player.teleportAsync(world.spawnLocation)
    }

    suspend fun loadBuildingWorld(buildingWorldId: String) {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId }
            ?: error("Building world with id $buildingWorldId not found")

        withContext(Dispatchers.IO) {
            Bukkit.createWorld(WorldCreator.name(buildingWorld.worldName))
                ?: error("Failed to load building world with id $buildingWorldId")
        }
    }
}