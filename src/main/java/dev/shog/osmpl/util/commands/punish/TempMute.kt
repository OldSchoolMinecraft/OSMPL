package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.*
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.api.msg.sendMessage
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.stream.Collectors

/**
 * The ban command.
 */
internal val TEMP_MUTE_COMMAND = Command.make("tempmute") {
    if (args.size < 2)
        return@make false
    else {
        val player = args[0]
        val time = args[1]

        val parsedTime = parseDateDiff(time, true)

        if (parsedTime == -1L) {
            sendMessage("${ChatColor.RED}Invalid time!")
            return@make true
        }

        val user = DataManager.getUserData(player.toLowerCase())

        if (user == null) {
            sendMessage("${ChatColor.RED}Player was not found!")
        } else {
            val reason = if (args.size > 1) {
                val rArgs = args.toMutableList()

                rArgs.removeAt(0)
                rArgs.removeAt(0)

                rArgs.stream()
                        .collect(Collectors.joining(" "))
                        .trim()
            } else "You have been temporarily muted!"

            val avoid = osmModule.pl.server.onlinePlayers
                    .filter { p -> p.name.equals(user.name, true) }
                    .filter { p -> p.hasPermissionOrOp("osm.mute.avoid") }
                    .any()

            if (avoid) {
                sendMessageHandler("mute.avoid")
                return@make true
            }

            val senderName = if (sender is Player) sender.name else "Console"

            DataManager.punishUser(
                    user.name,
                    senderName,
                    Punishment(System.currentTimeMillis(), reason, PunishmentType.MUTE, parsedTime)
            )

            val op = "${ChatColor.RED}${user.name} (${user.ip}) has been muted by $senderName for \"$reason\" until ${parsedTime.defaultFormat()}"

            val default = "${ChatColor.RED}${user.name} has been muted by $senderName for \"$reason\" until ${parsedTime.defaultFormat()}"

            broadcastPermission(
                    Pair(op, "osm.bannotify"),
                    Pair(default, "osm.bannotify.sanitized")
            )
        }
    }

    true
}