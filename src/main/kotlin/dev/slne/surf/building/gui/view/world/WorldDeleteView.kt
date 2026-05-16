package dev.slne.surf.building.gui.view.world

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.CentralMenu
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createWorldItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.plugin
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object WorldDeleteView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3)
            .titleBuilder {
                primary("Welt löschen")
            }
            .layout(
                "OOOOOOOOO",
                "OOAOIOCOO",
                "OOOOBOOOO"
            ).cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val world = worldHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(WorldView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
        render.layoutSlot('A', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(WorldView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
        render.layoutSlot('C', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val targetWorld = worldHolder.get(click)
            click.openForPlayer(CentralMenu::class.java)
            click.player.sendText {
                appendInfoPrefix()
                info("Die Welt wird gelöscht...")
            }
            plugin.launch {
                val success = WorldManager.deleteBuildingWorld(targetWorld.buildingWorldId)
                if (success) {
                    click.player.sendText {
                        appendSuccessPrefix()
                        success("Die Welt wurde erfolgreich gelöscht!")
                    }
                } else {
                    click.player.sendText {
                        appendErrorPrefix()
                        error("Die Welt konnte nicht gelöscht werden. Bitte versuche es später erneut.")
                    }
                }
            }
        }
        render.layoutSlot('I', createWorldItem(world))
    }

    private val confirmItem = buildItem(Material.LIME_STAINED_GLASS_PANE) {
        displayName {
            success("✔ Welt löschen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke hier, um die Welt zu löschen.")
            }
            emptyLine()
            line {
                error("✘ Diese Aktion kann nicht rückgängig gemacht werden!")
            }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("✘ Abbrechen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()
                white("Klicke hier, um abzubrechen.")
            }
        }
    }
}
