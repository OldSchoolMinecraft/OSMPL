package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiMessageHandler
import dev.shog.osmpl.fancyDate
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

fun divide(one: Int, two: Int): Int {
    when {
        one == 0 -> return 0
        two == 0 -> return one
    }

    return one / two
}

/**
 * The play time top handler. Manages the play time leaderboard. /kdlb
 */
private object KillDeathLeaderboard {
    private val timer = Timer()

    /**
     * A username and their kills, deaths, and KDR.
     */
    var data = arrayListOf<Pair<String, Triple<Int, Int, Int>>>()
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
        val new = arrayListOf<Pair<String, Triple<Int, Int, Int>>>()

        DataManager.data
                .forEach { user -> new.add(user.name to Triple(user.kills, user.deaths, divide(user.kills, user.deaths))) }

        new.sortByDescending { user ->
            user.second.third
        }

        data = new
    }

    init {
        timer.schedule(timerTask { refresh() }, 0, TimeUnit.HOURS.toMillis(1))
    }
}

/**
 * PTT command
 */
internal val KILL_DEATH_LEADERBOARD = Command.make("kdlb") {
    if (args.size == 1 && sender is Player) {
        when (args[0].toLowerCase()) {
            "loc" -> {
                var userLoc = -1

                KillDeathLeaderboard.data.forEachIndexed { index, pair ->
                    if (pair.first.equals(sender.name, true))
                        userLoc = index
                }

                if (userLoc != -1)
                    sendMessageHandler("kdlb.loc", userLoc + 1)
                else sendMessageHandler("kdlb.error")

                return@make true
            }

            "cur" -> {
                val user = DataManager.getUserData(sender.name)

                if (user != null) {
                    sendMessageHandler("kdlb.current", divide(user.kills, user.deaths), user.kills, user.deaths)
                }

                return@make true
            }

        }
    }

    val size = if (KillDeathLeaderboard.data.size < 10)
        KillDeathLeaderboard.data.size
    else 10

    val message = KillDeathLeaderboard.data
            .subList(0, size)
            .mapIndexed { index, pair ->
                messageContainer.getMessage("kdlb.user-entry", index + 1, pair.first, pair.second.third, pair.second.first, pair.second.second)
            }
            .joinToString("\n")

    sendMultiMessageHandler("kdlb.header", message)
    true
}