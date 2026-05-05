package dev.slne.surf.building

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.building.command.buildingWorldCommand
import dev.slne.surf.building.config.BuildingConfigHolder
import dev.slne.surf.building.gui.view.CentralMenu
import dev.slne.surf.building.gui.view.warp.WarpView
import dev.slne.surf.building.gui.view.warp.WarpsView
import dev.slne.surf.building.gui.view.warp.WarpCreateView
import dev.slne.surf.building.gui.view.warp.WarpEditView
import dev.slne.surf.building.gui.view.warp.WarpDeleteView
import dev.slne.surf.building.gui.view.warp.WarpEditItemView
import dev.slne.surf.building.gui.view.world.*
import dev.slne.surf.building.listener.*
import dev.slne.surf.building.service.WorldConfigManager
import dev.slne.surf.building.util.primaryColored
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        CentralMenu.register()
        WorldView.register()
        WarpsView.register()
        WarpView.register()
        WarpCreateView.register()
        WarpEditView.register()
        WarpDeleteView.register()
        WarpEditItemView.register()
        WorldCreateView.register()
        WorldCreateItemView.register()
        WorldDeleteView.register()
        WorldEditView.register()
        WorldEditItemView.register()
    }

    override suspend fun onEnableAsync() {
        ConnectionListener.register()
        PlayerWorldListener.register()
        MenuItemListener.register()
        PlayerMoveListener.register()
        PlayerWorldStatusListener.register()

        buildingWorldCommand()

        WorldConfigManager.cacheAllBuildingWorlds()
    }

    val menuItem = buildItem(Material.COMPASS) {
        displayName {
            primaryColored("Bau Welten Menu")
        }
    }
}

val buildingConfigHolder = BuildingConfigHolder()
val buildingConfig get() = buildingConfigHolder.config