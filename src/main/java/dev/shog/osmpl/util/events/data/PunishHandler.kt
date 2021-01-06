package dev.shog.osmpl.util.events.data

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.User
import dev.shog.osmpl.api.data.isExpired
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.defaultFormat
import net.minecraft.server.PlayerList
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.*

object PunishHandler {
    val BAN_HANDLE: (OsmModule) -> PlayerListener = { osm: OsmModule ->
        object : PlayerListener() {
            override fun onPlayerPreLogin(event: PlayerPreLoginEvent?) {
                if (event != null) {
                    if (DataManager.isUserBanned(event.name))
                        osm.handleBan(DataManager.getUserData(event.name), event)
                }
            }
        }
    }

    val MUTE_HANDLE: (OsmModule) -> PlayerListener = { osm: OsmModule ->
        object : PlayerListener() {
            override fun onPlayerChat(event: PlayerChatEvent?) {
                if (event != null && DataManager.isUserMuted(event.player.name))
                    osm.handleMute(DataManager.getUserData(event.player.name), event)
            }
        }
    }

    val MUTE_COMMAND_HANDLE: (OsmModule) -> PlayerListener = { osm: OsmModule ->
        object : PlayerListener() {
            override fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent?) {
                if (event != null && DataManager.isUserMuted(event.player.name))
                    osm.handleCommandMute(DataManager.getUserData(event.player.name), event)
            }
        }
    }
}

/**
 * Handle warns.
 */
internal fun OsmModule.handleWarn(data: User?, player: Player) {
    if (data != null) {
        val warns = data.punishments
            .filter { punishment -> punishment.type == PunishmentType.WARN }
            .filter { punishment -> !punishment.isExpired() }

        if (warns.isNotEmpty()) {
            player.sendMessage(messageContainer.getMessage("warn.info", warns.size, 3))
        }
    }
}

/**
 * Handle a ban.
 * This assumes that the player has been confirmed as ban, and handles that accordingly.
 *
 * @param data The data user that corresponds to the banned user.
 * @param event The event that corresponds to the banned user.
 */
internal fun OsmModule.handleBan(data: User?, event: PlayerPreLoginEvent) {
    val ban = data?.currentBan

    if (ban != null) {
        if (ban.isExpired()) {
            pl.server.broadcastPermission(
                messageContainer.getMessage("admin.expired.ban", event.name),
                "osm.notify.ban",
                true
            )

            data.currentBan = null
        } else {
            pl.server.broadcastPermission(
                messageContainer.getMessage("admin.tried.ban", event.name),
                "osm.notify.ban",
                true
            )

            val message = when {
                ban.expire == -1L ->
                    messageContainer.getMessage("banned.permanent", ban.reason)

                ban.expire != -1L ->
                    messageContainer.getMessage("banned.timed", ban.reason, ban.expire.defaultFormat())

                else ->
                    messageContainer.getMessage("banned.invalid")
            }

            if (message.length > 100) {
                // If the message is over 100 (the allowed amount), slim it down.
                event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, messageContainer.getMessage("banned.invalid"))
            } else event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, message)
        }
    }
}

/**
 * Commands that shouldn't be usable while muted.
 */
val MUTE_CMD = arrayListOf("me", "r", "msg", "er", "emsg", "eme", "tell")

/**
 * Handle a mute.
 */
internal fun OsmModule.handleCommandMute(data: User?, event: PlayerCommandPreprocessEvent) {
    val mute = data?.currentMute

    if (mute != null && !mute.isExpired()) {
        val cmdStr = event.message.split(" ")[0]

        val bannedCmd = MUTE_CMD
            .map { cmd -> cmdStr.equals("/$cmd", true) }
            .contains(true)

        if (bannedCmd) {
            pl.server.broadcastPermission(
                messageContainer.getMessage("admin.tried.mute-command", event.player.name),
                "osm.notify.ban",
                true
            )

            val message = when {
                mute.expire == -1L ->
                    messageContainer.getMessage("muted.permanent", mute.reason)

                mute.expire != -1L ->
                    messageContainer.getMessage("muted.timed", mute.reason, mute.expire.defaultFormat())

                else ->
                    messageContainer.getMessage("muted.invalid")
            }

            event.isCancelled = true
            event.player.sendMessage(message)
        }
    }
}

/**
 * Handle a mute.
 */
internal fun OsmModule.handleMute(data: User?, event: PlayerChatEvent) {
    val mute = data?.currentMute

    if (mute != null) {
        if (mute.isExpired()) {
            pl.server.broadcastPermission(
                messageContainer.getMessage("admin.expired.mute", event.player.name),
                "osm.notify.ban",
                true
            )

            data.currentMute = null

            event.player.sendMessage(messageContainer.getMessage("admin.expired.player-mute", event.player.name))
        } else {
            pl.server.broadcastPermission(
                messageContainer.getMessage("admin.tried.mute", event.player.name),
                "osm.notify.ban",
                true
            )

            val message = when {
                mute.expire == -1L ->
                    messageContainer.getMessage("muted.permanent", mute.reason)

                mute.expire != -1L ->
                    messageContainer.getMessage("muted.timed", mute.reason, mute.expire.defaultFormat())

                else ->
                    messageContainer.getMessage("muted.invalid")
            }

            event.isCancelled = true
            event.player.sendMessage(message)
        }
    }
}