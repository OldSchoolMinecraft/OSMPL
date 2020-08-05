package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.isExpired
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.sendMultiline
import dev.shog.osmpl.fancyDate
import org.bukkit.ChatColor

internal val WARNINGS_COMMAND = Command.make("warnings") {
    val user = DataManager.getUserData(sender.name) ?:  return@make false

    val currentWarns = user.punishments
            .filter { punishment -> punishment.type == PunishmentType.WARN }
            .filter { punishment -> !punishment.isExpired() }

    sendMultiline(buildString {
        if (currentWarns.isEmpty()) {
            append("${ChatColor.GRAY}none")
        } else {
            currentWarns.forEachIndexed { index, punishment ->
                val date = (punishment.expire - System.currentTimeMillis()).fancyDate()
                if (index == 0){
                    append(messageContainer.getMessage("warnings.index", index+1, punishment.reason, date).removePrefix("\n\n"))
                } else {
                    append(messageContainer.getMessage("warnings.index", index+1, punishment.reason, date))
                }
            }
        }
    })

    true
}