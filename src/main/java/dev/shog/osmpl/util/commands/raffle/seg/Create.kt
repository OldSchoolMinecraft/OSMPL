package dev.shog.osmpl.util.commands.raffle.seg

import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.api.msg.broadcastMessageHandler
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.util.commands.raffle.RaffleEvent
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Create a Raffle
 *
 * @return true
 */
internal fun CommandContext.raffleCreateSegment(): Boolean {
    val totalLimit = args[1].toIntOrNull()
    val limitPerUser = args[2].toIntOrNull()
    val costOfTicket = args[3].toIntOrNull()
    val time = args[4].toIntOrNull()
    val amountOfWinners = args[5].toIntOrNull()

    val anyNull = sequenceOf(time, costOfTicket, limitPerUser, totalLimit, amountOfWinners)
            .any { it == null }

    if (anyNull) {
        sendMessageHandler("raffle.error.valid-number")
        return true
    }

    if (limitPerUser!! > totalLimit!!) {
        sendMessageHandler("raffle.error.player-limit-over-global")
        return true
    }

    val params = args.toMutableList()

    (1..6)
            .forEach { _ -> params.removeAt(0) }

    val prize = params
            .stream()
            .collect(Collectors.joining(" "))
            .trim()

    val endsAt = TimeUnit.MINUTES.toMillis(time!!.toLong())

    broadcastMessageHandler("raffle.create.broadcast", prize, time, totalLimit)

    RaffleEvent.CURRENT_EVENT = RaffleEvent(
            endsAt,
            amountOfWinners!!,
            costOfTicket!!,
            totalLimit,
            limitPerUser,
            osmModule.pl.server,
            this,
            prize,
            sender as Player
    )

    return true
}