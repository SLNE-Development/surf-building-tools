package dev.slne.surf.building.service

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.building.config.BuildingWorldConfig
import dev.slne.surf.building.plugin
import dev.slne.surf.building.util.parseWorldType
import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.Warp
import dev.slne.surf.building.world.WarpConfig
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import java.nio.file.Files
import java.time.OffsetDateTime
import kotlin.io.path.isDirectory

object WorldConfigManager {
    val buildingWorldConfigManagers =
        mutableObject2ObjectMapOf<String, SpongeConfigManager<BuildingWorldConfig>>()


    fun saveWorld(buildingWorld: BuildingWorld, world: World) {
        surfConfigApi.createSpongeYmlConfig(
            BuildingWorldConfig::class.java,
            world.worldPath,
            "building-world-config.yml"
        )

        buildingWorldConfigManagers[buildingWorld.buildingWorldId] =
            surfConfigApi.createSpongeYmlConfigManager(
                BuildingWorldConfig::class.java,
                world.worldPath,
                "building-world-config.yml"
            )

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
    }

    fun updateWorldConfig(buildingWorld: BuildingWorld) {
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
    }

    fun invalidate(buildingWorldId: String) = buildingWorldConfigManagers.remove(buildingWorldId)

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
                val bWorld = createWorld(config)

                WorldManager.cacheWorld(bWorld)
                buildingWorldConfigManagers[config.buildingWorldId] =
                    configManager

                plugin.logger.info("Loaded Building World '${bWorld.buildingWorldName}' (#${bWorld.buildingWorldId}) by ${bWorld.authorName}!")
            }
        }

        plugin.logger.info("Finished loading Building Worlds. Total: ${WorldManager.buildingWorlds.size}")
    }

    fun createWorld(config: BuildingWorldConfig) = BuildingWorld(
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
        } catch (ignored: IllegalArgumentException) {
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
}