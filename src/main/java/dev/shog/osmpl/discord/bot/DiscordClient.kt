package dev.shog.osmpl.discord.bot

import com.oldschoolminecraft.vanish.Invisiman
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.discord.handle.WebhookHandler
import dev.shog.osmpl.generateRandomString
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
            val str = server.onlinePlayers
                    .filterNot{ player -> Invisiman.instance.isVanished(player) }
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

        /*put("!link") {
            val con = SqlHandler.getConnection(db = "hydra")
            val stmt = con.prepareStatement("INSERT INTO dc_verify (discord_id, code) VALUES (?, ?)");
            val id = message.author?.id?.asString ?: ""
            if (id != "") {
                val code = generateRandomString(6)
                stmt.setString(1, id)
                stmt.setString(2, code)
                if (stmt.execute()) {
                    message.channel.createMessage("Your code has been generated successfully! Login to the server and run `/link ${code}` to complete the process.")
                } else {
                    message.channel.createMessage("An unknown error occurred while committing to the database. If this error persists, please contact an administrator.")
                }
            } else {
                message.channel.createMessage("Unable to get your ID. If this error persists, please contact an administrator.")
            }
        }*/
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