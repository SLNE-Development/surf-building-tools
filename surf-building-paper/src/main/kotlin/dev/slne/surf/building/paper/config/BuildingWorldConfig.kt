package dev.slne.surf.building.paper.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

@ConfigSerializable
data class BuildingWorldConfig(
    val buildingWorldName: String,
    val buildingWorldId: String,
    val worldName: String,
    val worldUuid: UUID,
    val authorName: String,
    val authorUuid: UUID,
    val createdAt: Long
)
