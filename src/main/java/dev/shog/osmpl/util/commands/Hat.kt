package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The hat command.
 *
 * /hat -> Sets block in hand to your head.
 */
internal val HAT_COMMAND = Command.make("hat") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    val inv = sender.inventory

    if (inv.itemInHand.type != Material.AIR) {
        val hand = inv.itemInHand.clone()

        if (inv.helmet != null && inv.helmet.type != Material.AIR) {
            inv.itemInHand = inv.helmet
        } else inv.itemInHand = null

        inv.helmet = hand

        sendMessageHandler("hat.success")
    } else sendMessageHandler("hat.error")

    true
}