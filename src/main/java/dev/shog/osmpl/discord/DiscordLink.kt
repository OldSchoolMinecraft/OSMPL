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
internal class DiscordLink(pl: OsmPlugin): OsmModule("discordlink", 1.0F, pl) {
    override val defaultMessageContainer: MessageContainer = MessageContainer.fromFile("messages/dl.json")

    companion object {
        lateinit var client: GatewayDiscordClient
    }

    override fun onEnable() {
        if (!config.has("token", "channel", "url")) {
            config.content.put("token", "Paste Discord bot token here :)")
            config.content.put("channel", "Paste #minecraft-chat channel ID here :)")
            config.content.put("url", "Paste the #minecraft-chat webhook here :)")

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
                            defaultMessageContainer.getMessage(
                                    "commands.cursed.default", cursed.asSequence().joinToString(", ").removeSuffix(", ")
                            ))

                    true
                }

                args.isNotEmpty() && args.size == 2 -> {
                    when (args[0].toLowerCase()) {
                        "add" -> {
                            val word = args[1]

                            if (cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(defaultMessageContainer.getMessage("commands.cursed.already-exists"))
                            } else {
                                CursedDataHandler.addCursed(word)
                                sender.sendMessage(defaultMessageContainer.getMessage("commands.cursed.added", word))
                            }

                            true
                        }

                        "remove" -> {
                            val word = args[1]

                            if (!cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(defaultMessageContainer.getMessage("commands.cursed.doesnt-exist"))
                            } else {
                                CursedDataHandler.removeCursed(word)
                                sender.sendMessage(defaultMessageContainer.getMessage("commands.cursed.removed", word))
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

    override val config: Configuration = Configuration(this)
}