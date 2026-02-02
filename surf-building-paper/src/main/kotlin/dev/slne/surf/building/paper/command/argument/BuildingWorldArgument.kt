package dev.slne.surf.building.paper.command.argument

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import org.bukkit.command.CommandSender

class BuildingWorldArgument(nodeName: String) :
    CustomArgument<BuildingWorld, String>(StringArgument(nodeName), { info ->
        buildingWorldService.buildingWorlds.find { it.buildingWorldName == info.input }
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
                buildingWorldService.buildingWorlds.map { it.buildingWorldName }
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