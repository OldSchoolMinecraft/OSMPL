package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessage
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.fancyDate
import java.util.concurrent.TimeUnit

/**
 * The streak  command.
 */
internal val STREAK_COMMAND = Command.make("streak") {
    val rs = SqlHandler.getConnection(db = "money")
            .prepareStatement("SELECT lastLogin, streak FROM dailyreward WHERE player = ?")
            .apply {
                setString(1, sender.name.toLowerCase())
            }
            .executeQuery()

    if (rs.next()) {
        sendMessageHandler(
                "streak.command.default",
                rs.getInt("streak"),
                rs.getInt("streak") * 5,
                ((TimeUnit.DAYS.toMillis(1) + rs.getLong("lastLogin")) - System.currentTimeMillis()).fancyDate() // not readable lol
        )
    } else {
        sendMessage("yo lol")
    }

    true
}