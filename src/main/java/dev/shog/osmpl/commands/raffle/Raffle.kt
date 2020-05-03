package dev.shog.osmpl.commands.raffle

import dev.shog.osmpl.*
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.commands.impl.PlayerCommand
import dev.shog.osmpl.commands.raffle.seg.raffleBuyCommand
import dev.shog.osmpl.commands.raffle.seg.raffleCreateSegment
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

/**
 * The raffle command.
 */
internal val RAFFLE_COMMAND = Command.make("raffle") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    if (args.isNotEmpty() && args[0].equals("help", true)) {
        if (sender.hasPermissionOrOp("osm.raffle")) {
            sendMultiMessageHandler("raffle.help.admin")
        } else {
            sendMultiMessageHandler("raffle.help.default")
        }

        return@make true
    }

    // Allowed commands while a raffle isn't going on
    when {
        args.size >= 7 && args[0].equals("create", true) && sender.hasPermissionOrOp("osm.raffle") ->
            return@make raffleCreateSegment()

        args.isNotEmpty()
                && RaffleEvent.currentEvent()
                && args[0].equals("end", true)
                && sender.hasPermissionOrOp("osm.raffle") -> {
            RaffleEvent.endEvent(this, false)

            broadcastMessageHandler("raffle.end.broadcast")

            return@make true
        }

        args.isNotEmpty() && RaffleEvent.currentEvent() && args[0].equals("pick-winner", true) && sender.hasPermissionOrOp("osm.raffle") -> {
            RaffleEvent.endEvent(this, true, sender)

            return@make true
        }
    }

    if (!RaffleEvent.currentEvent()) {
        sendMessageHandler("raffle.error.no-raffle")
        return@make true
    }

    val ev = RaffleEvent.CURRENT_EVENT!!

    when {
        // /raffle
        args.isEmpty() -> {
            val amount = ev.getUserAmount(sender.name)

            if (amount <= 0) {
                sendMessageHandler("raffle.online-default.unentered", ev.prize, TimeUnit.MILLISECONDS.toMinutes(ev.delay))
            } else {
                sendMessageHandler("raffle.online-default.entered", ev.prize, TimeUnit.MILLISECONDS.toMinutes(ev.delay), amount, ev.playerLimit)
            }
        }

        // /raffle board
        args.isNotEmpty() && args[0].equals("board", true) -> {
            val total = RaffleEvent.CURRENT_EVENT!!.getTotalPurchasedTickets()

            if (RaffleEvent.CURRENT_EVENT!!.names.isEmpty()) {
                sendMessageHandler("raffle.board.empty")
                return@make true
            }

            sendMultiline(buildString {
                append(messageContainer.getMessage("raffle.board.default"))

                RaffleEvent.CURRENT_EVENT!!.names.keys.forEachIndexed { index, s ->
                    val amount = RaffleEvent.CURRENT_EVENT!!.names[s] ?: 0

                    append(messageContainer.getMessage("raffle.board.entry", index + 1, s, amount, (amount.toDouble() / total.toDouble()).parsePercent()))
                }
            })
        }

        args.isNotEmpty() && args[0].equals("buy", true) ->
            return@make raffleBuyCommand()

        else -> return@make false
    }

    true
}