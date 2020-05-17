package dev.shog.osmpl.util.commands

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiMessageHandler
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

/**
 * Manages the balance leaderboard. /baltop
 */
private object BalTopHandler {
    private val timer = Timer()

    /**
     * The player's username to their balance.
     */
    var data = ArrayList<Pair<String, Double>>()
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
        val new = arrayListOf<Pair<String, Double>>()

        DataManager.data
                .forEach { user -> new.add(user.name to Economy.getMoney(user.name)) }

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
internal val BAL_TOP = Command.make("baltop") {
    if (args.size == 1 && args[0].equals("loc", true) && sender is Player) {
        var userLoc = -1

        BalTopHandler.data.forEachIndexed { index, pair ->
            if (pair.first.equals(sender.name, true))
                userLoc = index
        }

        if (userLoc != -1)
            sendMessageHandler("baltop.loc", userLoc + 1)
        else sendMessageHandler("baltop.error")

        return@make true
    }

    val size = if (BalTopHandler.data.size < 10)
        BalTopHandler.data.size
    else 10

    val message = BalTopHandler.data
            .subList(0, size)
            .mapIndexed { index, pair ->
                messageContainer.getMessage("baltop.user-entry", index + 1, pair.first, "$${pair.second.roundToInt()}")
            }
            .joinToString("\n")

    sendMultiMessageHandler("baltop.header", message)
    true
}