package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.util.isBuildingWorld
import dev.slne.surf.building.paper.util.teleportToHighestSpawn
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object PlayerMoveListener : Listener {
    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!event.to.world.isBuildingWorld()) {
            return
        }

        if (event.to.y < -70) {
            event.to.world.teleportToHighestSpawn(event.player).thenRun {
                event.player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest zum Spawn teleportiert, da du aus der Welt gefallen bist.")
                }
            }
        }
    }
}