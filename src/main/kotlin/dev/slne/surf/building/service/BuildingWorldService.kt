package dev.slne.surf.building.service

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.building.buildingConfig
import dev.slne.surf.building.config.BuildingWorldConfig
import dev.slne.surf.building.plugin
import dev.slne.surf.building.util.addGeneratorToBukkitYml
import dev.slne.surf.building.util.generateBuildingWorldId
import dev.slne.surf.building.util.parseWorldType
import dev.slne.surf.building.util.removeGeneratorFromBukkitYml
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.generator.BuildingWorldGenerator
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.WarpConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.*
import org.bukkit.block.BlockType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import java.nio.file.Files
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.io.path.isDirectory

object BuildingWorldService {
    private val buildingWorldsMap = mutableObject2ObjectMapOf<String, BuildingWorld>()
    val buildingWorlds get() = buildingWorldsMap.values
    val buildingWorldConfigManagers =
        mutableObject2ObjectMapOf<String, SpongeConfigManager<BuildingWorldConfig>>()

    fun findBuildingWorlds() = buildingWorldsMap.values
    fun findBuildingWorldById(id: String) = buildingWorldsMap[id]
    fun findBuildingWorldByWorld(world: World) =
        buildingWorlds.firstOrNull { it.worldUuid == world.uid }

    fun createBuildingWorld(
        buildingWorldName: String,
        authorName: String,
        authorUuid: UUID,
        type: BuildingWorld.Type,
        displayItem: Material = Material.GRASS_BLOCK
    ): BuildingWorld? {
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
        } ?: return null

        if (type == BuildingWorld.Type.VOID) {
            addGeneratorToBukkitYml(world.name)
        }

