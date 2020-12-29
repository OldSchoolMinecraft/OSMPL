package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.unPunishmentWebhook
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * The ban command.
 */
internal val UN_BAN_COMMAND = Command.make("unban") {
    if (args.isEmpty())
        return@make false
    else {
        val player = args[0]
        val user = DataManager.getUserData(player.toLowerCase())

        if (user == null) {
            sender.sendMessage("${ChatColor.RED}Player was not found!")
        } else {
            val senderName = if (sender is Player) sender.name else "Console"

            if (user.currentBan != null) {
                unPunishmentWebhook(user.name, user.currentBan!!)

                user.currentBan = null

                broadcastPermission(
                        Triple(
                                "${ChatColor.RED}${user.name} (${user.ip}) has been unbanned by $senderName",
                                "osm.bannotify",
                                true
                        ),
                        Triple(
                                "${ChatColor.RED}${user.name} has been unbanned by $senderName",
                                "osm.bannotify.sanitized",
                                false
                        )
                )
            } else {
                sender.sendMessage("${ChatColor.RED}That player isn't banned!")
            }
        }
    }

    true
}