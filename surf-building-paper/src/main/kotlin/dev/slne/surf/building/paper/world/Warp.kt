package dev.slne.surf.building.paper.world

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class WarpConfig(
    var name: String = "Warp",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var pitch: Float = 0.0f,
    var yaw: Float = 0.0f,
    var displayItemName: String = "ENDER_PEARL"
)

data class Warp(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val displayItem: Material
)
