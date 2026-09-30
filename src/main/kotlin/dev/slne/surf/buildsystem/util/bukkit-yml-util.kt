package dev.slne.surf.buildsystem.util

import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.world.generator.BuildingWorldGenerator
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

private val GENERATOR_NAME = "${plugin.name}:${BuildingWorldGenerator::class.java.simpleName}"

private val LEGACY_PLUGIN_NAMES = listOf("surf-building-tools")

private fun bukkitYmlFile(): File {
    val worldContainer = plugin.server.worldContainer
    return worldContainer.parentFile?.let { File(it, "bukkit.yml") }
        ?: File(worldContainer.absolutePath, "../bukkit.yml").canonicalFile
}

fun migrateGeneratorsInBukkitYml() {
    val bukkitYmlFile = bukkitYmlFile()
    if (!bukkitYmlFile.exists()) return

    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)
    val worlds = config.getConfigurationSection("worlds") ?: return
    var migrated = 0

    for (worldName in worlds.getKeys(false)) {
        val generator = worlds.getString("$worldName.generator") ?: continue
        val pluginName = generator.substringBefore(':')

        if (pluginName in LEGACY_PLUGIN_NAMES) {
            worlds.set("$worldName.generator", GENERATOR_NAME)
            migrated++
        }
    }

    if (migrated > 0) {
        config.save(bukkitYmlFile)
        plugin.logger.info("Migrated $migrated world generator(s) in bukkit.yml to '$GENERATOR_NAME'.")
    }
}

fun addGeneratorToBukkitYml(worldName: String) {
    val bukkitYmlFile = bukkitYmlFile()

    if (!bukkitYmlFile.exists()) {
        bukkitYmlFile.createNewFile()
    }

    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)

    config.set("worlds.$worldName.generator", GENERATOR_NAME)
    config.save(bukkitYmlFile)
}

fun removeGeneratorFromBukkitYml(worldName: String) {
    val bukkitYmlFile = bukkitYmlFile()

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
