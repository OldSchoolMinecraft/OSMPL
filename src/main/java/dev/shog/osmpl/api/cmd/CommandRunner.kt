package dev.shog.osmpl.api.cmd

import dev.shog.osmpl.api.OsmPlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Add executors to an [osmPlugin]
 */
class CommandRunner(private val osmPlugin: OsmPlugin) {
    /**
     * Get the [CommandContext].
     */
    private fun getContext(sender: CommandSender, args: Array<out String>, cmd: Command): CommandContext =
            CommandContext(sender, args.toList(), cmd, osmPlugin, osmPlugin.defaultMessageContainer)

    // Add the commands to [osmPlugin]
    init {
        osmPlugin.commands.forEach { osmCmd ->
            osmPlugin.getCommand(osmCmd.name).executor = CommandExecutor { sender, cmd, _, args ->
                osmCmd.execute(getContext(sender, args, cmd))
            }
        }
    }
}