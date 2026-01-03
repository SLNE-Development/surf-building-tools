package dev.slne.surf.building.paper.util

import dev.slne.surf.building.paper.service.buildingWorldService

fun generateBuildingWorldId(): String {
    val charset = ('a'..'z') + ('0'..'9')

    while (true) {
        val id = (1..8)
            .map { charset.random() }
            .joinToString("")

        val exists = buildingWorldService.buildingWorlds
            .any { it.buildingWorldId == id }

        if (!exists) {
            return id
        }
    }
}
