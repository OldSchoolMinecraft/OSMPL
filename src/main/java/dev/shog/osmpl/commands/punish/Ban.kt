package dev.shog.osmpl.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.broadcastPermission
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.stream.Collectors

/**
 * The ban command.
 */
internal val BAN_COMMAND = Command.make("ban") {
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
            } else "You have been banned!"

            osmPlugin.server.onlinePlayers.asSequence()
                    .filter { opl -> opl.name.toLowerCase() == user.name.toLowerCase() }
                    .forEach { opl -> opl.kickPlayer("${ChatColor.RED}You have been banned!") }

            val senderName = if (sender is Player) sender.name else "Console"

            DataManager.punishUser(
                    user.name,
                    senderName,
                    Punishment(System.currentTimeMillis(), reason, PunishmentType.BAN, -1)
            )

            broadcastPermission(
                    Pair("${ChatColor.RED}${user.name} (${user.ip}) has been banned by $senderName for \"$reason\"", "osm.bannotify"),
                    Pair("${ChatColor.RED}${user.name} has been banned by $senderName for \"$reason\"", "osm.bannotify.sanitized")
            )
        }
    }

    true
}