package dev.shog.osmpl.api

import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.MessageContainer
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An OSM plugin.
 */
abstract class OsmModule(val name: String, val version: Float, val pl: OsmPlugin) {
    /**
     * The default message container for commands.
     */
    abstract val messageContainer: MessageContainer

    /**
     * A list of commands for the [OsmModule].
     */
    val commands = mutableListOf<Command>()

    /**
     * When the module is enabled by [JavaPlugin].
     */
    abstract fun onEnable()

    /**
     * When the module is disabled by [JavaPlugin].
     */
    abstract fun onDisable()

    /**
     * When the module is refreshed.
     */
    abstract fun onRefresh()

    /**
     * Config
     */
    abstract val config: Configuration

    /**
     * Log something and tag the module.
     */
    fun log(log: String) {
        println("[OSMPL:${name}:${version}] $log")
    }
}