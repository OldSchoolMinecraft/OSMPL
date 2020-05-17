package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.either
import dev.shog.osmpl.api.msg.sendMessageHandler

/**
 * The donate command.
 */
internal val DONATE_COMMAND = Command.make("donate") {
    OsmApi.isDonor(sender.name)
            .handleAsync { es, _ ->
                val link = es.either("donate.already-donated", "donate.not-donated")

                sendMessageHandler(link)
            }
            .get()

    true
}