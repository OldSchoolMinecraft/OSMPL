package dev.shog.osmpl.questss.quest.handle.commands

import dev.shog.osmpl.quests.quest.handle.ranks.user.User
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.entity.Player

/**
 * View your XP.
 */
val VIEW_XP = Command.make("xp") {
    if (sender is Player) {
        sendMessageHandler("commands.xp.xp", User.getUser(sender.name).xp)
    } else {
        sendMessageHandler("command.no-console")
    }

    true
}