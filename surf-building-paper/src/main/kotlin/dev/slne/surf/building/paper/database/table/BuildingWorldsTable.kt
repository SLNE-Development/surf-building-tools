package dev.slne.surf.building.paper.database.table

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object BuildingWorldsTable : LongIdTable("building_worlds") {
    val buildingWorldName = varchar("building_world_name", 255)
    val buildingWorldId = varchar("building_world_id", 8).uniqueIndex()
    val worldName = varchar("world_name", 255)
    val worldUuid = uuid("world_uuid")
    val authorName = varchar("author_name", 255)
    val authorUuid = uuid("author_uuid")
    val createdAt = long("created_at")
}