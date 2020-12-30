package dev.shog.osmpl.discord.handle

import kong.unirest.Unirest
import java.util.concurrent.ConcurrentHashMap

object ImageHandler {
    /**
     * Store a user's image so it doesn't spam the API.
     */
    private val userImageCache = ConcurrentHashMap<String, String>()

    /**
     * Get a user's skin, then use minotar to get their head.
     */
    fun getUserImage(name: String): String? {
        if (userImageCache.containsKey(name))
            return userImageCache[name]

        val request = Unirest.get("https://www.oldschoolminecraft.com/getskin.php?username=$name")
            .asString()

        return if (request.isSuccess) {
            val body = request.body

            if (body.startsWith("https://minotar.net/skin/"))
                "https://minotar.net/helm/${body.removePrefix("https://minotar.net/skin/")}/100.png"
            else body
        } else null
    }

}