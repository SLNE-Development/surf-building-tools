package dev.slne.surf.building.paper.listener

import dev.slne.surf.building.paper.menu.showBuildingWorldMenu
import dev.slne.surf.building.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.event.cancel
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

object MenuItemListener : Listener {
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.item == null) {
            return
        }

        if (event.item == plugin.menuItem) {
            showBuildingWorldMenu(event.player)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.currentItem == null) {
            return
        }

        if (event.currentItem == plugin.menuItem) {
            event.cancel()
        }
    }

    @EventHandler
    fun onOffhand(event: PlayerSwapHandItemsEvent) {
        if (event.mainHandItem == plugin.menuItem) {
            event.cancel()
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (event.itemDrop.itemStack == plugin.menuItem) {
            event.cancel()
        }
    }
}