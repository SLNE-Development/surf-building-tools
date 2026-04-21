package dev.slne.surf.building.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.building.util.isBuildingWorld
import dev.slne.surf.building.util.teleportToHighestSpawn
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
                    appendInfoPrefix()
                    info("Du wurdest zum Spawn teleportiert, da du aus der Welt gefallen bist.")
                }
            }
        }
    }
}