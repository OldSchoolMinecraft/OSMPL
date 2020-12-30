package dev.shog.osmpl.discord.bot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.discord.handle.WebhookHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.bukkit.ChatColor
import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Get the bot.
 */
internal fun DiscordLink.getBot() = object : IBot {
    /**
     * Discord's commands.
     */
    private val commands = HashMap<String, suspend MessageCreateEvent.(server: Server) -> Unit>().apply {
        put("!online") {
            message.channel.createMessage(
                messageContainer.getMessage("commands.online", it.onlinePlayers.size.toString())
            )
        }

        put("!list") { server ->
            val str = server.onlinePlayers.asSequence()
                    .joinToString { "`${it.name}`" }
                    .trim()
                    .removeSuffix(",")

            message.channel.createMessage(
                messageContainer.getMessage("commands.list",
                    server.onlinePlayers.size.toString(),
                    str
                )
            )
        }
    }

    /**
     * Replace @'s to user name
     */
    suspend fun getProperContent(e: MessageCreateEvent, dl: DiscordLink): String {
        var content = e.message.content

        e.message.mentionedUsers.collect { user ->
            content = content.replace(
                "<@!${user.id.asString}>", dl.messageContainer.getMessage(
                    "mentions.user",
                    user.username,
                    user.discriminator
                )
            )
        }

        return content
    }

    /**
     * Execute Discord commands from [messageCreateEvent].
     */
    private suspend fun execCommands(messageCreateEvent: MessageCreateEvent, server: Server) {
        val content = messageCreateEvent.message.content

        commands.filter { it.key.equals(content, true) }
            .entries
            .firstOrNull()
            ?.value
            ?.invoke(messageCreateEvent, server)
    }

    private suspend fun createClient(): Kord {
        val kord = Kord(config.content.getString("token"))

        kord.on<MessageCreateEvent> {
            val mem = member
            if (mem != null) {
                if (mem.isBot)
                    return@on

                execCommands(this, pl.server)

                println(message.channelId.value)
                println(config.content.getLong("channel"))

                if (message.channelId.value == config.content.getLong("channel") && !message.content.startsWith("!")) {
                    pl.server.broadcastMessage(
                        messageContainer.getMessage("minecraft.default",
                            mem.username,
                            mem.discriminator,
                            ChatColor.stripColor(getProperContent(this, this@getBot))
                        )
                    )
                }
            }
        }

        kord.login {
            playing("with yo momma")
        }

        return kord
    }

    init {
        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
            override fun onPlayerJoin(event: PlayerJoinEvent?) {
                runBlocking {
                    WebhookHandler.invoke(messageContainer.getMessage("discord.join", event?.player?.name), "OSM Server")
                }
            }
        }, Event.Priority.Normal, pl)

        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
            override fun onPlayerQuit(event: PlayerQuitEvent?) {
                runBlocking {
                    WebhookHandler.invoke(messageContainer.getMessage("discord.leave", event?.player?.name), "OSM Server")
                }
            }
        }, Event.Priority.Normal, pl)
    }

    private var CLIENT: Kord? = null

    override suspend fun getClient(): Kord {
        val cli = CLIENT

        if (cli != null) {
            return cli
        }

        val newCli = createClient()

        CLIENT = newCli

        return newCli
    }
}