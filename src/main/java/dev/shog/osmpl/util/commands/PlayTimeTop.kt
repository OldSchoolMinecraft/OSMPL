package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.fancyDate
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiMessageHandler
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * The play time top handler. Manages the play time leaderboard. /ptt
 */
private object PlayTimeTopHandler {
    private val timer = Timer()

    /**
     * A username and their play time.
     */
    var data = ArrayList<Pair<String, Long>>()
        private set
        get() {
            if (field.isEmpty())
                refresh()

            return field
        }

    /**
     * Refresh [data].
     */
    fun refresh() {
        val new = arrayListOf<Pair<String, Long>>()

        DataManager.data
                .forEach { user -> new.add(user.name to user.playTime) }

        new.sortByDescending { user -> user.second }

        data = new
    }

    init {
        timer.schedule(timerTask { refresh() }, 0, TimeUnit.HOURS.toMillis(1))
    }
}

/**
 * PTT command
 */
internal val PLAY_TIME_TOP = Command.make("ptt") {
    if (args.size == 1 && args[0].equals("loc", true) && sender is Player) {
        var userLoc = -1

        PlayTimeTopHandler.data.forEachIndexed { index, pair ->
            if (pair.first.equals(sender.name, true))
                userLoc = index
        }

        if (userLoc != -1)
            sendMessageHandler("ptt.loc", userLoc + 1)
        else sendMessageHandler("ptt.error")

        return@make true
    }

    val size = if (PlayTimeTopHandler.data.size < 10)
        PlayTimeTopHandler.data.size
    else 10

    val message = PlayTimeTopHandler.data
            .subList(0, size)
            .mapIndexed { index, pair ->
                messageContainer.getMessage("ptt.user-entry", index + 1, pair.first, pair.second.fancyDate())
            }
            .joinToString("\n")

    sendMultiMessageHandler("ptt.header", message)
    true
}