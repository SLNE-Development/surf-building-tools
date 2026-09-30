package dev.slne.surf.buildsystem.listener

import dev.slne.surf.api.paper.event.cancel
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
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
            centralMenu.open(event.player)
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

    @EventHandler
    fun onDrag(event: InventoryDragEvent) {
        if (event.oldCursor == plugin.menuItem || event.newItems.any { it.value.isSimilar(plugin.menuItem) }) {
            event.cancel()
        }
    }
}