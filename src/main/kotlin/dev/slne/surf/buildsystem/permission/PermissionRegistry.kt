package dev.slne.surf.buildsystem.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.buildsystem"

    val WORLD_DELETE = create("$BASE.world.delete")
    val COMMAND = create("$BASE.command")
    val COMMAND_LOBBY = create("$COMMAND.lobby")

    private const val COMMAND_BUILDING_WORLD = "$BASE.command.buildingworld"
    val COMMAND_BUILDING_WORLD_CREATE = create("$COMMAND_BUILDING_WORLD.create")
    val COMMAND_BUILDING_WORLD_JOIN = create("$COMMAND_BUILDING_WORLD.join")
    val COMMAND_BUILDING_WORLD_LOAD = create("$COMMAND_BUILDING_WORLD.load")
    val COMMAND_BUILDING_WORLD_DONE = create("$COMMAND_BUILDING_WORLD.done")
    val COMMAND_BUILDING_WORLD_PUBLISHED = create("$COMMAND_BUILDING_WORLD.published")
    val COMMAND_BUILDING_WORLD_IMPORT = create("$COMMAND_BUILDING_WORLD.import")
    val COMMAND_BUILDING_WORLD_LOBBY = create("$COMMAND_BUILDING_WORLD.lobby")

    val BUILDER = create("$BASE.builder")
}