package dev.shog.osmpl.discord

import dev.shog.osmpl.OsmPl
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * Replace @'s to user name
 */
internal fun getProperContent(e: MessageCreateEvent, dl: DiscordLink): Mono<String> = e.message.content.toMono()
    .flatMap { cnt ->
        e.message.userMentions
            .collectList()
            .map { list ->
                var repl = cnt

                for (en in list) {
                    repl = repl.replace("<@!${en.id.asLong()}>", dl.container.getMessage("mentions.user", en.username, en.discriminator))
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
                    repl = repl.replace(en.mention, dl.container.getMessage("mentions.channel", en.name))
                }

                repl
            }
    }