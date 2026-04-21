package dev.slne.surf.building.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.building.util.buildingWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object PlayerWorldStatusListener : Listener {
    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.blockPlaced.world.buildingWorld?.status?.allowBuild == false) {
            event.isCancelled = true

            event.player.sendText {
                appendErrorPrefix()
                error("Bauen ist in dieser Welt deaktiviert.")
            }
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.block.world.buildingWorld?.status?.allowBuild == false) {
            event.isCancelled = true

            event.player.sendText {
                appendErrorPrefix()
                error("Bauen ist in dieser Welt deaktiviert.")
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.world.buildingWorld?.status?.allowBuild == false) {
            event.isCancelled = true

            event.player.sendText {
                appendErrorPrefix()
                error("Bauen ist in dieser Welt deaktiviert.")
            }
        }
    }

    @EventHandler
    fun onInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        if (event.player.world.buildingWorld?.status?.allowBuild == false) {
            event.isCancelled = true

            event.player.sendText {
                appendErrorPrefix()
                error("Bauen ist in dieser Welt deaktiviert.")
            }
        }
    }
}