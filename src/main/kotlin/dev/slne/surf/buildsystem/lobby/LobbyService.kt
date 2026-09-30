package dev.slne.surf.buildsystem.lobby

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.buildsystem.buildingConfig
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.plugin
import dev.slne.surf.buildsystem.world.generator.BuildingWorldGenerator
import kotlinx.coroutines.withContext
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import java.util.concurrent.CompletableFuture

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

    fun isLobby(world: World) = world.key == lobbyKey

    fun isInLobby(player: Player) = isLobby(player.world)

    fun teleportToLobby(player: Player): CompletableFuture<Boolean> =
        player.teleportAsync(spawnLocation)

    fun prepare(player: Player) {
        player.gameMode = GameMode.ADVENTURE
        player.inventory.clear()
        player.inventory.heldItemSlot = 4
        player.inventory.setItem(4, plugin.menuItem)
        player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.saturation = 20f
        player.fallDistance = 0f

        openMenuLater(player)
    }

    fun openMenuLater(player: Player, delayTicks: Long = 2) {
        player.scheduler.runDelayed(plugin, {
            if (!player.isOnline || !isInLobby(player)) return@runDelayed
            if (player.openInventory.topInventory.type != InventoryType.CRAFTING) return@runDelayed

            centralMenu.open(player)
        }, null, delayTicks)
    }
}
