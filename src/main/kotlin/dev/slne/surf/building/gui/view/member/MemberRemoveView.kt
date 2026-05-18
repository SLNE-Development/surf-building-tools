package dev.slne.surf.building.gui.view.member

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.outlineItem
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.building.gui.view.backItem
import dev.slne.surf.building.gui.view.createMemberItem
import dev.slne.surf.building.gui.view.playGeneralClickSound
import dev.slne.surf.building.service.WorldManager
import dev.slne.surf.building.world.BuildingWorld
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material
import java.util.UUID

object MemberRemoveView : View() {
    private val worldHolder = initialState<BuildingWorld>("world")
    private val memberUuidHolder = initialState<UUID>("memberUuid")

    override fun onInit(config: ViewConfigBuilder) {
        config.size(3).layout(
            "OOOOOOOOO",
            "OOAOIOCOO",
            "OOOOBOOOO"
        ).titleBuilder {
            primary("Mitglied entfernen")
        }.cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val memberUuid = memberUuidHolder.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createMemberItem(memberUuid))
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(MembersView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
        render.layoutSlot('A', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(MembersView::class.java, mutableMapOf("world" to worldHolder.get(click)))
        }
        render.layoutSlot('C', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val world = worldHolder.get(click)
            val uuid = memberUuidHolder.get(click)
            val updatedWorld = WorldManager.removeMember(world, uuid)
            click.openForPlayer(MembersView::class.java, mutableMapOf("world" to updatedWorld))
        }
    }

    private val confirmItem = buildItem(Material.LIME_STAINED_GLASS_PANE) {
        displayName { success("\u2714 Mitglied entfernen") }
        buildLore {
            emptyLine()
            line { spacer("\u00bb"); appendSpace(); white("Klicke hier, um das Mitglied zu entfernen.") }
            emptyLine()
            line { error("\u2718 Diese Aktion kann nicht r\u00fckg\u00e4ngig gemacht werden!") }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName { error("\u2718 Abbrechen") }
        buildLore {
            emptyLine()
            line { spacer("\u00bb"); appendSpace(); white("Klicke hier, um abzubrechen.") }
        }
    }
}
