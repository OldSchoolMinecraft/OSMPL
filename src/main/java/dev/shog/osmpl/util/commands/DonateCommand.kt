package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.either
import dev.shog.osmpl.api.msg.sendMessageHandler

/**
 * The donate command.
 */
internal val DONATE_COMMAND = Command.make("donate") {
    val link = OsmApi.isDonor(sender.name).either("donate.already-donated", "donate.not-donated")

    sendMessageHandler(link)
    true
}