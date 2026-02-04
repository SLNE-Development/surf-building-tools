package dev.slne.surf.building.paper.util

import dev.slne.surf.building.paper.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Utility functions for managing bukkit.yml configuration
 */

/**
 * Adds a world generator entry to bukkit.yml
 */
fun addGeneratorToBukkitYml(worldName: String, generatorName: String) {
    val bukkitYmlFile = File(plugin.server.worldContainer.parentFile, "bukkit.yml")
    
    // Create bukkit.yml if it doesn't exist
    if (!bukkitYmlFile.exists()) {
        bukkitYmlFile.createNewFile()
    }
    
    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)
    
    // Set the generator for the world
    config.set("worlds.$worldName.generator", generatorName)
    
    // Save the configuration
    config.save(bukkitYmlFile)
    
    plugin.logger.info("Added generator '$generatorName' for world '$worldName' to bukkit.yml")
}

/**
 * Removes a world generator entry from bukkit.yml
 */
fun removeGeneratorFromBukkitYml(worldName: String) {
    val bukkitYmlFile = File(plugin.server.worldContainer.parentFile, "bukkit.yml")
    
    // If bukkit.yml doesn't exist, nothing to remove
    if (!bukkitYmlFile.exists()) {
        return
    }
    
    val config = YamlConfiguration.loadConfiguration(bukkitYmlFile)
    
    // Check if the world exists in the configuration
    if (config.contains("worlds.$worldName")) {
        // Remove the entire world section
        config.set("worlds.$worldName", null)
        
        // If worlds section is now empty, remove it entirely
        if (config.getConfigurationSection("worlds")?.getKeys(false)?.isEmpty() == true) {
            config.set("worlds", null)
        }
        
        // Save the configuration
        config.save(bukkitYmlFile)
        
        plugin.logger.info("Removed generator for world '$worldName' from bukkit.yml")
    }
}
