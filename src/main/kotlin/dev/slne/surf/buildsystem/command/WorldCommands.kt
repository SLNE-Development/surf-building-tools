package dev.slne.surf.buildsystem.command

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.util.toOfflinePlayers
import dev.slne.surf.buildsystem.gui.view.canManageWarps
import dev.slne.surf.buildsystem.gui.view.canModifyBuildingWorld
import dev.slne.surf.buildsystem.permission.PermissionRegistry
import dev.slne.surf.buildsystem.service.WorldManager
import dev.slne.surf.buildsystem.util.currentBuildingWorld
import dev.slne.surf.buildsystem.world.BuildingWorld
import dev.slne.surf.buildsystem.world.Warp
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val warpSuggestions = ArgumentSuggestions.stringCollection<CommandSender> { info ->
    (info.sender as? Player)?.currentBuildingWorld()?.warps?.map { it.name } ?: emptyList()
}

private val memberSuggestions = ArgumentSuggestions.stringCollection<CommandSender> { info ->
    (info.sender as? Player)?.currentBuildingWorld()?.members?.toOfflinePlayers()
        ?.mapNotNull { it.name }
        ?: emptyList()
}

private val onlinePlayerSuggestions = ArgumentSuggestions.stringCollection<CommandSender> {
    Bukkit.getOnlinePlayers().map { it.name }
}


private fun Player.sendError(message: String) = sendText {
    appendErrorPrefix()
    error(message)
}

private fun Player.sendSuccess(message: String) = sendText {
    appendSuccessPrefix()
    success(message)
}

private fun Player.requireBuildingWorld(): BuildingWorld? =
    currentBuildingWorld() ?: run {
        sendError("Du befindest dich in keiner Bau-Welt!")
        null
    }

private fun Player.requireEditableWorld(): BuildingWorld? {
    val buildingWorld = requireBuildingWorld() ?: return null
    if (!canManageWarps(buildingWorld)) {
        sendError("Du hast keine Berechtigung, diese Bau-Welt zu bearbeiten!")
        return null
    }
    return buildingWorld
}

private fun Player.requireMemberManagement(): BuildingWorld? {
    val buildingWorld = requireBuildingWorld() ?: return null
    if (!canModifyBuildingWorld()) {
        sendError("Du hast keine Berechtigung, die Mitglieder dieser Bau-Welt zu verwalten!")
        return null
    }
    return buildingWorld
}

fun worldCommands() {
    createWarpCommand()
    deleteWarpCommand()
    warpCommand()
    setSpawnCommand()
    addMemberCommand()
    removeMemberCommand()
}

private fun createWarpCommand() = commandTree("createwarp") {
    withPermission(PermissionRegistry.COMMAND)

    stringArgument("name") {
        playerExecutor { player, args ->
            val name: String by args
            val buildingWorld = player.requireEditableWorld() ?: return@playerExecutor

            if (buildingWorld.warps.any { it.name.equals(name, ignoreCase = true) }) {
                player.sendError("Ein Warp mit dem Namen '$name' existiert bereits!")
                return@playerExecutor
            }

            val location = player.location
            WorldManager.addWarp(
                buildingWorld,
                Warp(
                    name = name,
                    x = location.x,
                    y = location.y,
                    z = location.z,
                    pitch = location.pitch,
                    yaw = location.yaw,
                    displayItem = Material.ENDER_EYE
                )
            )

            player.sendSuccess("Der Warp '$name' wurde erstellt!")
        }
    }
}

private fun deleteWarpCommand() = commandTree("deletewarp") {
    withAliases("delwarp")
    withPermission(PermissionRegistry.COMMAND)

    stringArgument("name") {
        replaceSuggestions(warpSuggestions)

        playerExecutor { player, args ->
            val name: String by args
            val buildingWorld = player.requireEditableWorld() ?: return@playerExecutor
            val warp = buildingWorld.warps.find { it.name.equals(name, ignoreCase = true) }

            if (warp == null) {
                player.sendError("Der Warp '$name' wurde nicht gefunden!")
                return@playerExecutor
            }

            WorldManager.deleteWarp(buildingWorld, warp)
            player.sendSuccess("Der Warp '${warp.name}' wurde gelöscht!")
        }
    }
}

private fun warpCommand() = commandTree("warp") {
    withPermission(PermissionRegistry.COMMAND)

    stringArgument("name") {
        replaceSuggestions(warpSuggestions)

        playerExecutor { player, args ->
            val name: String by args
            val buildingWorld = player.requireBuildingWorld() ?: return@playerExecutor
            val warp = buildingWorld.warps.find { it.name.equals(name, ignoreCase = true) }

            if (warp == null) {
                player.sendError("Der Warp '$name' wurde nicht gefunden!")
                return@playerExecutor
            }

            player.teleportAsync(warp.location(player.world)).thenAccept { success ->
                if (success) player.sendSuccess("Du wurdest zum Warp '${warp.name}' teleportiert!")
            }
        }
    }
}

private fun setSpawnCommand() = commandTree("setspawn") {
    withPermission(PermissionRegistry.COMMAND)

    playerExecutor { player, _ ->
        player.requireEditableWorld() ?: return@playerExecutor

        player.world.spawnLocation = player.location
        player.sendSuccess("Der Spawn der Bau-Welt wurde gesetzt!")
    }
}

private fun addMemberCommand() = commandTree("addmember") {
    withPermission(PermissionRegistry.COMMAND)

    stringArgument("player") {
        replaceSuggestions(onlinePlayerSuggestions)

        playerExecutor { player, args ->
            val playerName = args["player"] as String
            val buildingWorld = player.requireMemberManagement() ?: return@playerExecutor

            @Suppress("DEPRECATION")
            val target = Bukkit.getOfflinePlayer(playerName)

            when {
                !target.hasPlayedBefore() && !target.isOnline ->
                    player.sendError("Der Spieler '$playerName' wurde nicht gefunden!")

                target.uniqueId == buildingWorld.authorUuid ->
                    player.sendError("Der Ersteller der Welt kann nicht als Mitglied hinzugefügt werden!")

                target.uniqueId in buildingWorld.members ->
                    player.sendError("'$playerName' ist bereits Mitglied dieser Welt!")

                else -> {
                    WorldManager.addMember(buildingWorld, target.uniqueId)
                    player.sendSuccess("'${target.name ?: playerName}' wurde als Mitglied hinzugefügt!")
                }
            }
        }
    }
}

private fun removeMemberCommand() = commandTree("removemember") {
    withPermission(PermissionRegistry.COMMAND)

    stringArgument("player") {
        replaceSuggestions(memberSuggestions)

        playerExecutor { player, args ->
            val playerName = args["player"] as String
            val buildingWorld = player.requireMemberManagement() ?: return@playerExecutor
            val member = buildingWorld.members.toOfflinePlayers()
                .find { it.name.equals(playerName, ignoreCase = true) }

            if (member == null) {
                player.sendError("'$playerName' ist kein Mitglied dieser Welt!")
                return@playerExecutor
            }

            WorldManager.removeMember(buildingWorld, member.uniqueId)
            player.sendSuccess("'${member.name ?: playerName}' wurde als Mitglied entfernt!")
        }
    }
}
