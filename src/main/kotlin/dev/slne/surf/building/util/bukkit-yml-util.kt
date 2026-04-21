package dev.slne.surf.building.util

import dev.slne.surf.building.plugin
import dev.slne.surf.building.world.generator.BuildingWorldGenerator
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

private val GENERATOR_NAME = "${plugin.name}:${BuildingWorldGenerator::class.java.simpleName}"

fun addGeneratorToBukkitYml(worldName: String) {
    val worldContainer = plugin.server.worldContainer
    val bukkitYmlFile = worldContainer.parentFile?.let { File(it, "bukkit.yml") }
        ?: File(worldContainer.absolutePath, "../bukkit.yml").canonicalFile

    if (!bukkitYmlFile.exists()) {
        bukkitYmlFile.createNewFile()
    }

    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)

    config.set("worlds.$worldName.generator", GENERATOR_NAME)
    config.save(bukkitYmlFile)
}

fun removeGeneratorFromBukkitYml(worldName: String) {
    val worldContainer = plugin.server.worldContainer
    val bukkitYmlFile = worldContainer.parentFile?.let { File(it, "bukkit.yml") }
        ?: File(worldContainer.absolutePath, "../bukkit.yml").canonicalFile

    if (!bukkitYmlFile.exists()) {
        return
    }

    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)

    if (config.contains("worlds.$worldName")) {
        config.set("worlds.$worldName", null)

        if (config.getConfigurationSection("worlds")?.getKeys(false)?.isEmpty() == true) {
            config.set("worlds", null)
        }

        config.save(bukkitYmlFile)
    }
}
