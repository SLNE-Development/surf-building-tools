package dev.slne.surf.buildsystem

import io.papermc.paper.datapack.Datapack
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY) { event ->
            val uri = requireNotNull(javaClass.getResource("/space-pack")) {
                "Bundled space-pack datapack is missing"
            }.toURI()

            event.registrar().discoverPack(uri, "space") {
                it.autoEnableOnServerStart(true)
                it.position(true, Datapack.Position.TOP)
            }
        }
    }
}
