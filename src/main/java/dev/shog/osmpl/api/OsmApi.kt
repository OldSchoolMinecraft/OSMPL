package dev.shog.osmpl.api

import kong.unirest.Unirest
import java.util.concurrent.CompletableFuture

/**
 * Interact with OSM properties.
 */
object OsmApi {
    /**
     * Cache of donors.
     */
    var cache: List<String> = listOf()

    /**
     * Get all donors of OSM.
     *
     * @return List of usernames of donors.
     */
    fun getDonors(): CompletableFuture<List<String>> =
            Unirest.get("https://www.oldschoolminecraft.com/donators.php")
                    .asJsonAsync()
                    .handleAsync { http, _ -> http.body.`object`.getJSONArray("donators") }
                    .handleAsync { donors, _ ->
                        val list = donors
                                .toList()
                                .asSequence()
                                .map { any -> any.toString() }
                                .toList()

                        cache = list

                        list
                    }

    /**
     * If the user is a donor.
     *
     * @return If the user is a donor.
     */
    fun isDonor(user: String): Boolean {
        if (cache.isEmpty())
            getDonors().join()

        return cache.contains(user)
    }
}