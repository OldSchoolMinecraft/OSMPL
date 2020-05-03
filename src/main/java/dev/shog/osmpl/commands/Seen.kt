package dev.shog.osmpl.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.User
import dev.shog.osmpl.defaultFormat
import dev.shog.osmpl.fancyDate
import dev.shog.osmpl.sendMessageHandler
import dev.shog.osmpl.sendMultiMessageHandler
import org.bukkit.entity.Player

/**
 * The donate command.
 */
internal val SEEN_COMMAND = Command.make("seen") {
    when {
        args.isEmpty() && sender is Player -> seen(sender.name)
        args.isEmpty() && sender !is Player -> sendMessageHandler("seen.console")
        else -> seen(args[0])
    }

    true
}

/**
 * Get the seen for a player.
 */
private fun CommandContext.seen(name: String) {
    val data = DataManager.getUserData(name)

    if (data == null)
        sendMessageHandler("seen.not-found")
    else {
        val user = osmPlugin.server.onlinePlayers
                .asSequence()
                .filter { pl -> pl.name.equals(name, true) }
                .singleOrNull()

        when {
            user != null -> onlinePlayerSeen(user, data)
            data.currentBan == null -> offlinePlayerSeen(data)
            data.currentBan != null -> bannedPlayerSeen(data)
        }
    }
}

/**
 * Get seen data for online player.
 *
 * @param player The player
 * @param data The data for the [player]
 */
private fun CommandContext.onlinePlayerSeen(player: Player, data: User) {
    val mute = mutedPlayerData(data)

    sendMultiMessageHandler("seen.online",
            player.displayName,
            data.lastLogIn.defaultFormat(),
            (System.currentTimeMillis() - data.lastLogIn).fancyDate(),
            getPlayTime(data),
            data.firstJoin.defaultFormat(),
            mute.first,
            mute.second
    )
}

/**
 * Get the current playtime of [data].
 */
private fun getPlayTime(data: User): String =
        ((System.currentTimeMillis() - data.lastLogIn) + data.playTime).fancyDate()

/**
 * Get seen data for offline player.
 *
 * @param data The data player.
 */
private fun CommandContext.offlinePlayerSeen(data: User) {
    val mute = mutedPlayerData(data)

    sendMultiMessageHandler("seen.offline",
            data.name,
            data.lastLogOut.defaultFormat(),
            data.playTime.fancyDate(),
            data.firstJoin.defaultFormat(),
            mute.first,
            mute.second
    )
}

/**
 * Get data for a muted player.
 *
 * @param data The data player.
 */
private fun CommandContext.mutedPlayerData(data: User): Pair<String, String> {
    val mute = data.currentMute

    return if (mute != null) {
        val reason = mute.reason
        val expire = when (mute.expire) {
            -1L -> "Permanent"
            else -> mute.expire.defaultFormat()
        }

        " §cMuted" to messageContainer.getMessage("seen.muted", expire, reason)
    } else "" to ""
}

/**
 * Get seen data for a banned player.
 *
 * @param data The data player.
 */
fun CommandContext.bannedPlayerSeen(data: User) {
    val ban = data.currentBan

    val reason = ban?.reason ?: "Banned"
    val expire = when (ban?.expire) {
        -1L, null -> "Permanent"
        else -> ban.expire.defaultFormat()
    }

    sendMultiMessageHandler("seen.banned",
            data.name,
            data.lastLogOut.defaultFormat(),
            data.playTime.fancyDate(),
            data.firstJoin.defaultFormat(),
            reason,
            expire
    )
}