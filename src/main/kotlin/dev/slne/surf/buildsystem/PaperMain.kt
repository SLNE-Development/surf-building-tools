package dev.slne.surf.buildsystem

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.buildsystem.command.buildingWorldCommand
import dev.slne.surf.buildsystem.command.lobbyCommand
import dev.slne.surf.buildsystem.command.worldCommands
import dev.slne.surf.buildsystem.config.BuildingConfigHolder
import dev.slne.surf.buildsystem.gui.view.centralMenu
import dev.slne.surf.buildsystem.gui.view.hint
import dev.slne.surf.buildsystem.gui.view.materialSelectView
import dev.slne.surf.buildsystem.gui.view.member.memberRemoveView
import dev.slne.surf.buildsystem.gui.view.member.membersView
import dev.slne.surf.buildsystem.gui.view.warp.warpDeleteView
import dev.slne.surf.buildsystem.gui.view.warp.warpEditorView
import dev.slne.surf.buildsystem.gui.view.warp.warpView
import dev.slne.surf.buildsystem.gui.view.warp.warpsView
import dev.slne.surf.buildsystem.gui.view.world.worldCreateView
import dev.slne.surf.buildsystem.gui.view.world.worldDeleteView
import dev.slne.surf.buildsystem.gui.view.world.worldEditView
import dev.slne.surf.buildsystem.gui.view.world.worldView
import dev.slne.surf.buildsystem.listener.*
import dev.slne.surf.buildsystem.lobby.LobbyService
import dev.slne.surf.buildsystem.service.WorldConfigManager
import dev.slne.surf.buildsystem.util.buildPrimary
import dev.slne.surf.buildsystem.util.migrateGeneratorsInBukkitYml
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        centralMenu.register()
        materialSelectView.register()
        worldView.register()
        worldCreateView.register()
        worldEditView.register()
        worldDeleteView.register()
        warpsView.register()
        warpView.register()
        warpEditorView.register()
        warpDeleteView.register()
        membersView.register()
        memberRemoveView.register()
    }

    override suspend fun onEnableAsync() {
        migrateGeneratorsInBukkitYml()
        LobbyService.loadLobbyWorld()

        ConnectionListener.register()
        PlayerWorldListener.register()
        MenuItemListener.register()
        PlayerMoveListener.register()
        PlayerWorldStatusListener.register()
        LobbyListener.register()

        buildingWorldCommand()
        lobbyCommand()
        worldCommands()

        WorldConfigManager.cacheAllBuildingWorlds()
    }

    val menuItem = ViewIcon(ViewIconType.MENU, ViewIconColor.GREEN).build {
        displayName {
            buildPrimary("Bau-Welten Menü")
        }

        buildLore {
            emptyLine()
            hint("Rechtsklick, um das Menü zu öffnen.")
        }
    }
}

val buildingConfigHolder by lazy { BuildingConfigHolder() }
val buildingConfig get() = buildingConfigHolder.config
