package dev.slne.surf.building.paper.world

import dev.slne.surf.building.paper.plugin
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
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
    val createdAt: OffsetDateTime
) {
    val currentPlayers = mutableObjectSetOf<UUID>()
    val folder = plugin.server.worldContainer.resolve(worldName)
    val world get() = Bukkit.getWorld(worldUuid)

    @ConfigSerializable
    enum class Status(val displayName: String, val material: Material, val allowBuild: Boolean) {
        UNKNOWN("Unbekannt", Material.LIGHT_GRAY_DYE, false),
        EDITING("In Bearbeitung", Material.YELLOW_DYE, true),
        DONE("Fertiggestellt", Material.LIME_DYE, false),
        PUBLISHED("Veröffentlicht", Material.LIGHT_BLUE_DYE, false),
    }
}