package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.entity.Player

val disabledStaffMessages = arrayListOf<String>()

internal val TOGGLE_STAFF_MESSAGES = Command.make("togglestaffmessages") {
    if (sender !is Player) {
        return@make true
    }

    if (sender.hasPermissionOrOp("osm.tsm")) {
        if (disabledStaffMessages.contains(sender.name.toLowerCase())) {
            disabledStaffMessages.remove(sender.name.toLowerCase())

            sendMessageHandler("tsm.enabled")
        } else {
            disabledStaffMessages.add(sender.name.toLowerCase())

            sendMessageHandler("tsm.disabled")
        }
    } else {
        sendMessageHandler("seen.no-permission")
    }

    true
}