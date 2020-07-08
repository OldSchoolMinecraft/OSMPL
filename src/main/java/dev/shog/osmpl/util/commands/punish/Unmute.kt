package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.unPunishmentWebhook
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * The unmute command.
 */
internal val UN_MUTE_COMMAND = Command.make("unmute") {
    if (args.isEmpty())
        return@make false
    else {
        val player = args[0]
        val user = DataManager.getUserData(player.toLowerCase())

        when {
            user == null ->
                sender.sendMessage("${ChatColor.RED}Player was not found!")

            user.name.equals(sender.name, true) ->
                sender.sendMessage("${ChatColor.RED}You cannot unmute yourself!")

            else -> {
                val senderName = if (sender is Player) sender.name else "Console"

                if (user.currentMute != null) {
                    unPunishmentWebhook(user.name, user.currentMute!!)
                    user.currentMute = null

                    broadcastPermission(
                            Pair("${ChatColor.RED}${user.name} (${user.ip}) has been unmuted by $senderName", "osm.bannotify"),
                            Pair("${ChatColor.RED}${user.name} has been unmuted by $senderName", "osm.bannotify.sanitized")
                    )
                } else {
                    sender.sendMessage("${ChatColor.RED}That player isn't muted!")
                }
            }
        }
    }

    true
}