package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.sendMessage
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiline
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.ChatColor
import org.bukkit.entity.Player

internal val SEARCH_IP_COMMAND = Command.make("searchip") {
    when {
        sender !is Player -> {
            sendMessageHandler("error.console")
            return@make true
        }

        !sender.hasPermissionOrOp("osmpl.searchip") -> {
            sendMessageHandler("seen.no-permission")
            return@make true
        }
    }

    if (args.isEmpty()) {
        sendMessage("${ChatColor.RED}You must include an IP to search!")
        return@make true
    }

    val ip = args[0]
    val filter = DataManager.data.filter { user -> user.ip == ip }

    if (filter.isEmpty()) {
        sendMessage("${ChatColor.RED}No users could be matched to that IP!")
    } else {
        sendMultiline("${ChatColor.GREEN}${filter.size} users were found with that IP!\n${filter.joinToString(",")}")
    }

    true
}
