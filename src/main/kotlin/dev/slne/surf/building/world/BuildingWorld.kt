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
    val world get() = worldOrNull ?: error("World $worldName ($worldUuid) is not loaded")

    @ConfigSerializable
    enum class Status(
        val displayName: String,
        val material: Material,
        val allowBuild: Boolean,
        val description: String
    ) {
        UNKNOWN(
            "Unbekannt",
            Material.LIGHT_GRAY_CANDLE,
            false,
            "Der Status der Bau-Welt ist unbekannt. Es könnte ein Fehler vorliegen."
        ),
        EDITING(
            "In Bearbeitung",
            Material.YELLOW_CANDLE,
            true,
            "Die Bau-Welt befindet sich in Bearbeitung. Builder/Mitglieder können beitreten und bauen."
        ),
        DONE(
            "Fertiggestellt",
            Material.LIGHT_BLUE_CANDLE,
            false,
            "Die Bau-Welt ist fertiggestellt. Builder/Mitglieder können beitreten, aber nicht mehr bauen."
        ),
        PUBLISHED(
            "Veröffentlicht",
            Material.LIME_CANDLE,
            false,
            "Die Bau-Welt ist veröffentlicht. Builder/Mitglieder können beitreten, aber nicht mehr bauen."
        ),
    }

    enum class Type(val displayName: String) {
        VOID("Leer"), FLAT("Flach");


        fun next() = entries[(ordinal + 1) % entries.size]
        fun previous() = entries[(ordinal - 1 + entries.size) % entries.size]
    }
}