package dev.slne.surf.building.paper.config

import org.bukkit.Bukkit
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BuildingConfig(
    val lobbyWorldName: String = Bukkit.getWorlds().first().name
)
