package dev.slne.surf.building.config

import dev.slne.surf.building.world.BuildingWorld
import dev.slne.surf.building.world.WarpCategoryConfig
import dev.slne.surf.building.world.WarpConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.OffsetDateTime
import java.util.*

@ConfigSerializable
data class BuildingWorldConfig(
    var buildingWorldName: String = "???",
    var buildingWorldId: String = "????????",
    var worldName: String = "???",
    var worldUuid: UUID = UUID.randomUUID(),
    var authorName: String = "???",
    var authorUuid: UUID = UUID.randomUUID(),
    var status: String = BuildingWorld.Status.UNKNOWN.name,
    var createdAtString: String = OffsetDateTime.MIN.toString(),
    var worldType: String = BuildingWorld.Type.FLAT.name,
    var displayItemName: String = "GRASS_BLOCK",
    var warps: MutableList<WarpConfig> = mutableListOf(),
    var members: MutableList<UUID> = mutableListOf(),
    var categories: MutableList<WarpCategoryConfig> = mutableListOf()
)
