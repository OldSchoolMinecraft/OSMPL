package dev.shog.osmpl.commands.raffle.seg

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.sendMessageHandler
import dev.shog.osmpl.commands.raffle.RaffleEvent
import org.bukkit.entity.Player

/**
 * Buy Tickets
 *
 * @param args
 * @param player
 * @return true
 */
internal fun CommandContext.raffleBuyCommand(): Boolean {
    val ev = RaffleEvent.CURRENT_EVENT!!

    val amount = if (args.size == 2) {
        val arg = args[1].toIntOrNull()

        if (arg == null) {
            sendMessageHandler("raffle.error.valid-number")
            return true
        }

        arg
    } else 1

    val cost = amount * ev.costOfTicket

    if (!Economy.hasEnough(sender.name, cost.toDouble())) {
        sendMessageHandler("raffle.error.no-money")
        return true
    }

    if (!ev.isAboveLimit(sender.name, amount)) {
        Economy.subtract(sender.name, cost.toDouble())
        ev.enterUser(sender.name, amount)

        sendMessageHandler("raffle.buy.default", amount)
    } else {
        sendMessageHandler("raffle.error.over-limit", amount, ev.limit)
    }

    return true
}