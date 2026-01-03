package dev.slne.surf.building.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.building"
    val COMMAND = create("$BASE.command")
}