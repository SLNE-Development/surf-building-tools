package dev.slne.surf.building.paper.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

@ConfigSerializable
data class BuildingWorldConfig(
    var buildingWorldName: String = "???",
    var buildingWorldId: String = "????????",
    var worldName: String = "???",
    var worldUuid: UUID = UUID.randomUUID(),
    var authorName: String = "???",
    var authorUuid: UUID = UUID.randomUUID(),
    var createdAt: Long = System.currentTimeMillis()
)
