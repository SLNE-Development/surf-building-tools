package dev.slne.surf.buildsystem.util

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.bitmap.common.provider.BitmapProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity

val BUILD_PRIMARY = TextColor.fromHexString("#4FBF6A")!!
val BUILD_SECONDARY = TextColor.fromHexString("#B9DCC0")!!
val BUILD_HIGHLIGHT = TextColor.fromHexString("#C8F03C")!!
val BUILD_USELESS = TextColor.fromHexString("#DCEBDF")!!

fun SurfComponentBuilder.buildPrimary(text: Any, vararg decoration: TextDecoration) =
    text(text.toString(), BUILD_PRIMARY, *decoration)

fun SurfComponentBuilder.buildSecondary(text: Any, vararg decoration: TextDecoration) =
    text(text.toString(), BUILD_SECONDARY, *decoration)

fun SurfComponentBuilder.buildHighlight(text: Any, vararg decoration: TextDecoration) =
    text(text.toString(), BUILD_HIGHLIGHT, *decoration)

fun SurfComponentBuilder.buildUseless(text: Any, vararg decoration: TextDecoration) =
    text(text.toString(), BUILD_USELESS, *decoration)

fun bitmapTitle(title: String, background: TextColor = BUILD_PRIMARY): Component =
    BitmapProvider.translateToComponent(title.toBitmapSafe(), Colors.WHITE, background)

fun SurfComponentBuilder.appendBitmapTitle(title: String, background: TextColor = BUILD_PRIMARY) =
    append(bitmapTitle(title, background))

fun SurfComponentBuilder.appendBuildPrefix() = append {
    appendBitmapTitle("Bau")
    appendSpace()
}

private fun String.toBitmapSafe() = this
    .replace("ä", "ae").replace("Ä", "Ae")
    .replace("ö", "oe").replace("Ö", "Oe")
    .replace("ü", "ue").replace("Ü", "Ue")
    .replace("ß", "ss")
    .replace(" ", "")

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
