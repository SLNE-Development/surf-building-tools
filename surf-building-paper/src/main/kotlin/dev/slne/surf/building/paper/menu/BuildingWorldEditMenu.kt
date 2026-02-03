package dev.slne.surf.building.paper.menu

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton
import dev.slne.surf.building.paper.service.buildingWorldService
import dev.slne.surf.building.paper.util.infoColored
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.menu
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity

private const val width = 9
private const val height = 5

fun showBuildingWorldEditMenu(player: HumanEntity, buildingWorld: BuildingWorld) =
    menu(buildText { spacer("Bau-Welt bearbeiten") }, height) {
        withOutline(width, height)
        withOutClicks()
        withHomeButton(height)

        val previousState = buildingWorld.status.allowBuild
        var currentState = previousState

        val statusButton = ToggleButton(
            4, 2, 1, 1, buildingWorld.status.allowBuild
        ).apply {
            setEnabledItem(GuiItem(MenuHeads.STATE_EDITING.apply {
                displayName {
                    infoColored("Status: ")
                    variableValue("Bearbeitbar")
                }
            }) {
                currentState = false

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Die Bauwelt ist nun als fertig markiert.")
                }
                it.whoClicked.playClickSound()
            })

            setDisabledItem(GuiItem(MenuHeads.STATE_BLOCKED.apply {
                displayName {
                    infoColored("Status: ")
                    variableValue("Fertiggestellt")
                }
            }) {
                currentState = true

                it.whoClicked.sendText {
                    appendSuccessPrefix()
                    success("Die Bauwelt ist nun als bearbeitbar markiert.")
                }
                it.whoClicked.playClickSound()
            })
        }

        setOnClose {
            if (previousState != currentState) {
                buildingWorldService.saveBuildingWorld(buildingWorld.copy(status = if (currentState) BuildingWorld.Status.EDITING else BuildingWorld.Status.DONE))
            }
        }

        addPane(statusButton)
        show(player)
    }

private fun HumanEntity.playClickSound() {
    this.playSound(true) {
        type(Sound.UI_BUTTON_CLICK)
    }
}