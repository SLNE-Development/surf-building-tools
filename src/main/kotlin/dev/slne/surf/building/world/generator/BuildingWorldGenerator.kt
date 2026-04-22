package dev.slne.surf.building.world.generator

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import java.util.*

object BuildingWorldGenerator : ChunkGenerator() {
    override fun canSpawn(world: World, x: Int, z: Int) = true
    override fun getFixedSpawnLocation(world: World, random: Random) =
        Location(world, 0.5, 100.0, 0.5)
}