package dev.shog.osmpl.api.cmd

import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.msg.MessageContainer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

data class CommandContext(
        val sender: CommandSender,
        val args: List<String>,
        val cmd: Command,
        val osmPlugin: OsmPlugin,
        val messageContainer: MessageContainer
)