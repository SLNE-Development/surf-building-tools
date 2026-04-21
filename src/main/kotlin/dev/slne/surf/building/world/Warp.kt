package dev.slne.surf.building.world

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class WarpConfig(
    var name: String = "???",
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var pitch: Float = 0f,
    var yaw: Float = 0f,
    var displayItemName: String = "COMPASS"
)

data class Warp(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
    val displayItem: Material
) {
    fun location(world: World) = Location(world, x, y, z, yaw, pitch)
}
