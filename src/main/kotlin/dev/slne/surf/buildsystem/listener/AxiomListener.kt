package dev.slne.surf.buildsystem.listener

import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.plugin
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Blocks Axiom modifications in worlds the player is not allowed to build in.
 *
 * AxiomPaper is an optional dependency without a published API artifact, so its events
 * are resolved through the AxiomPaper class loader and registered reflectively.
 */
object AxiomListener : Listener {
    private const val AXIOM_MAIN_CLASS = "com.moulberry.axiom.AxiomPaper"
    private const val AXIOM_EVENT_PACKAGE = "com.moulberry.axiom.event"

    private val axiomEvents = mapOf(
        "AxiomModifyWorldEvent" to "getWorld",
        "AxiomSpawnEntityEvent" to "getEntity",
        "AxiomManipulateEntityEvent" to "getEntity",
        "AxiomRemoveEntityEvent" to "getEntity",
    )

    fun registerIfPresent() {
        val axiom = Bukkit.getPluginManager().plugins
            .firstOrNull { it.javaClass.name == AXIOM_MAIN_CLASS }
            ?: return

        val classLoader = axiom.javaClass.classLoader
        axiomEvents.forEach { (eventName, targetGetter) ->
            registerAxiomEvent(classLoader, eventName, targetGetter)
        }

        plugin.logger.info("Hooked into ${axiom.name} to restrict Axiom building.")
    }

    private fun registerAxiomEvent(classLoader: ClassLoader, eventName: String, targetGetter: String) {
        val eventClass = runCatching { Class.forName("$AXIOM_EVENT_PACKAGE.$eventName", true, classLoader) }
            .getOrNull()
            ?.takeIf { Event::class.java.isAssignableFrom(it) && Cancellable::class.java.isAssignableFrom(it) }
            ?.asSubclass(Event::class.java)
            ?: run {
                plugin.logger.warning("Axiom event '$eventName' not found, Axiom building may not be fully restricted.")
                return
            }

        val getPlayer = eventClass.getMethod("getPlayer")
        val getTarget = eventClass.getMethod(targetGetter)

        Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.LOW, { _, event ->
            if (!eventClass.isInstance(event)) return@registerEvent

            val player = getPlayer.invoke(event) as? Player ?: return@registerEvent
            val world = when (val target = getTarget.invoke(event)) {
                is World -> target
                is Entity -> target.world
                else -> return@registerEvent
            }

            val reason = axiomDeniedReason(world, player) ?: return@registerEvent

            (event as Cancellable).isCancelled = true
            PlayerWorldStatusListener.sendDenied(player, reason)
        }, plugin, true)
    }

    private fun axiomDeniedReason(world: World, player: Player): String? {
        if (LobbyService.isLobby(world)) {
            return "Du kannst in der Lobby nicht bauen."
        }

        return PlayerWorldStatusListener.buildDeniedReason(world, player)
    }
}
