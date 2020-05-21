package dev.shog.osmpl.quests.handle.commands

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiMessageHandler
import dev.shog.osmpl.fancyDate
import dev.shog.osmpl.quests.handle.ranks.user.User
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player

/**
 * View rank.
 */
val RANK = Command.make("rank") {
    val quests = osmModule as Quests

    if (sender !is Player) {
        sendMessageHandler("command.no-console")
        return@make true
    }

    val user = User.getUser(sender.name)
    val upperRank = quests.ladder.getUpperRank(user)

    val nextRank = if (upperRank != null) {
        sendMessageHandler("commands.rank.next-rank",
                upperRank.name,
                messageContainer.getMessage("commands.rank.requirements",
                        user.getPlayTime().fancyDate(), upperRank.requirements.timeHr,
                        user.xp, upperRank.requirements.xp,
                        Economy.getMoney(sender.name), upperRank.requirements.balance
                )
        )
    } else sendMessageHandler("commands.rank.max-rank")

    sendMultiMessageHandler(
            "commands.rank.default",
            quests.ladder.getRank(user.rank)?.name,
            nextRank
    )

    true
}