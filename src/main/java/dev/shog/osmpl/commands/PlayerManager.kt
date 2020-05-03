package dev.shog.osmpl.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.sendMessageHandler

/**
 * The player manager command.
 */
internal val PLAYER_MANAGER = Command.make("plmg") {
    if (args.isEmpty())
        return@make false

    val player = args.first().toLowerCase()
    val userData = DataManager.getUserData(player)

    if (userData != null) {
        val user = osmPlugin.server.onlinePlayers
                .singleOrNull { onlinePlayer -> onlinePlayer.name.equals(player, true) }

        if (user != null) {
            sendMessageHandler("player-manager.online-user", user.name, userData)
        } else {
            sendMessageHandler("player-manager.offline-user", player, userData)
        }
    } else {
        sendMessageHandler("player-manager.invalid-user")
    }

    true
}