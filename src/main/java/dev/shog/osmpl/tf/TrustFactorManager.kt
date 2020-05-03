package dev.shog.osmpl.tf

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.sendMessage
import dev.shog.osmpl.sendMessageHandler

private val MAPPER = ObjectMapper()

/**
 * View progress
 */
internal val VIEW_PROGRESS = Command.make("vtfp") {
    return@make if (args.size == 1) {
        val obj = when (args[0].toLowerCase()) {
            "break" ->
                TrustFactorHookHandler.breakProgress

            "place" ->
                TrustFactorHookHandler.placeProgress

            "kill" ->
                TrustFactorHookHandler.killProgress

            "move" ->
                TrustFactorHookHandler.moveProgress

            else -> "bruh"
        }

        sendMessage(MAPPER.writeValueAsString(obj))

        true
    } else false
}

/**
 * Manage a user's trust factor
 */
internal val MANAGE_TRUST_FACTOR = Command.make("tfm") {
    return@make when {
        args.size == 1 && args[0].equals("view", true) -> {
            sendMessageHandler("trustfactor.view", sender.name, DefaultTrustFactorHandler.viewTrust(sender.name))
            true
        }

        args.size == 3 -> {
            val manageType = args[0]
            val user = args[1]

            when (manageType.toLowerCase()) {
                "view" -> {
                    if (DataManager.userExists(user)) {
                        sendMessageHandler("trustfactor.view", user, DefaultTrustFactorHandler.viewTrust(user))
                    } else sendMessageHandler("error.user-exists")

                    true
                }

                "take" -> {
                    val amount = args[2].toIntOrNull() ?: return@make false

                    DefaultTrustFactorHandler.removeTrust(user, amount)

                    sendMessageHandler("trustfactor.modify", user, DefaultTrustFactorHandler.viewTrust(user))

                    true
                }

                "give" -> {
                    val amount = args[2].toIntOrNull() ?: return@make false

                    DefaultTrustFactorHandler.giveTrust(user, amount)

                    sendMessageHandler("trustfactor.modify", user, DefaultTrustFactorHandler.viewTrust(user))

                    true
                }

                else -> false
            }
        }

        args.size == 2 -> {
            val manageType = args[0]

            when (manageType.toLowerCase()) {
                "view" -> {
                    if (DataManager.userExists(args[1])) {
                        sendMessageHandler("trustfactor.view", sender.name, DefaultTrustFactorHandler.viewTrust(args[1]))
                    } else sendMessageHandler("error.user-exists")

                    true
                }

                "take" -> {
                    val amount = args[1].toIntOrNull() ?: return@make false

                    DefaultTrustFactorHandler.removeTrust(sender.name, amount)

                    sendMessageHandler("trustfactor.modify", sender.name, DefaultTrustFactorHandler.viewTrust(sender.name))

                    true
                }

                "give" -> {
                    val amount = args[1].toIntOrNull() ?: return@make false

                    DefaultTrustFactorHandler.giveTrust(sender.name, amount)

                    sendMessageHandler("trustfactor.modify", sender.name, DefaultTrustFactorHandler.viewTrust(sender.name))

                    true
                }

                else -> false
            }
        }

        else -> false
    }
}