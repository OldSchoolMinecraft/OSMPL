package dev.shog.osmpl.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.broadcastPermission
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

        if (user == null) {
            sender.sendMessage("${ChatColor.RED}Player was not found!")
        } else {
            val senderName = if (sender is Player) sender.name else "Console"

            if (user.currentMute != null) {
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

    true
}