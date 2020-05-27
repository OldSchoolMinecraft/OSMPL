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

        args.size == 2 && (args[0].toLowerCase() == "deposit" || args[0].toLowerCase() == "withdraw") -> {
            val amount = args[1]
            val deposit = args[0].toLowerCase() == "deposit"

            if (amount.length >= 2 && amount[0] == '$') {
                val doubleAmount = amount.removePrefix("$").toDoubleOrNull()

                when {
                    doubleAmount == null ->
                        sendMessageHandler("savings.invalid-number")

                    0 >= doubleAmount ->
                        sendMessageHandler("savings.invalid-number")

                    deposit && !Economy.hasEnough(sender.name, doubleAmount) ->
                        sendMessageHandler("savings.not-enough-deposit")

                    !deposit && doubleAmount >= user.savings ->
                        sendMessageHandler("savings.not-enough-withdraw")

                    deposit -> {
                        sendMessageHandler("savings.deposit", doubleAmount)
                        Economy.subtract(sender.name, doubleAmount)

                        user.savings += doubleAmount
                        return@make true
                    }

                    !deposit -> {
                        Economy.add(sender.name, doubleAmount)

                        user.savings -= doubleAmount

                        sendMessageHandler("savings.withdraw", doubleAmount)
                    }

                    else -> return@make false
                }
            }

            return@make true
        }

        user.savings <= 0 ->
            sendMessageHandler("savings.no-savings")

        else ->
            sendMessageHandler("savings.amount", user.savings)
    }

    true
}