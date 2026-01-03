package dev.slne.surf.building.paper.service

import dev.slne.surf.building.paper.buildingConfig
import dev.slne.surf.building.paper.database.repository.buildingWorldRepository
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
import java.util.concurrent.CompletableFuture

val buildingWorldService = BuildingWorldService()

class BuildingWorldService {
    val buildingWorlds = mutableObjectSetOf<BuildingWorld>()

    suspend fun createBuildingWorld(
        buildingWorldName: String,
        authorName: String,
        authorUuid: UUID
    ): Boolean {
        val id = generateBuildingWorldId()
        val world = WorldCreator.name("bw-$id").generator(BuildingWorldGenerator).createWorld()
            ?: return false

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

        val bWorld = BuildingWorld(
            buildingWorldName = buildingWorldName,
            buildingWorldId = id,
            worldName = world.name,
            worldUuid = world.uid,
            authorName = authorName,
            authorUuid = authorUuid,
            createdAt = System.currentTimeMillis()
        )

        buildingWorlds.add(bWorld)
        buildingWorldRepository.saveBuildingWorld(bWorld)

        return true
    }

    fun joinBuildingWorld(player: Player, buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val world = Bukkit.getWorld(buildingWorld.worldName) ?: return false

        player.teleportAsync(world.spawnLocation)

        return true
    }

    suspend fun loadBuildingWorld(buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        withContext(Dispatchers.IO) {
            Bukkit.createWorld(WorldCreator.name(buildingWorld.worldName))
                ?: return@withContext false
        }

        return true
    }

    suspend fun deleteBuildingWorld(buildingWorldId: String): Boolean =
        withContext(Dispatchers.IO) {
            val buildingWorld = buildingWorlds
                .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return@withContext false

            val world = Bukkit.getWorld(buildingWorld.worldName)
                ?: return@withContext false

            val lobbySpawn = Bukkit.getWorld(buildingConfig.lobbyWorldName)?.spawnLocation
                ?: return@withContext false

            val futures = mutableListOf<CompletableFuture<Boolean>>()

            world.players.forEach {
                futures.add(it.teleportAsync(lobbySpawn))
            }

            CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
                if (Bukkit.getWorld(world.name) != null) {
                    if (!Bukkit.unloadWorld(world, true)) {
                        error("Failed to unload world ${world.name}")
                    }
                }

                val file = Bukkit.getWorldContainer().resolve(world.name)
                if (!file.exists() || !file.isDirectory) {
                    error("World folder for world ${world.name} does not exist")
                }

                file.deleteRecursively()
            }
            return@withContext true
        }
}