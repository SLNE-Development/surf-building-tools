package dev.slne.surf.building.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.building.paper.config.BuildingConfigHolder
import dev.slne.surf.building.paper.database.table.BuildingWorldsTable
import dev.slne.surf.building.paper.listener.ConnectionListener
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.surfapi.bukkit.api.event.register
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onLoad() {
        DatabaseApi.create(dataPath)
    }

    override suspend fun onEnableAsync() {
        ConnectionListener.register()

        suspendTransaction {
            SchemaUtils.create(BuildingWorldsTable)
        }
    }
}

val buildingConfigHolder = BuildingConfigHolder()
val buildingConfig get() = buildingConfigHolder.config