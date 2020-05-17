package dev.shog.osmpl.util.commands.impl

import dev.shog.osmpl.OsmPl
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * A default OSM command.
 *
 * @param name The name of the command
 */
internal abstract class OsmCommand(val name: String) {
    abstract fun invoke(osm: OsmPl, args: Array<String>, cmd: Command, sender: CommandSender): Boolean

    /**
     * Get a [CommandExecutor].
     */
    fun getCommandExecutor(osm: OsmPl) = CommandExecutor { sender: CommandSender, cmd: Command, _, args: Array<String> ->
        invoke(osm, args, cmd, sender)
    }

    companion object {
        /**
         * Create OsmCommand
         */
        fun createCommand(name: String, exec: OsmPl.(args: Array<String>, cmd: Command, sender: CommandSender) -> Boolean): OsmCommand {
            return object : OsmCommand(name) {
                override fun invoke(osm: OsmPl, args: Array<String>, cmd: Command, sender: CommandSender): Boolean {
                    return exec.invoke(osm, args, cmd, sender)
                }
            }
        }
    }
}

/**
 * A player command.
 *
 * @param name The name of the command.
 */
internal open class PlayerCommand(
        name: String,
        private val exec: OsmPl.(args: Array<String>, cmd: Command, player: Player) -> Boolean
) : OsmCommand(name) {
    override fun invoke(osm: OsmPl, args: Array<String>, cmd: Command, sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command is only available to players!")
        } else exec.invoke(osm, args, cmd, sender)

        return true
    }

    companion object {
        /**
         * Create a PlayerCommand
         */
        fun createCommand(name: String, exec: OsmPl.(args: Array<String>, cmd: Command, player: Player) -> Boolean) =
                PlayerCommand(name, exec)
    }
}

/**
 * A command that returns a string to send to the player.
 *
 * @param name The name of the command.
 * @param exec A lambda to return the string.
 */
internal open class StringCommand(
        name: String,
        private val exec: OsmPl.(args: Array<String>, cmd: Command, sender: CommandSender) -> String
) : OsmCommand(name) {
    override fun invoke(osm: OsmPl, args: Array<String>, cmd: Command, sender: CommandSender): Boolean {
        val exc = exec.invoke(osm, args, cmd, sender)

        sender.sendMessage(exc)

        return true
    }

    companion object {
        /**
         * Create a StringCommand
         */
        fun createCommand(name: String, exec: OsmPl.(args: Array<String>, cmd: Command, sender: CommandSender) -> String) =
                StringCommand(name, exec)
    }
}