package dev.shog.osmpl.discord.bot

import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.discord.handle.WebhookHandler
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerQuitEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

/**
 * Get the bot.
 */
internal fun DiscordLink.getBot() = object : IBot {
    /**
     * Discord's commands.
     */
    private val commands = HashMap<String, MessageCreateEvent.(server: Server) -> Mono<*>>().apply {
        put("!online") {
            message.channel
                    .flatMap { ch ->
                        ch.createMessage(messageContainer.getMessage("commands.online", it.onlinePlayers.size.toString()))
                    }
        }

        put("!list") { server ->
            val str = server.onlinePlayers.asSequence()
                    .joinToString { "`${it.name}`" }
                    .trim()
                    .removeSuffix(",")

            message.channel
                    .flatMap { ch -> ch.createMessage(
                            messageContainer.getMessage("commands.list",
                                    server.onlinePlayers.size.toString(),
                                    str
                            )) }
        }
    }

    /**
     * Replace @'s to user name
     */
    fun getProperContent(e: MessageCreateEvent, dl: DiscordLink): Mono<String> = e.message.content.toMono()
            .flatMap { cnt ->
                e.message.userMentions
                        .collectList()
                        .map { list ->
                            var repl = cnt

                            for (en in list) {
                                repl = repl.replace("<@!${en.id.asLong()}>", dl.messageContainer.getMessage(
                                        "mentions.user",
                                        en.username,
                                        en.discriminator
                                ))
                            }

                            repl
                        }
            }
            .flatMap { cnt ->
                e.message.roleMentions
                        .collectList()
                        .map { list ->
                            var repl = cnt

                            for (en in list) {
                                repl = repl.replace(en.mention, dl.messageContainer.getMessage(
                                        "mentions.channel",
                                        en.name
                                ))
                            }

                            repl
                        }
            }

    /**
     * Execute Discord commands from [messageCreateEvent].
     */
    private fun execCommands(messageCreateEvent: MessageCreateEvent, server: Server): Mono<*> {
        val content = messageCreateEvent.message.content

        return commands
                .toList()
                .toFlux()
                .filter { obj -> content == obj.first }
                .singleOrEmpty()
                .flatMap { command -> command.second.invoke(messageCreateEvent, server) }
    }

    private val CLIENT: GatewayDiscordClient = DiscordClient
            .create(config.content.getString("token"))
            .gateway()
            .login()
            .doOnNext {
                it.on(MessageCreateEvent::class.java)
                        .filter { e -> e.message.author.isPresent && e.member.isPresent }
                        .filterWhen { e ->
                            e.client.self
                                    .map { user -> user.id }
                                    .map { id -> e.member.get().id != id }
                        }
                        .doOnNext { e -> execCommands(e, pl.server).subscribe() }
                        .filter { e -> e.message.channelId.asLong() == config.content.getLong("channel") }
                        .filter { e -> !e.message.content.startsWith("!") }
                        .flatMap { e ->
                            getProperContent(e, this@getBot)
                                    .doOnNext { content ->
                                        pl.server.broadcastMessage(
                                                messageContainer.getMessage("minecraft.default",
                                                        e.member.get().username,
                                                        e.member.get().discriminator,
                                                        content
                                                )
                                        )
                                    }
                        }
                        .subscribe()
            }
            .block()!!

    init {
        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
            override fun onPlayerJoin(event: PlayerJoinEvent?) {
                WebhookHandler.invoke(messageContainer.getMessage("discord.join", event?.player?.name), "OSM Server")
            }
        }, Event.Priority.Normal, pl)

        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
            override fun onPlayerQuit(event: PlayerQuitEvent?) {
                WebhookHandler.invoke(messageContainer.getMessage("discord.leave", event?.player?.name), "OSM Server")
            }
        }, Event.Priority.Normal, pl)
    }

    override fun getClient(): GatewayDiscordClient =
        CLIENT
}