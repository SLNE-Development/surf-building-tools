package dev.slne.surf.building.world

import org.bukkit.Material
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class WarpCategoryConfig(
    var name: String = "",
    var displayItemName: String = "CHEST",
    var warps: MutableList<WarpConfig> = mutableListOf(),
    var subCategories: MutableList<WarpCategoryConfig> = mutableListOf()
)

data class WarpCategory(
    val name: String,
    val displayItem: Material = Material.CHEST,
    val warps: List<Warp> = emptyList(),
    val subCategories: List<WarpCategory> = emptyList()
) {
    companion object {
        fun empty() = WarpCategory("#empty", Material.BARRIER, emptyList(), emptyList())
    }
}

