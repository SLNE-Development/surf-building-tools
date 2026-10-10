package dev.slne.surf.buildsystem.lobby

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.buildingConfig
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.world.generator.BuildingWorldGenerator
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kotlinx.coroutines.withContext
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object LobbyService {
    private val lobbyConfig get() = buildingConfig.lobby
    private val lobbyKey
        get() = requireNotNull(NamespacedKey.fromString(lobbyConfig.worldKey)) {
            "Invalid lobby world key '${lobbyConfig.worldKey}'"
        }

    val lobbyWorld: World
        get() = Bukkit.getWorld(lobbyKey) ?: error("Lobby world is not loaded")

    val spawnLocation: Location
        get() = Location(lobbyWorld, lobbyConfig.spawnX, lobbyConfig.spawnY, lobbyConfig.spawnZ)

    suspend fun loadLobbyWorld() = withContext(plugin.globalRegionDispatcher) {
        val world = Bukkit.getWorld(lobbyKey) ?: run {
            plugin.logger.warning("Space dimension '$lobbyKey' is not loaded, creating a plain void lobby world instead. Is the bundled datapack enabled?")

            WorldCreator.ofKey(lobbyKey)
                .generator(BuildingWorldGenerator)
                .generateStructures(false)
                .createWorld()
        } ?: error("Could not create lobby world '$lobbyKey'")

        world.setSpawnLocation(spawnLocation)
        world.setStorm(false)
        world.setGameRule(GameRules.ADVANCE_WEATHER, false)
        world.setGameRule(GameRules.SPAWN_MOBS, false)
        world.setGameRule(GameRules.SPAWN_MONSTERS, false)
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false)
        world.setGameRule(GameRules.SPAWN_PATROLS, false)
        world.setGameRule(GameRules.SPAWN_WARDENS, false)
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)

        val spawn = spawnLocation
        world.setBlockData(
            spawn.blockX,
            spawn.blockY - 1,
            spawn.blockZ,
            BlockType.BARRIER.createBlockData()
        )

        plugin.logger.info("Lobby world '${world.name}' loaded.")
    }

    fun isLobby(world: World) = isLobbyKey(world.key)

    fun isLobbyKey(key: NamespacedKey) = key == lobbyKey

    fun isInLobby(player: Player) = isLobby(player.world)

    fun teleportToLobby(player: Player): CompletableFuture<Boolean> =
        player.teleportAsync(spawnLocation)

    fun prepare(player: Player) {
        player.gameMode = GameMode.ADVENTURE
        player.inventory.clear()
        player.inventory.heldItemSlot = 4
        player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.saturation = 20f
        player.fallDistance = 0f

        if (player.hasMenuAccess()) {
            player.inventory.setItem(4, plugin.menuItem)
            openMenuLater(player)
        } else {
            showNoAccessTitleLater(player)
        }
    }

    fun Player.hasMenuAccess() = hasPermission(PermissionRegistry.COMMAND)

    fun openMenuLater(player: Player, delayTicks: Long = 2) {
        player.scheduler.runDelayed(plugin, {
            if (!player.isOnline || !isInLobby(player)) return@runDelayed
            if (!player.hasMenuAccess()) return@runDelayed
            if (player.openInventory.topInventory.type != InventoryType.CRAFTING) return@runDelayed

            centralMenu.open(player)
        }, null, delayTicks)
    }

    private const val NO_ACCESS_TITLE_INTERVAL_TICKS = 40L
    private val noAccessTitleTasks = ConcurrentHashMap<UUID, ScheduledTask>()

    private val noAccessTitle
        get() = Title.title(
            buildText {
                error("Kein Zugriff")
            },
            buildText {
                error("Du hast keinen Zugriff auf die Bau-Welten.".toSmallCaps())
            },
            Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ofMillis(500))
        )

    fun showNoAccessTitleLater(player: Player, delayTicks: Long = 2) {
        noAccessTitleTasks.remove(player.uniqueId)?.cancel()

        val task = player.scheduler.runAtFixedRate(
            plugin,
            { task ->
                if (!player.isOnline || !isInLobby(player) || player.hasMenuAccess()) {
                    task.cancel()
                    noAccessTitleTasks.remove(player.uniqueId, task)
                    player.clearTitle()
                    return@runAtFixedRate
                }

                player.showTitle(noAccessTitle)
            },
            { noAccessTitleTasks.remove(player.uniqueId) },
            delayTicks,
            NO_ACCESS_TITLE_INTERVAL_TICKS
        )

        task?.let { noAccessTitleTasks[player.uniqueId] = it }
    }

    fun hideNoAccessTitle(player: Player) {
        val task = noAccessTitleTasks.remove(player.uniqueId) ?: return

        task.cancel()
        player.clearTitle()
    }
}
