package dev.slne.surf.building.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.building"
    val COMMAND = create("$BASE.command")
    val COMMAND_LOBBY = create("$COMMAND.lobby")

    val BUILDER = create("$BASE.builder")
}