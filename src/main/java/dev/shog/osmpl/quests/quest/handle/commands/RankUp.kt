package dev.shog.osmpl.quests.quest.handle.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.quests.quest.handle.ranks.user.User
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player

/**
 * Rank up.
 */
val RANK_UP = Command.make("rankup") {
    if (sender !is Player) {
        sendMessageHandler("command.no-console")
        return@make true
    }
    val user = User.getUser(sender.name)

    user.rankUp(sender, osmModule as Quests)

    true
}