        world.setSpawnLocation(0, 0, 0)
        world.setBlockData(0, -1, 0, BlockType.BEDROCK.createBlockData())
        world.setGameRule(GameRules.ADVANCE_TIME, false)
        world.setGameRule(GameRules.ADVANCE_WEATHER, false)
        world.setGameRule(GameRules.MOB_GRIEFING, false)
        world.setGameRule(GameRules.SPAWN_MOBS, false)
        world.setGameRule(GameRules.SPAWN_PATROLS, false)
        world.setGameRule(GameRules.SPAWN_WARDENS, false)
        world.setGameRule(GameRules.SPAWN_MONSTERS, false)
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false)
        world.setGameRule(GameRules.RESPAWN_RADIUS, 0)
        world.setGameRule(GameRules.TNT_EXPLODES, false)
        world.setGameRule(GameRules.SPAWNER_BLOCKS_WORK, false)
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)

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

        surfConfigApi.createSpongeYmlConfig(
            BuildingWorldConfig::class.java,
            world.worldPath,
            "building-world-config.yml"
        )

        buildingWorldConfigManagers[id] = surfConfigApi.createSpongeYmlConfigManager(
            BuildingWorldConfig::class.java,
            world.worldPath,
            "building-world-config.yml"
        )

        buildingWorldConfigManagers[id]?.apply {
            config.buildingWorldName = bWorld.buildingWorldName
            config.buildingWorldId = bWorld.buildingWorldId
            config.worldName = bWorld.worldName
            config.worldUuid = bWorld.worldUuid
            config.authorName = bWorld.authorName
            config.authorUuid = bWorld.authorUuid
            config.status = bWorld.status.name
            config.createdAtString = bWorld.createdAt.toString()
            config.worldType = bWorld.type.name
            config.displayItemName = bWorld.displayItem.name
            config.warps = bWorld.warps.map { warp ->
                WarpConfig(
                    name = warp.name,
                    x = warp.x,
                    y = warp.y,
                    z = warp.z,
                    pitch = warp.pitch,
                    yaw = warp.yaw,
                    displayItemName = warp.displayItem.name
                )
            }.toMutableList()

            this.save()
        }

        return bWorld
    }

    fun changeStatus(buildingWorld: BuildingWorld, status: BuildingWorld.Status): Boolean {
        if (buildingWorld.status == status) {
            return false
        }

        saveBuildingWorld(buildingWorld.copy(status = status))
        return true
    }

    fun saveBuildingWorld(buildingWorld: BuildingWorld) {
        buildingWorldConfigManagers[buildingWorld.buildingWorldId]?.apply {
            config.buildingWorldName = buildingWorld.buildingWorldName
            config.buildingWorldId = buildingWorld.buildingWorldId
            config.worldName = buildingWorld.worldName
            config.worldUuid = buildingWorld.worldUuid
            config.authorName = buildingWorld.authorName
            config.authorUuid = buildingWorld.authorUuid
            config.status = buildingWorld.status.name
            config.createdAtString = buildingWorld.createdAt.toString()
            config.worldType = buildingWorld.type.name
            config.displayItemName = buildingWorld.displayItem.name
            config.warps = buildingWorld.warps.map { warp ->
                WarpConfig(
                    name = warp.name,
                    x = warp.x,
                    y = warp.y,
                    z = warp.z,
                    pitch = warp.pitch,
                    yaw = warp.yaw,
                    displayItemName = warp.displayItem.name
                )
            }.toMutableList()

            this.save()
        }

        buildingWorldsMap[buildingWorld.buildingWorldId] = buildingWorld
    }

    fun cacheAllBuildingWorlds() {
        plugin.logger.info("Loading Building Worlds...")

        Files.walk(Bukkit.getWorldContainer().toPath()).filter {
            it.isDirectory()
        }.forEach {
            if (Files.exists(it.resolve("building-world-config.yml"))) {
                val configManager = surfConfigApi.createSpongeYmlConfigManager(
                    BuildingWorldConfig::class.java,
                    it,
                    "building-world-config.yml"
                )

                val config = configManager.config

                val bWorld = BuildingWorld(
                    buildingWorldName = config.buildingWorldName,
                    buildingWorldId = config.buildingWorldId,
                    worldName = config.worldName,
                    worldUuid = config.worldUuid,
                    authorName = config.authorName,
                    authorUuid = config.authorUuid,
                    status = BuildingWorld.Status.valueOf(config.status),
                    createdAt = OffsetDateTime.parse(config.createdAtString),
                    type = parseWorldType(config.worldType),
                    displayItem = try {
                        Material.valueOf(config.displayItemName)
                    } catch (e: IllegalArgumentException) {
                        plugin.logger.warning("Invalid display item '${config.displayItemName}' for world '${config.buildingWorldName}', using default GRASS_BLOCK")
                        Material.GRASS_BLOCK
                    },
                    warps = config.warps.map { warpConfig ->
                        Warp(
                            name = warpConfig.name,
                            x = warpConfig.x,
                            y = warpConfig.y,
                            z = warpConfig.z,
                            pitch = warpConfig.pitch,
                            yaw = warpConfig.yaw,
                            displayItem = try {
                                Material.valueOf(warpConfig.displayItemName)
                            } catch (e: IllegalArgumentException) {
                                plugin.logger.warning("Invalid warp display item '${warpConfig.displayItemName}' for warp '${warpConfig.name}', using default COMPASS")
                                Material.COMPASS
                            }
                        )
                    }
                )

                buildingWorldsMap[bWorld.buildingWorldId] = bWorld
                buildingWorldConfigManagers[config.buildingWorldId] = configManager

                plugin.logger.info("Loaded Building World '${bWorld.buildingWorldName}' (#${bWorld.buildingWorldId}) by ${bWorld.authorName}!")
            }
        }

        plugin.logger.info("Finished loading Building Worlds. Total: ${buildingWorldsMap.size}")
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

    fun joinAndOrLoadBuildingWorld(player: HumanEntity, buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val world = Bukkit.getWorld(buildingWorld.worldName) ?: run {
            val worldCreator = WorldCreator.name(buildingWorld.worldName)
            if (buildingWorld.type == BuildingWorld.Type.VOID) {
                worldCreator.generator(BuildingWorldGenerator)
            }
            Bukkit.createWorld(worldCreator)
        } ?: return false

        player.teleportAsync(world.spawnLocation)

        return true
    }

    fun loadBuildingWorld(buildingWorldId: String): Boolean {
        val buildingWorld = buildingWorlds
            .firstOrNull { it.buildingWorldId == buildingWorldId } ?: return false

        val worldCreator = WorldCreator.name(buildingWorld.worldName)
        if (buildingWorld.type == BuildingWorld.Type.VOID) {
            worldCreator.generator(BuildingWorldGenerator)
        }
        Bukkit.createWorld(worldCreator)
            ?: return false

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

            buildingWorldConfigManagers.remove(buildingWorldId)
            buildingWorldsMap.remove(buildingWorldId)
            return@withContext true
        }
}