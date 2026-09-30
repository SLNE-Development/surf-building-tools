package dev.slne.surf.buildsystem.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.buildsystem.plugin

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