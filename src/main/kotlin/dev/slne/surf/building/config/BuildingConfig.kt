package dev.slne.surf.building.config

import org.bukkit.Bukkit
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BuildingConfig(
    val lobbyWorldName: String = Bukkit.getWorlds().first().name
)
