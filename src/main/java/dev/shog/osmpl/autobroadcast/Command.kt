package dev.shog.osmpl.autobroadcast

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.entity.Player

/**
 * /ignorebroadcast
 * Ignores the automatic broadcasts.
 */
val IGNORE_COMMAND = Command.make("ignorebroadcast") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    val user = DataManager.getUserData(sender.name) ?: return@make true

    user.ignoreBroadcast = !user.ignoreBroadcast

    sendMessageHandler("ignore.${!user.ignoreBroadcast}")

    true
}

/**
 * Broadcast [message].
 */
fun broadcastMessage(autoBroadcaster: AutoBroadcaster, message: String) {
    val messageSplit = message.split("\n")

    autoBroadcaster.pl.server.onlinePlayers
        .filter { player -> DataManager.getUserData(player.name)?.ignoreBroadcast == false }
        .forEach { player ->
            messageSplit.forEach { msg ->
                player.sendMessage(msg)
            }
        }
}