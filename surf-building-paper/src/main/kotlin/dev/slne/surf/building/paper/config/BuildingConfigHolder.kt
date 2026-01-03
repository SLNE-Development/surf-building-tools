package dev.slne.surf.building.paper.config

import dev.slne.surf.building.paper.plugin
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

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