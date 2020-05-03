package dev.shog.osmpl.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.sendMessageHandler

/**
 * The Discord command.
 *
 * /discord -> Discord Link
 */
internal val DISCORD = Command.make("discord") {
    sendMessageHandler("discord", osmPlugin.configuration.getString("discordLink"))

    true
}