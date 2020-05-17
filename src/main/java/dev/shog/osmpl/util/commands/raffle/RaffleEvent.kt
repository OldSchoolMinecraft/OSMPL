package dev.shog.osmpl.util.commands.raffle

import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.api.msg.broadcastMultiMessageHandler
import dev.shog.osmpl.parsePercent
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.*
import kotlin.concurrent.timerTask

/**
 * A raffle event.
 *
 * @param delay Delay til winners are picked (in minutes)
 * @param winnersAmount The amount of winners
 * @param costOfTicket The cost of a single ticket
 * @param limit The total limit of tickets (winner is picked on full)
 * @param playerLimit The limit of tickets per player
 * @param server The server
 * @param prize The prize string
 * @param serverCtx This will be used for server announcements.
 * @param admin The user who made the raffle
 */
class RaffleEvent(
        val delay: Long,
        val winnersAmount: Int,
        val costOfTicket: Int,
        val limit: Int,
        val playerLimit: Int,
        val server: Server,
        val serverCtx: CommandContext,
        val prize: String,
        val admin: Player
) {
    companion object {
        /**
         * The current event.
         */
        var CURRENT_EVENT: RaffleEvent? = null
            get() {
                return field ?: throw Exception("There isn't a raffle going on at the moment!")
            }

        /**
         * If there's an event going on.
         */
        fun currentEvent(): Boolean =
                try {
                    CURRENT_EVENT

                    true
                } catch (ex: Exception) { false }

        /**
         * End the event with or without a winner.
         */
        fun endEvent(ctx: CommandContext, pickWinner: Boolean = false, user: CommandSender? = null) {
            if (CURRENT_EVENT != null) {
                if (pickWinner) {
                    val res = CURRENT_EVENT?.runWinner() ?: false

                    if (!res && user != null) {
                        ctx.sendMessageHandler("raffle.error.not-enough")
                        return
                    }
                }

                CURRENT_EVENT = null
            }
        }
    }

    /**
     * The users who have bought tickets
     */
    val names = hashMapOf<String, Int>()

    /**
     * Get the amount of tickets purchased buy a user.
     */
    fun getUserAmount(user: String): Int =
            names[user] ?: 0

    private val limiter: RaffleLimit = RaffleLimit(limit) { runWinner() }

    /**
     * The timer
     */
    private val timer = Timer()

    init {
        timer.schedule(timerTask {
            runWinner()
        }, delay)
    }

    /**
     * Get the total amount of purchased tickets.
     */
    fun getTotalPurchasedTickets() =
            names
                    .asSequence()
                    .map { pair -> pair.value }
                    .sum()

    /**
     * Get winners
     */
    fun runWinner(): Boolean {
        if (winnersAmount != names.size)
            return false

        timer.cancel()
        val winners = getWinners()

        if (winners.size == 1) {
            val winner = winners.first()

            serverCtx.broadcastMultiMessageHandler("raffle.winner-single", winner.first, prize, winner.second.parsePercent())
        } else {
            val message = buildString {
                winners.forEachIndexed { index, pair ->
                    if (winners.size - 1 == index) {
                        append("and ${pair.first} §8(${pair.second.parsePercent()}%)§7")
                    } else {
                        append("§7${pair.first} §8(${pair.second.parsePercent()}%)§7, ")
                    }
                }
            }

            serverCtx.broadcastMultiMessageHandler("raffle.winner-multiple", message, prize)
        }

        return true
    }

    /**
     * If [name] is above the [playerLimit] with [amount].
     */
    fun isAboveLimit(name: String, amount: Int) =
            (names[name] ?: 0) + amount > playerLimit

    /**
     * Enter a user [name] with their [amount].
     */
    fun enterUser(name: String, amount: Int = 1) {
        if (amount > limiter.limit || isAboveLimit(name, amount))
            return

        limiter.take(amount)

        if (names.contains(name.toLowerCase()))
            names[name.toLowerCase()] = names[name.toLowerCase()]!! + amount
        else {
            names[name.toLowerCase()] = amount
        }
    }

    /**
     * Get the winning users' names
     */
    private fun getWinners(total: Int = getTotalPurchasedTickets()): List<Pair<String, Double>> {
        val winners = arrayListOf<String>()

        while (winners.size != winnersAmount) {
            val user = names.keys.random()

            if (!winners.contains(user))
                winners.add(user)
        }

        return winners
                .map { key -> Pair(key, (names[key] ?: 0).toDouble() / total.toDouble()) }
    }
}
