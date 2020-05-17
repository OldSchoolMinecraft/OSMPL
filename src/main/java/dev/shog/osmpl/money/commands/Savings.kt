package dev.shog.osmpl.money.commands

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.money.user.BankUser
import org.bukkit.entity.Player

val SAVINGS_COMMAND = Command.make("savings") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    val user = BankUser.getUser(sender.name)

    when {
        user.bank == -1 ->
            sendMessageHandler("savings.no-bank")

        args.size == 2 && args[0] == "deposit" -> {
            val amount = args[1]

            if (amount.length >= 2 && amount[0] == '$') {
                val doubleAmount = amount.removePrefix("$").toDoubleOrNull()

                if (doubleAmount != null) {
                    val econ = Economy.hasEnough(sender.name, doubleAmount)

                    if (econ) {
                        sendMessageHandler("savings.deposit", doubleAmount)
                        Economy.subtract(sender.name, doubleAmount)

                        user.savings += doubleAmount
                        return@make true
                    } else {
                        sendMessageHandler("savings.not-enough-deposit")
                        return@make true
                    }
                }
            }

            sendMessageHandler("savings.invalid-number")
            return@make true
        }

        args.size == 2 && args[0] == "withdraw" -> {
            val amount = args[1]

            if (amount.length >= 2 && amount[0] == '$') {
                val doubleAmount = amount.removePrefix("$").toDoubleOrNull()

                if (doubleAmount != null) {
                    if (user.savings >= doubleAmount) {
                        sendMessageHandler("savings.withdraw")
                        Economy.add(sender.name, doubleAmount)

                        user.savings -= doubleAmount
                        return@make true
                    } else {
                        sendMessageHandler("savings.not-enough-withdraw")
                        return@make true
                    }
                }
            }

            sendMessageHandler("savings.invalid-number")
            return@make true
        }

        user.savings <= 0 ->
            sendMessageHandler("savings.no-savings")

        else ->
            sendMessageHandler("savings.amount", user.savings)
    }

    true
}