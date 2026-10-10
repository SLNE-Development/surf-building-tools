package dev.slne.surf.buildsystem.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.buildsystem.util.buildingWorld
import dev.slne.surf.buildsystem.util.canAccessBuildingWorld
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

object BuildingWorldAccessListener : Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        val toWorld = event.to.world
        if (event.from.world == toWorld) return

        val buildingWorld = toWorld.buildingWorld ?: return
        if (event.player.canAccessBuildingWorld(buildingWorld)) return

        event.isCancelled = true
        sendNoAccess(event.player)
    }

    fun sendNoAccess(player: Player) = player.sendText {
        appendErrorPrefix()
        error("Du hast keine Berechtigung, diese Bau-Welt zu betreten.")
    }
}
