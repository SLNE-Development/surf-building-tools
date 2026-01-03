package dev.slne.surf.building.paper.database.repository

import dev.slne.surf.building.paper.database.table.BuildingWorldsTable
import dev.slne.surf.building.paper.world.BuildingWorld
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import kotlinx.coroutines.flow.map

val buildingWorldRepository = BuildingWorldRepository()

class BuildingWorldRepository {
    suspend fun getBuildingWorlds() = suspendTransaction {
        BuildingWorldsTable.selectAll().map {
            BuildingWorld(
                buildingWorldName = it[BuildingWorldsTable.buildingWorldName],
                buildingWorldId = it[BuildingWorldsTable.buildingWorldId],
                worldName = it[BuildingWorldsTable.worldName],
                worldUuid = it[BuildingWorldsTable.worldUuid],
                authorName = it[BuildingWorldsTable.authorName],
                authorUuid = it[BuildingWorldsTable.authorUuid],
                createdAt = it[BuildingWorldsTable.createdAt]
            )
        }
    }

    suspend fun saveBuildingWorld(buildingWorld: BuildingWorld) = suspendTransaction {
        BuildingWorldsTable.upsert {
            it[buildingWorldName] = buildingWorld.buildingWorldName
            it[buildingWorldId] = buildingWorld.buildingWorldId
            it[worldName] = buildingWorld.worldName
            it[worldUuid] = buildingWorld.worldUuid
            it[authorName] = buildingWorld.authorName
            it[authorUuid] = buildingWorld.authorUuid
            it[createdAt] = buildingWorld.createdAt
        }
    }
}