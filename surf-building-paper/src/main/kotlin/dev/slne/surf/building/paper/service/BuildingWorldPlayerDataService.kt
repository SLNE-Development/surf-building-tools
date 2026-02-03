package dev.slne.surf.building.paper.service

import dev.slne.surf.building.paper.world.BuildingWorld
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

val buildingWorldPlayerDataService = BuildingWorldPlayerDataService()

class BuildingWorldPlayerDataService {
    fun savePlayerData(player: Player, buildingWorld: BuildingWorld) {
        val folder = buildingWorld.folder.resolve("playerData")

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, "${player.uniqueId}.yml")
        val config = YamlConfiguration()

        val inventoryBytes = player.inventory.contents.serializeItemsToBytes()
        val base64 = Base64.getEncoder().encodeToString(inventoryBytes)
        config.set("inventory", base64)
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
        val base64 = config.getString("inventory")
        if (base64 != null) {
            val bytes = Base64.getDecoder().decode(base64)
            player.inventory.contents = ItemStack.deserializeItemsFromBytes(bytes)
        } else {
            player.inventory.clear()
        }
    }

    fun Array<ItemStack?>.serializeItemsToBytes(): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        val dataOutput = java.io.DataOutputStream(outputStream)

        dataOutput.writeByte(1)
        dataOutput.writeInt(this.size)

        for (item in this) {
            if (item == null || item.isEmpty) {
                dataOutput.writeInt(0)
            } else {
                val itemBytes = item.serializeAsBytes()
                dataOutput.writeInt(itemBytes.size)
                dataOutput.write(itemBytes)
            }
        }

        dataOutput.close()
        return outputStream.toByteArray()
    }
}