package dev.slne.surf.building.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.building.paper.command.buildingWorldCommand
import dev.slne.surf.building.paper.config.BuildingConfigHolder
import dev.slne.surf.building.paper.listener.ConnectionListener
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        ConnectionListener.register()

        buildingWorldCommand()

        buildingWorldService.cacheAllBuildingWorlds()
    }
}

val buildingConfigHolder = BuildingConfigHolder()
val buildingConfig get() = buildingConfigHolder.config