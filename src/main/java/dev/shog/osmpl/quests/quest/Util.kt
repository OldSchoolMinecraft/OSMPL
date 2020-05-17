package dev.shog.osm.quest

import org.bukkit.command.CommandSender

/**
 * Send a multi-line message.
 */
fun CommandSender.sendMultiline(message: String) {
    message.split("\n")
        .forEach { msg -> this.sendMessage(msg) }
}