package dev.shog.osmpl.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.handle.sleepingPlayers
import dev.shog.osmpl.sendMessageHandler
import org.bukkit.World

/**
 * View the current sleeping players.
 *
 * /sleeping -> View the current sleeping players.
 */
internal val SLEEPING_COMMAND = Command.make("sleeping") {
    val world = osmPlugin.server.getWorld("world")

    when {
        canSleep(world) -> {
            if (sleepingPlayers.isEmpty())
                sendMessageHandler("sleeping.no-one")
            else {
                sendMessageHandler("sleeping.header", sleepingPlayers
                        .asSequence()
                        .joinToString { messageContainer.getMessage("sleeping.entry", it.name) }
                )
            }
        }

        else -> sendMessageHandler("sleeping.cant-sleep")
    }

    return@make true
}

/**
 * If a player can sleep in [world].
 */
internal fun canSleep(world: World): Boolean =
        (world.isThundering || world.time in 12542..23457) && world.name == "world"