package dev.shog.osmpl.discord.handle

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.data.DataManager
import kong.unirest.Unirest
import me.moderator_man.fo.FakeOnline
import org.bukkit.entity.Player
import reactor.core.publisher.toMono

/**
 * Manage the webhook
 */
object WebhookHandler {
    /**
     * Invoke for a listener.
     */
    fun invoke(message: String, name: String) {
        ImageHandler.getUserImage(if (name == "OSM Server") "osm" else name)
                .flatMap { image -> sendMessage(message, name, image, OsmPl.discordLink.config.content.getString("url")) }
                .subscribe()
    }

    /**
     * Send the message.
     */
    fun sendDiscordMessage(player: Player, message: String) {
        if (!FakeOnline.instance.um.isAuthenticated(player.name) || DataManager.isUserMuted(player.name))
            return

        if (CursedDataHandler.isCursed(message.split(" "))) {
            player.sendMessage(OsmPl.discordLink.messageContainer.getMessage("errors.everyone"))
            return
        }

        invoke(OsmPl.discordLink.messageContainer.getMessage("discord.default", message), player.name)
    }

    /**
     * Send a message through the webhook.
     */
    private fun sendMessage(
        message: String,
        username: String,
        image: String,
        hook: String
    ) = Unirest.post(hook)
            .header("Content-Type", "application/json")
            .body(getJsonObject(username, image, message))
            .asStringAsync()
            .toMono()
            .doOnNext { println(it.isSuccess);println(it.body) }

    private fun getJsonObject(username: String, image: String, content: String): String =
        "{\"username\": \"$username\", \"avatar_url\": \"$image\", \"tts\": \"false\", \"content\": \"$content\"}"
}