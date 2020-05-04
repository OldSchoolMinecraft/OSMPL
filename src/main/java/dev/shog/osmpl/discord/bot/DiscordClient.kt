package dev.shog.osmpl.discord.bot

import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.discord.getProperContent
import dev.shog.osmpl.discord.handle.CursedDataHandler
import dev.shog.osmpl.discord.handle.WebhookHandler
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import me.moderator_man.fo.FakeOnline
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerQuitEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux

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
                    .flatMap { ch -> ch.createMessage(container.getMessage("commands.online", it.onlinePlayers.size.toString())) }
        }

        put("!list") { server ->
            val str = server.onlinePlayers.asSequence()
                    .joinToString { "`${it.name}`" }
                    .trim()
                    .removeSuffix(",")

            message.channel
                    .flatMap { ch -> ch.createMessage(
                            container.getMessage("commands.list",
                                    server.onlinePlayers.size.toString(),
                                    str
                            )) }
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
            .create(osmPl.configuration.getString("botToken"))
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
                        .doOnNext { e -> execCommands(e, osmPl.server).subscribe() }
                        .filter { e -> e.message.channelId.asLong() == osmPl.configuration.getString("botChannel").toLongOrNull() }
                        .filter { e -> !e.message.content.startsWith("!") }
                        .flatMap { e ->
                            getProperContent(e, this@getBot)
                                    .doOnNext { content ->
                                        osmPl.server.broadcastMessage(
                                                container.getMessage("minecraft.default",
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
        osmPl.server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, object : PlayerListener() {
            override fun onPlayerChat(event: PlayerChatEvent?) {
                if (event != null) {
                    if (!FakeOnline.instance.um.isAuthenticated(event.player.name) || DataManager.isUserMuted(event.player.name))
                        return

                    if (CursedDataHandler.isCursed(event.message.split(" "))) {
                        event.player.sendMessage(container.getMessage("errors.everyone"))
                        return
                    }

                    WebhookHandler
                        . invokeForListener(container.getMessage("discord.default", event.message), event.player.name, osmPl.configuration)
                }
            }
        }, Event.Priority.Normal, osmPl)

        osmPl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
            override fun onPlayerJoin(event: PlayerJoinEvent?) {
                WebhookHandler.invokeForListener(container.getMessage("discord.join", event?.player?.name), event?.player?.name, osmPl.configuration)
            }
        }, Event.Priority.Normal, osmPl)

        osmPl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
            override fun onPlayerQuit(event: PlayerQuitEvent?) {
                WebhookHandler.invokeForListener(container.getMessage("discord.leave", event?.player?.name), event?.player?.name, osmPl.configuration)
            }
        }, Event.Priority.Normal, osmPl)

        osmPl.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
            override fun onEntityDeath(event: EntityDeathEvent?) {
                if (event?.entity is Player) {
                    val player = event.entity as Player

                    WebhookHandler.invokeForListener(container.getMessage("discord.death", player.name), player.name, osmPl.configuration)
                }
            }
        }, Event.Priority.Normal, osmPl)
    }

    override fun getClient(): GatewayDiscordClient =
        CLIENT
}