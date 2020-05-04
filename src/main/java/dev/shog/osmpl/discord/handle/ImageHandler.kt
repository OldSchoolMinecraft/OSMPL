package dev.shog.osmpl.discord.handle

import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.util.concurrent.ConcurrentHashMap

object ImageHandler {
    /**
     * Store a user's image so it doesn't spam the API.
     */
    private val userImageCache = ConcurrentHashMap<String, String>()

    /**
     * Get a user's skin, then use minotar to get their head.
     */
    fun getUserImage(name: String): Mono<String> {
        if (userImageCache.containsKey(name))
            return Mono.justOrEmpty(userImageCache[name])

        val url = Unirest.get("https://www.oldschoolminecraft.com/getskin.php?username=$name")
            .asStringAsync()
            .toMono()

        return url
            .filter { res -> res.isSuccess }
            .map { res -> res.body }
            .map {
                if (it.startsWith("https://minotar.net/skin/")) {
                    "https://minotar.net/helm/${it.removePrefix("https://minotar.net/skin/")}/100.png"
                } else it
            }
            .doOnNext { result -> userImageCache[name] = result }
            .switchIfEmpty("https://minotar.net/helm/steve/100.png".toMono())
    }

}