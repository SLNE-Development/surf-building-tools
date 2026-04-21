package dev.slne.surf.building.listener

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.event.cancel
import dev.slne.surf.building.plugin
import org.bukkit.Sound
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
            event.player.playSound(true) {
                type(Sound.ENTITY_CHICKEN_EGG)
            }
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