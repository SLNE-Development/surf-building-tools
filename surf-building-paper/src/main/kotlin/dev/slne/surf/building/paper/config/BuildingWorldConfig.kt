package dev.slne.surf.building.paper.config

import dev.slne.surf.building.paper.world.BuildingWorld
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
    var displayItemName: String = "GRASS_BLOCK"
)
