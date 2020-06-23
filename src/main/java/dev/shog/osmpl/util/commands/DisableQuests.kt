package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.entity.Player
import kotlin.random.Random

/**
 * The player manager command.
 */
internal val DISABLE_QUESTS = Command.make("disablequests") {
    sendMessageHandler("disablequests.complete")
    true
}