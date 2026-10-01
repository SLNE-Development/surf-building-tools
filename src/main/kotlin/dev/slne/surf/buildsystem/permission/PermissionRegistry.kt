package dev.slne.surf.buildsystem.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.buildsystem"

    val WORLD_DELETE = create("$BASE.world.delete")
    val COMMAND = create("$BASE.command")
    val COMMAND_LOBBY = create("$COMMAND.lobby")
    val COMMAND_IMPORT = create("$COMMAND.import")

    val BUILDER = create("$BASE.builder")
}