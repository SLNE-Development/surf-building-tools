package dev.slne.surf.building.command.argument

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import org.bukkit.command.CommandSender

class BuildingWorldArgument(nodeName: String) :
    CustomArgument<BuildingWorld, String>(StringArgument(nodeName), { info ->
        WorldManager.buildingWorlds.find { it.buildingWorldName == info.input }
            ?: throw CustomArgumentException.fromAdventureComponent {
                buildText {
                    appendErrorPrefix()
                    error("Die Bau-Welt wurde nicht gefunden!")
                }
            }

    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection<CommandSender> {
                WorldManager.buildingWorlds.map { it.buildingWorldName }
            }
        )
    }
}

inline fun Argument<*>.buildingWorldArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    BuildingWorldArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandTree.buildingWorldArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    BuildingWorldArgument(nodeName).setOptional(optional).apply(block)
)