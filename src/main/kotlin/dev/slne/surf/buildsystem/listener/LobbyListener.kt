package dev.slne.surf.buildsystem.listener

import dev.slne.surf.buildsystem.buildingConfig
import dev.slne.surf.buildsystem.lobby.LobbyService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

object LobbyListener : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        if (event.reason != InventoryCloseEvent.Reason.PLAYER) return
        if (!LobbyService.isInLobby(player)) return

        LobbyService.openMenuLater(player)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!LobbyService.isLobby(event.to.world)) return
        if (event.to.y >= buildingConfig.lobby.minimumY) return

        LobbyService.teleportToLobby(event.player)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        if (!LobbyService.isLobby(event.entity.world)) return

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (!LobbyService.isLobby(event.entity.world)) return

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (LobbyService.isLobby(event.block.world)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        if (LobbyService.isLobby(event.block.world)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onInteract(event: PlayerInteractEvent) {
        if (!LobbyService.isLobby(event.player.world)) return
        if (event.clickedBlock == null) return

        event.isCancelled = true
    }
}
