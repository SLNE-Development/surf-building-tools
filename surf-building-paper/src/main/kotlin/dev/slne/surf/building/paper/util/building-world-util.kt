package dev.slne.surf.building.paper.util

fun generateBuildingWorldId(): String {
    val charset = ('a'..'z') + ('0'..'9')
    return (1..8)
        .map { charset.random() }
        .joinToString("")
}