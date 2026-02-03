package dev.slne.surf.building.paper.util

import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

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
    ) // https://minecraft.fandom.com/wiki/Key_codes