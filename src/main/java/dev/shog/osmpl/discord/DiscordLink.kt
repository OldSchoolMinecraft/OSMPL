package dev.shog.osmpl.discord

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.api.msg.sendMessage
import dev.shog.osmpl.discord.bot.getBot
import dev.shog.osmpl.discord.handle.CursedDataHandler
import discord4j.core.GatewayDiscordClient
import org.bukkit.ChatColor

/**
 * Main class
 */
internal class DiscordLink(pl: OsmPlugin): OsmModule("DiscordLink", 1.0F, pl) {
    override val config: Configuration = Configuration(this)
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/dl.json")

    companion object {
        lateinit var client: GatewayDiscordClient
    }

    override fun onEnable() {
        if (config.anyBlank()) {
            if (!config.has("token", "channel", "url")) {
                config.content.put("token", "")
                config.content.put("channel", "")
                config.content.put("url", "")
            }

            System.err.println("[OSMPL:DL] The config is not properly filled out!")

            config.save()
            pl.disableModule(this)

            return
        }

        commands.add(Command.make("discordlink") {
            sendMessage("${ChatColor.GRAY}DiscordLink from OSMPL")
            true
        })

        commands.add(Command.make("cursed") {
            val cursed = CursedDataHandler.getCursed()

            return@make when {
                args.isEmpty() -> {
                    sender.sendMessage(
                            this@DiscordLink.messageContainer.getMessage(
                                    "commands.cursed.default", cursed.asSequence().joinToString(", ").removeSuffix(", ")
                            ))

                    true
                }

                args.isNotEmpty() && args.size == 2 -> {
                    when (args[0].toLowerCase()) {
                        "add" -> {
                            val word = args[1]

                            if (cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(this@DiscordLink.messageContainer.getMessage("commands.cursed.already-exists"))
                            } else {
                                CursedDataHandler.addCursed(word)
                                sender.sendMessage(this@DiscordLink.messageContainer.getMessage("commands.cursed.added", word))
                            }

                            true
                        }

                        "remove" -> {
                            val word = args[1]

                            if (!cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(this@DiscordLink.messageContainer.getMessage("commands.cursed.doesnt-exist"))
                            } else {
                                CursedDataHandler.removeCursed(word)
                                sender.sendMessage(this@DiscordLink.messageContainer.getMessage("commands.cursed.removed", word))
                            }

                            true
                        }

                        else -> false
                    }
                }

                else -> false
            }
        })


        pl.server.scheduler.scheduleAsyncDelayedTask(pl) {
            client = getBot().getClient()
        }
    }

    override fun onDisable() {
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
    }
}