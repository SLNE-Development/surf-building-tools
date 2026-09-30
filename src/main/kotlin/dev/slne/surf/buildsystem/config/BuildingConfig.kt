package dev.slne.surf.buildsystem.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BuildingConfig(
    val lobby: LobbyConfig = LobbyConfig()
)

@ConfigSerializable
data class LobbyConfig(
    val worldKey: String = "surf:build_lobby",
    val spawnX: Double = 0.5,
    val spawnY: Double = 100.0,
    val spawnZ: Double = 0.5,
    val minimumY: Double = 80.0
)
