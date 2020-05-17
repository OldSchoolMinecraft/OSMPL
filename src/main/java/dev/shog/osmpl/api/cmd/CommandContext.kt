package dev.shog.osmpl.api.cmd

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.msg.MessageContainer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

data class CommandContext(
        val sender: CommandSender,
        val args: List<String>,
        val cmd: Command,
        val osmModule: OsmModule,
        val messageContainer: MessageContainer
)