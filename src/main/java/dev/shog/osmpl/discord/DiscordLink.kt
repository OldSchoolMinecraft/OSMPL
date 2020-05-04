package dev.shog.osmpl.discord

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.discord.bot.getBot
import dev.shog.osmpl.discord.handle.CursedDataHandler
import dev.shog.osmpl.handle.ConfigHandler
import dev.shog.osmpl.sendMessage
import discord4j.core.GatewayDiscordClient
import org.bukkit.ChatColor

/**
 * Main class
 */
internal class DiscordLink(val osmPl: OsmPl) {
    val client: GatewayDiscordClient
    val container = MessageContainer.fromFile("dl-messages.json")

    init {
        ConfigHandler

        osmPl.commands.add(Command.make("discordlink") {
            sendMessage("${ChatColor.GRAY}DiscordLink from OSMPL")
            true
        })

        osmPl.commands.add(Command.make("cursed") {
            val cursed = CursedDataHandler.getCursed()

            return@make when {
                args.isEmpty() -> {
                    sender.sendMessage(
                            container.getMessage(
                                    "commands.cursed.default", cursed.asSequence().joinToString(", ").removeSuffix(", ")
                            ))

                    true
                }

                args.isNotEmpty() && args.size == 2 -> {
                    when (args[0].toLowerCase()) {
                        "add" -> {
                            val word = args[1]

                            if (cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(container.getMessage("commands.cursed.already-exists"))
                            } else {
                                CursedDataHandler.addCursed(word)
                                sender.sendMessage(container.getMessage("commands.cursed.added", word))
                            }

                            true
                        }

                        "remove" -> {
                            val word = args[1]

                            if (!cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(container.getMessage("commands.cursed.doesnt-exist"))
                            } else {
                                CursedDataHandler.removeCursed(word)
                                sender.sendMessage(container.getMessage("commands.cursed.removed", word))
                            }

                            true
                        }

                        else -> false
                    }
                }

                else -> false
            }
        })


        client = getBot().getClient()
    }
}