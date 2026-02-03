package dev.slne.surf.building.paper.service

import dev.slne.surf.building.paper.world.BuildingWorld
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

val buildingWorldItemsService = BuildingWorldItemsService()

class BuildingWorldItemsService {
    fun savePlayerData(player: Player, buildingWorld: BuildingWorld) {
        val folder = buildingWorld.folder.resolve("playerData")

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, "${player.uniqueId}.yml")
        val config = YamlConfiguration()
        val playerLocation = player.location

        config.set("inventory", player.inventory.contents.toList())
        config.set("location.world", playerLocation.world?.name)
        config.set("location.x", playerLocation.x)
        config.set("location.y", playerLocation.y)
        config.set("location.z", playerLocation.z)
        config.set("location.yaw", playerLocation.yaw)
        config.set("location.pitch", playerLocation.pitch)
        config.save(file)
    }

    fun loadPlayerData(player: Player, buildingWorld: BuildingWorld) {
        val folder = buildingWorld.folder.resolve("playerData")

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, "${player.uniqueId}.yml")

        if (!file.exists()) {
            player.inventory.clear()
            return
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val items = config.getList("inventory") as? List<ItemStack>
        if (items != null) {
            player.inventory.contents = items.toTypedArray()
        }

        val worldName = config.getString("location.world") ?: return
        val world = Bukkit.getWorld(worldName) ?: return

        val x = config.getDouble("location.x")
        val y = config.getDouble("location.y")
        val z = config.getDouble("location.z")
        val yaw = config.getDouble("location.yaw").toFloat()
        val pitch = config.getDouble("location.pitch").toFloat()

        val location = Location(world, x, y, z, yaw, pitch)
        player.teleportAsync(location)
    }
}