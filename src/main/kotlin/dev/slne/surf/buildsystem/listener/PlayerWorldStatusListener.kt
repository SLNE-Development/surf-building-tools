package dev.slne.surf.buildsystem.listener

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.util.buildingWorld
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerWorldStatusListener : Listener {
    private const val MESSAGE_COOLDOWN_MILLIS = 1000L
    private val lastMessage = ConcurrentHashMap<UUID, Long>()

    private fun canBuildInWorld(
        event: Cancellable,
        world: World,
        player: Player,
        notify: Boolean = true
    ): Boolean {
        val buildingWorld = world.buildingWorld ?: return true

        if (!buildingWorld.status.allowBuild) {
            event.isCancelled = true
            if (notify) sendDenied(player, "Bauen ist in dieser Welt deaktiviert.")
            return false
        }

        if (!player.hasPermission(PermissionRegistry.BUILDER) && player.uniqueId !in buildingWorld.members) {
            event.isCancelled = true
            if (notify) sendDenied(player, "Du hast keine Berechtigung, in dieser Welt zu bauen.")
            return false
        }

        return true
    }

    private fun sendDenied(player: Player, message: String) {
        val now = System.currentTimeMillis()
        val last = lastMessage[player.uniqueId]
        if (last != null && now - last < MESSAGE_COOLDOWN_MILLIS) return
        lastMessage[player.uniqueId] = now

        player.sendText {
            appendErrorPrefix()
            error(message)
        }
    }

    private fun Entity.responsiblePlayer(): Player? = when (this) {
        is Player -> this
        is Projectile -> shooter as? Player
        else -> null
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
        when (event.action) {
            Action.LEFT_CLICK_AIR, Action.RIGHT_CLICK_AIR -> return
            Action.PHYSICAL -> canBuildInWorld(event, event.player.world, event.player, notify = false)
            else -> canBuildInWorld(event, event.player.world, event.player)
        }
    }

    @EventHandler
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onBucketFill(event: PlayerBucketFillEvent) {
        canBuildInWorld(event, event.player.world, event.player)
    }

    @EventHandler
    fun onEntityPlace(event: EntityPlaceEvent) {
        val player = event.player ?: return
        canBuildInWorld(event, event.entity.world, player)
    }

    @EventHandler
    fun onHangingPlace(event: HangingPlaceEvent) {
        val player = event.player ?: return
        canBuildInWorld(event, event.entity.world, player)
    }

    @EventHandler
    fun onHangingBreak(event: HangingBreakByEntityEvent) {
        val player = event.remover.responsiblePlayer() ?: return
        canBuildInWorld(event, event.entity.world, player)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.entity is Player) return
        val player = event.damager.responsiblePlayer() ?: return
        canBuildInWorld(event, event.entity.world, player)
    }

    @EventHandler
    fun onVehicleDamage(event: VehicleDamageEvent) {
        val player = event.attacker?.responsiblePlayer() ?: return
        canBuildInWorld(event, event.vehicle.world, player)
    }

    @EventHandler
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker?.responsiblePlayer() ?: return
        canBuildInWorld(event, event.vehicle.world, player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        lastMessage.remove(event.player.uniqueId)
    }
}
