package dev.shog.osmpl.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.broadcastPermission
import dev.shog.osmpl.defaultFormat
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors


@Throws(Exception::class)
internal fun parseDateDiff(time: String, future: Boolean): Long {
    val timePattern: Pattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2)

    val m: Matcher = timePattern.matcher(time)

    var years = 0
    var months = 0
    var weeks = 0
    var days = 0
    var hours = 0
    var minutes = 0
    var seconds = 0
    var found = false

    while (m.find()) {
        if (m.group() == null || m.group().isEmpty()) continue

        for (i in 0 until m.groupCount()) {
            if (m.group(i) != null && m.group(i).isNotEmpty()) {
                found = true
                break
            }
        }

        if (found) {
            if (m.group(1) != null && m.group(1).isNotEmpty()) years = m.group(1).toInt()
            if (m.group(2) != null && m.group(2).isNotEmpty()) months = m.group(2).toInt()
            if (m.group(3) != null && m.group(3).isNotEmpty()) weeks = m.group(3).toInt()
            if (m.group(4) != null && m.group(4).isNotEmpty()) days = m.group(4).toInt()
            if (m.group(5) != null && m.group(5).isNotEmpty()) hours = m.group(5).toInt()
            if (m.group(6) != null && m.group(6).isNotEmpty()) minutes = m.group(6).toInt()
            if (m.group(7) != null && m.group(7).isNotEmpty()) seconds = m.group(7).toInt()
            break
        }
    }

    if (!found)
        return -1

    val c: Calendar = GregorianCalendar()
    if (years > 0) c.add(1, years * if (future) 1 else -1)
    if (months > 0) c.add(2, months * if (future) 1 else -1)
    if (weeks > 0) c.add(3, weeks * if (future) 1 else -1)
    if (days > 0) c.add(5, days * if (future) 1 else -1)
    if (hours > 0) c.add(11, hours * if (future) 1 else -1)
    if (minutes > 0) c.add(12, minutes * if (future) 1 else -1)
    if (seconds > 0) c.add(13, seconds * if (future) 1 else -1)

    return c.timeInMillis
}

/**
 * The ban command.
 */
internal val TEMP_BAN_COMMAND = Command.make("tempban") {
    if (args.size < 2)
        return@make false
    else {
        val player = args[0]
        val time = args[1]

        val parsedTime = parseDateDiff(time, true)

        if (parsedTime == -1L) {
            sender.sendMessage("${ChatColor.RED}Invalid time!")
            return@make true
        }

        val user = DataManager.getUserData(player.toLowerCase())

        if (user == null) {
            sender.sendMessage("${ChatColor.RED}Player was not found!")
        } else {
            val reason = if (args.size > 1) {
                val rArgs = args.toMutableList()

                rArgs.removeAt(0)
                rArgs.removeAt(0)

                rArgs.stream()
                        .collect(Collectors.joining(" "))
                        .trim()
            } else "You have been temporarily banned!"

            osmPlugin.server.onlinePlayers.asSequence()
                    .filter { opl -> opl.name.toLowerCase() == user.name.toLowerCase() }
                    .forEach { opl -> opl.kickPlayer("${ChatColor.RED}You have been banned until ${parsedTime.defaultFormat()}!") }

            val senderName = if (sender is Player) sender.name else "Console"

            DataManager.punishUser(
                    user.name,
                    senderName,
                    Punishment(System.currentTimeMillis(), reason, PunishmentType.BAN, parsedTime)
            )

            broadcastPermission(
                    Pair("${ChatColor.RED}${user.name} (${user.ip}) has been banned by $senderName for \"$reason\" until ${parsedTime.defaultFormat()}", "osm.bannotify"),
                    Pair("${ChatColor.RED}${user.name} has been banned by $senderName for \"$reason\" until ${parsedTime.defaultFormat()}", "osm.bannotify.sanitized")
            )
        }
    }

    true
}