package dev.slne.surf.building.world

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.building.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.OffsetDateTime
import java.util.*

data class BuildingWorld(
    val buildingWorldName: String,
    val buildingWorldId: String,
    val worldName: String,
    val worldUuid: UUID,
    val authorName: String,
    val authorUuid: UUID,
    val status: Status,
    val createdAt: OffsetDateTime,
    val type: Type = Type.FLAT,
    val displayItem: Material = Material.GRASS_BLOCK,
    val warps: List<Warp> = emptyList(),
    val members: List<UUID> = emptyList()
) {
    val currentPlayers = mutableObjectSetOf<UUID>()
    val folder = plugin.server.worldContainer.resolve(worldName)
    val worldOrNull get() = Bukkit.getWorld(worldUuid)
    val world get() = worldOrNull ?: error("Die Welt mit der UUID $worldUuid existiert nicht.")

    @ConfigSerializable
    enum class Status(val displayName: String, val material: Material, val allowBuild: Boolean) {
        UNKNOWN("Unbekannt", Material.LIGHT_GRAY_CANDLE, false),
        EDITING("In Bearbeitung", Material.YELLOW_CANDLE, true),
        DONE("Fertiggestellt", Material.LIGHT_BLUE_CANDLE, false),
        PUBLISHED("Veröffentlicht", Material.LIME_CANDLE, false),
    }

    enum class Type(val displayName: String) {
        VOID("Leer"), FLAT("Flach");


        fun next() = entries[(ordinal + 1) % entries.size]
        fun previous() = entries[(ordinal - 1 + entries.size) % entries.size]
    }
}