package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.stream.Collectors

internal val WARN_COMMAND = Command.make("warn") {
    if (args.isEmpty())
        return@make false
    else {
        val player = args[0]
        val user = DataManager.getUserData(player.toLowerCase())

        if (user == null) {
            sender.sendMessage("${ChatColor.RED}Player was not found!")
        } else {
            val reason = if (args.size > 1) {
                val rArgs = args.toMutableList()

                rArgs.removeAt(0)

                rArgs.stream()
                        .collect(Collectors.joining(" "))
                        .trim()
            } else "You have been warned!"

            val avoid = osmModule.pl.server.onlinePlayers
                    .filter { p -> p.name.equals(user.name, true) }
                    .filter { p -> p.hasPermissionOrOp("osm.warn.avoid") }
                    .any()

            if (avoid) {
                sendMessageHandler("warn.avoid")
                return@make true
            }

            val senderName = if (sender is Player) sender.name else "Console"

            DataManager.punishUser(
                    user.name,
                    senderName,
                    Punishment(System.currentTimeMillis(), reason, PunishmentType.WARN, -1)
            )

            broadcastPermission(
                    Pair("${ChatColor.RED}${user.name} (${user.ip}) has been warned by $senderName for \"$reason\"", "osm.bannotify"),
                    Pair("${ChatColor.RED}${user.name} has been warned by $senderName for \"$reason\"", "osm.bannotify.sanitized")
            )
        }
    }

    true
}