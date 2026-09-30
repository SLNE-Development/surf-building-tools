package dev.slne.surf.buildsystem.gui.view

import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.buildsystem.util.translatable
import me.devnatan.inventoryframework.context.SlotClickContext
import org.bukkit.Material

typealias MaterialSelectCallback = (SlotClickContext, Material) -> Unit
typealias MaterialSelectBack = (SlotClickContext) -> Unit

val materialSelectView: AbstractSurfView = paginatedSurfView("Item auswählen") {
    val onSelectState = initialState<MaterialSelectCallback>("onSelect")
    val onBackState = initialState<MaterialSelectBack>("onBack")

    settings {
        paginationViewRows(PaginationViewRows.FIVE)
        navigateBackOnOutsideClick(false)
    }

    layoutTarget('I')

    pagination<Material> {
        source(validDisplayMaterials)

        elementFactory { _, builder, _, material ->
            builder.withItem(buildItem(material) {
                displayName {
                    translatable(material.translationKey())
                }

                buildLore {
                    emptyLine()
                    hint("Klicke, um dieses Item auszuwählen.")
                }
            }).onClick { click ->
                click.playGeneralClickSound()
                onSelectState[click](click, material)
            }
        }
    }

    onFirstRender {
        slot(6, 1, backItem).onClick { click ->
            click.playGeneralClickSound()
            onBackState[click](click)
        }
    }
}
