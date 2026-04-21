package dev.slne.surf.building.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.building.plugin

class BuildingConfigHolder {
    private val configManager: SpongeConfigManager<BuildingConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            BuildingConfig::class.java,
            plugin.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            BuildingConfig::class.java
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}