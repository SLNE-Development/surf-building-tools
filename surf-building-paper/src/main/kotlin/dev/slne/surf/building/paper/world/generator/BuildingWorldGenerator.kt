package dev.slne.surf.building.paper.world.generator

import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.*

class BuildingWorldGenerator : ChunkGenerator() {
    override fun generateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in worldInfo.minHeight..worldInfo.maxHeight) {
                    chunkData.setBlock(x, y, z, Material.AIR)
                }
            }
        }
    }

    override fun shouldGenerateCaves() = false

    override fun shouldGenerateDecorations() = false

    override fun shouldGenerateMobs() = false

    override fun shouldGenerateNoise() = false

    override fun shouldGenerateStructures() = false

    override fun shouldGenerateSurface() = false

    override fun shouldGenerateCaves(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false

    override fun shouldGenerateDecorations(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false

    override fun shouldGenerateMobs(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false

    override fun shouldGenerateNoise(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false

    override fun shouldGenerateStructures(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false

    override fun shouldGenerateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ) = false
}