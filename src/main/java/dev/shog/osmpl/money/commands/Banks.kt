package dev.shog.osmpl.money.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiline
import dev.shog.osmpl.fancyDate
import dev.shog.osmpl.money.BankHandler
import dev.shog.osmpl.money.user.BankUser
import dev.shog.osmpl.parsePercent
import org.bukkit.entity.Player
import java.text.DecimalFormat

private val decimalFormat = DecimalFormat("#%")

val BANKS = Command.make("banks") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    val user = BankUser.getUser(sender.name)

    when {
        user.bank == -1 && args.isEmpty() ->
            sendMessageHandler("banks.how-to", BankHandler.transferFee)

        user.bank != -1 && args.isEmpty() ->
            sendMessageHandler("banks.current", BankHandler.banks[user.bank].name)

        args.size == 1 && args[0].equals("list", true) ->
            sendMultiline(buildString {
                BankHandler.banks.forEachIndexed { i, bank ->
                    if (i == 0) {
                        append("§8#${bank.id + 1}§7: §8${bank.name}")
                    } else {
                        append("\n§8#${bank.id + 1}§7: §8${bank.name}")
                    }
                }
            })

        args.size == 2 && args[0].equals("about", true) -> {
            val bank = args[1].toIntOrNull()

            if (bank == null || bank > BankHandler.banks.size) {
                sendMessageHandler("banks.invalid")
                return@make true
            }

            val bankData = BankHandler.banks[bank - 1]

            sendMessageHandler(
                    "banks.about",
                    bankData.name,
                    bankData.savingsInterest.parsePercent(),
                    bankData.savingsInterval.fancyDate()
            )
        }

        args.size == 2 && args[0].equals("join", true) -> {
            val bank = args[1].toIntOrNull()

            if (bank == null || bank > BankHandler.banks.size) {
                sendMessageHandler("banks.invalid")
                return@make true
            }

            if (user.bank == -1) {
                val bankData = BankHandler.banks[bank - 1]

                user.bank = bankData.id
                user.savings = 0.0

                sendMessageHandler("banks.joined", bankData.name)
            } else {
                sendMessageHandler("banks.transfer", BankHandler.transferFee)
            }
        }

        args.size == 2 && args[0].equals("transfer", true) -> {
            val bank = args[1].toIntOrNull()

            if (bank == null || bank > BankHandler.banks.size) {
                sendMessageHandler("banks.invalid")
                return@make true
            }

            if (user.bank != -1) {
                val bankData = BankHandler.banks[bank - 1]

                user.bank = bankData.id

                sendMessageHandler("banks.transfer-s", bankData.name)
            } else {
                sendMessageHandler("banks.no-transfer", BankHandler.transferFee)
            }
        }

        else -> return@make false
    }

    true
}