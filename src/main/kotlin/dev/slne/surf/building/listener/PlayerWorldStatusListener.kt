package dev.slne.surf.building.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.building.permission.PermissionRegistry
import dev.slne.surf.building.util.buildingWorld
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

object PlayerWorldStatusListener : Listener {
    private fun canBuildInWorld(event: Cancellable, world: World, player: Player): Boolean {
        val buildingWorld = world.buildingWorld ?: return true

        if (!buildingWorld.status.allowBuild) {
            event.isCancelled = true
            player.sendText {
                appendErrorPrefix()
                error("Bauen ist in dieser Welt deaktiviert.")
            }
            return false
        }

        if (!player.hasPermission(PermissionRegistry.BUILDER) && player.uniqueId !in buildingWorld.members) {
            event.isCancelled = true
            player.sendText {
                appendErrorPrefix()
                error("Du hast keine Berechtigung, in dieser Welt zu bauen.")
            }
            return false
        }

        return true
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        canBuildInWorld(event, event.blockPlaced.world, event.player)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        canBuildInWorld(event, event.block.world, event.player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }
}