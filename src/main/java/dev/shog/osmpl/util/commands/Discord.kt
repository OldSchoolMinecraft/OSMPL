package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler

/**
 * The Discord command.
 *
 * /discord -> Discord Link
 */
internal val DISCORD = Command.make("discord") {
    sendMessageHandler("discord", osmModule.config.content.getString("discordLink"))

    true
}