package dev.slne.surf.building.service

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.building.buildingConfig
import dev.slne.surf.building.plugin
import dev.slne.surf.building.util.addGeneratorToBukkitYml
import dev.slne.surf.building.util.generateBuildingWorldId
import dev.slne.surf.building.util.removeGeneratorFromBukkitYml
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.generator.BuildingWorldGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.*
import org.bukkit.block.BlockType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

object WorldManager {
    private val buildingWorldsMap = mutableObject2ObjectMapOf<String, BuildingWorld>()
    val buildingWorlds get() = buildingWorldsMap.values

    fun findBuildingWorlds() = buildingWorldsMap.values
    fun findBuildingWorldById(id: String) = buildingWorldsMap[id]
    fun findBuildingWorldByWorld(world: World) =
        buildingWorlds.firstOrNull { it.worldUuid == world.uid }

    suspend fun createWorld(
        buildingWorldName: String,
        authorName: String,
        authorUuid: UUID,
        type: BuildingWorld.Type,
        displayItem: Material = Material.GRASS_BLOCK
    ): BuildingWorld? = withContext(plugin.globalRegionDispatcher) {
        val id = generateBuildingWorldId()

        val world = when (type) {
            BuildingWorld.Type.FLAT -> WorldCreator
                .name("bw-$id")
                .type(WorldType.FLAT)
                .generateStructures(false)
                .createWorld()

            BuildingWorld.Type.VOID -> WorldCreator
                .name("bw-$id")
                .generator(BuildingWorldGenerator)
                .generateStructures(false)
                .createWorld()
        } ?: return@withContext null

        if (type == BuildingWorld.Type.VOID) {
            addGeneratorToBukkitYml(world.name)
        }

        world.updateSettings()

        val bWorld = BuildingWorld(
            buildingWorldName = buildingWorldName,
            buildingWorldId = id,
            worldName = world.name,
            worldUuid = world.uid,
            authorName = authorName,
            authorUuid = authorUuid,
            status = BuildingWorld.Status.EDITING,
            createdAt = OffsetDateTime.now(),
            type = type,
            displayItem = displayItem
        )

        buildingWorldsMap[bWorld.buildingWorldId] = bWorld

        WorldConfigManager.saveWorld(bWorld, world)
        return@withContext bWorld
    }

    private fun World.updateSettings() {
        this.setSpawnLocation(0, 0, 0)
        this.setBlockData(0, -1, 0, BlockType.BEDROCK.createBlockData())
        this.setGameRule(GameRules.ADVANCE_TIME, false)
        this.setGameRule(GameRules.ADVANCE_WEATHER, false)
        this.setGameRule(GameRules.MOB_GRIEFING, false)
        this.setGameRule(GameRules.SPAWN_MOBS, false)
        this.setGameRule(GameRules.SPAWN_PATROLS, false)
        this.setGameRule(GameRules.SPAWN_WARDENS, false)
        this.setGameRule(GameRules.SPAWN_MONSTERS, false)
        this.setGameRule(GameRules.SPAWN_PHANTOMS, false)
        this.setGameRule(GameRules.RESPAWN_RADIUS, 0)
        this.setGameRule(GameRules.TNT_EXPLODES, false)
        this.setGameRule(GameRules.SPAWNER_BLOCKS_WORK, false)
        this.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)
    }

    fun changeStatus(buildingWorld: BuildingWorld, status: BuildingWorld.Status): Boolean {
        if (buildingWorld.status == status) {
            return false
        }

        saveBuildingWorld(buildingWorld.copy(status = status))
        return true
    }

    fun saveBuildingWorld(buildingWorld: BuildingWorld) {
        buildingWorldsMap[buildingWorld.buildingWorldId] = buildingWorld
        WorldConfigManager.updateWorldConfig(buildingWorld)
    }

    fun renameWorld(buildingWorld: BuildingWorld, name: String): BuildingWorld {
        val updated = buildingWorld.copy(buildingWorldName = name)
        saveBuildingWorld(updated)
        return updated
    }

    fun changeDisplayItem(buildingWorld: BuildingWorld, displayItem: Material): BuildingWorld {
        val updated = buildingWorld.copy(displayItem = displayItem)
        saveBuildingWorld(updated)
        return updated
    }

    fun addWarp(buildingWorld: BuildingWorld, warp: Warp): BuildingWorld {
        val updated = buildingWorld.copy(warps = buildingWorld.warps + warp)
        saveBuildingWorld(updated)
        return updated
    }

    fun updateWarp(buildingWorld: BuildingWorld, oldWarp: Warp, newWarp: Warp): BuildingWorld {
        val updated = buildingWorld.copy(warps = buildingWorld.warps.map { if (it == oldWarp) newWarp else it })
        saveBuildingWorld(updated)
        return updated
    }

    fun deleteWarp(buildingWorld: BuildingWorld, warp: Warp): BuildingWorld {
        val updated = buildingWorld.copy(warps = buildingWorld.warps.filter { it != warp })
        saveBuildingWorld(updated)
        return updated
    }

    fun getBuildingWorldByWorld(world: World) = buildingWorlds
        .firstOrNull { it.worldUuid == world.uid }

    fun joinBuildingWorld(player: Player, buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val world = Bukkit.getWorld(buildingWorld.worldName) ?: return false

        player.teleportAsync(world.spawnLocation)

        return true
    }

    fun cacheWorld(buildingWorld: BuildingWorld) {
        buildingWorldsMap[buildingWorld.buildingWorldId] = buildingWorld
    }

    suspend fun joinAndOrLoadBuildingWorld(player: HumanEntity, buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val world = Bukkit.getWorld(buildingWorld.worldName) ?: run {
            val worldCreator = WorldCreator.name(buildingWorld.worldName)
            if (buildingWorld.type == BuildingWorld.Type.VOID) {
                worldCreator.generator(BuildingWorldGenerator)
            }
            withContext(plugin.globalRegionDispatcher) {
                Bukkit.createWorld(worldCreator)
            }

        } ?: return false

        player.teleportAsync(world.spawnLocation)

        return true
    }

    suspend fun loadBuildingWorld(buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val worldCreator = WorldCreator.name(buildingWorld.worldName)
        if (buildingWorld.type == BuildingWorld.Type.VOID) {
            worldCreator.generator(BuildingWorldGenerator)
        }
        withContext(plugin.globalRegionDispatcher) {
            Bukkit.createWorld(worldCreator)
                ?: return@withContext false
        }

        return true
    }

    suspend fun deleteBuildingWorld(buildingWorldId: String): Boolean =
        withContext(Dispatchers.IO) {
            val buildingWorld = buildingWorlds
                .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return@withContext false

            val world = Bukkit.getWorld(buildingWorld.worldName)

            val lobbySpawn = Bukkit.getWorld(buildingConfig.lobbyWorldName)?.spawnLocation
                ?: return@withContext false

            val futures = mutableListOf<CompletableFuture<Boolean>>()

            world?.players?.forEach {
                futures.add(it.teleportAsync(lobbySpawn))
            }

            CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
                world?.let {
                    if (!Bukkit.unloadWorld(it, false)) {
                        error("Failed to unload world ${world.name}")
                    }
                }

                if (!buildingWorld.folder.exists() || !buildingWorld.folder.isDirectory) {
                    error("World folder for world ${buildingWorld.worldName} does not exist")
                }

                buildingWorld.folder.deleteRecursively()
                removeGeneratorFromBukkitYml(buildingWorld.worldName)
            }

            WorldConfigManager.invalidate(buildingWorldId)
            buildingWorldsMap.remove(buildingWorldId)

            return@withContext true
        }
}