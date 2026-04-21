package dev.slne.surf.building.util

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity

fun SurfComponentBuilder.infoColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(
        text.toString(),
        TextColor.color(66, 245, 147), *decoration
    )

fun SurfComponentBuilder.primaryColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(
        text.toString(),
        TextColor.color(41, 204, 133), *decoration
    )

fun SurfComponentBuilder.noteColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(
        text.toString(),
        TextColor.color(27, 218, 224), *decoration
    )

fun SurfComponentBuilder.displayKey(key: String) =
    append(
        MiniMessage.miniMessage().deserialize("<key:key.$key>")
    ).color(Colors.WHITE) // https://minecraft.fandom.com/wiki/Key_codes

fun SurfComponentBuilder.translatable(key: String) = append(Component.translatable(key))


fun HumanEntity.playClickSound() {
    this.playSound(true) {
        type(Sound.UI_BUTTON_CLICK)
    }
}