package dev.slne.surf.buildsystem.world

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.mutableObjectSetOf
import org.bukkit.Bukkit
import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File
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
    val members: List<UUID> = emptyList(),
    val folder: File
) {
    val currentPlayers = mutableObjectSetOf<UUID>()
    val worldOrNull get() = Bukkit.getWorld(worldUuid)
    val world get() = worldOrNull ?: error("World $worldName ($worldUuid) is not loaded")

    @ConfigSerializable
    enum class Status(
        val displayName: String,
        val material: Material,
        val allowBuild: Boolean,
        val icon: SurfComponentBuilder.() -> Unit,
        val description: String
    ) {
        EDITING(
            "In Bearbeitung",
            Material.YELLOW_CANDLE,
            true,
            {
                success("🖋")
            },
            "Die Bau-Welt befindet sich in Bearbeitung. Builder/Mitglieder können beitreten und bauen."
        ),
        DONE(
            "Fertiggestellt",
            Material.LIGHT_BLUE_CANDLE,
            false,
            {
                success("🕛")
            },
            "Die Bau-Welt ist fertiggestellt. Builder/Mitglieder können beitreten, aber nicht mehr bauen."
        ),
        PUBLISHED(
            "Veröffentlicht",
            Material.LIME_CANDLE,
            false,
            {
                success("✔")
            },
            "Die Bau-Welt ist veröffentlicht. Builder/Mitglieder können beitreten, aber nicht mehr bauen."
        );

        fun next() = entries[(ordinal + 1) % entries.size]
        fun previous() = entries[(ordinal - 1 + entries.size) % entries.size]
    }

    enum class Type(val displayName: String) {
        VOID("Leer"), FLAT("Flach");


        fun next() = entries[(ordinal + 1) % entries.size]
        fun previous() = entries[(ordinal - 1 + entries.size) % entries.size]
    }
}