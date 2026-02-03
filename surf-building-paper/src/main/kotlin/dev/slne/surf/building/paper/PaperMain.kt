package dev.slne.surf.building.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.building.paper.command.buildingWorldCommand
import dev.slne.surf.building.paper.config.BuildingConfigHolder
import dev.slne.surf.building.paper.listener.*
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.primaryColored
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        ConnectionListener.register()
        PlayerWorldListener.register()
        MenuItemListener.register()
        PlayerMoveListener.register()
        PlayerWorldStatusListener.register()

        buildingWorldCommand()

        buildingWorldService.cacheAllBuildingWorlds()
    }

    val menuItem = buildItem(Material.COMPASS) {
        displayName {
            primaryColored("Bau Welten Menu")
        }
    }
}

val buildingConfigHolder = BuildingConfigHolder()
val buildingConfig get() = buildingConfigHolder.config