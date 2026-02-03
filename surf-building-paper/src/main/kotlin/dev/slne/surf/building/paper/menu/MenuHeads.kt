package dev.slne.surf.building.paper.menu

import com.destroystokyo.paper.profile.ProfileProperty
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Material
import java.nio.charset.StandardCharsets
import java.util.*

@Suppress("UnstableApiUsage")
object MenuHeads {
    val CREATE_BUTTON =
        createSkull("http://textures.minecraft.net/texture/69b861aabb316c4ed73b4e5428305782e735565ba2a053912e1efd834fa5a6f")

    private fun createSkull(url: String) = buildItem(Material.PLAYER_HEAD) {
        val json = "{\"SKIN\":{\"url\":\"%s\"}}".format(url);
        val base64 = Base64.getEncoder().encodeToString(json.toByteArray(StandardCharsets.UTF_8))
        setData(
            DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile().addProperty(
                ProfileProperty("textures", base64)
            )
        )
    }
}