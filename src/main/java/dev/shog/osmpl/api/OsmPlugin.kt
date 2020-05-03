package dev.shog.osmpl.api

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.MessageContainer
import org.bukkit.plugin.java.JavaPlugin

/**
 * An OSM plugin.
 */
abstract class OsmPlugin : JavaPlugin() {
    /**
     * The default message container for commands.
     */
    abstract val defaultMessageContainer: MessageContainer

    /**
     * A list of commands for the [OsmPlugin]. This should be initialized before running [CommandRunner].
     */
    val commands = mutableListOf<Command>()
}