package dev.slne.surf.building.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument

fun buildingWorldCommand() = commandTree("buildingworld") {
    literalArgument("create") {
        
    }
}