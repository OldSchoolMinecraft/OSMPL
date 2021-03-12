package dev.shog.osmpl.discord.handle

import com.oldschoolminecraft.osas.OSAS
import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.data.DataManager
import kong.unirest.Unirest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.entity.Player

/**
 * Manage the webhook
 */
object WebhookHandler {
    /**
     * Invoke for a listener.
     */
    suspend fun invoke(message: String, name: String) {
//        val image = ImageHandler.getUserImage(if (name == "OSM Server") "osm" else name)

        GlobalScope.launch {
            sendMessage(
                message.replace(Regex("<((@!?\\d+)|(:.+?:\\d+)|(&\\d+))>"), ""),
                name,
//                image ?: "",
                OsmPl.discordLink.config.content.getString("url")
            ).join()
        }
    }

    /**
     * Send the message.
     */
    suspend fun sendDiscordMessage(player: Player, message: String) {
        if (!OSAS.instance.fallbackManager.isAuthenticated(player.name) || DataManager.isUserMuted(player.name))
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
//            image: String,
            hook: String
    ) = Unirest.post(hook)
            .header("Content-Type", "application/json")
            .body(getJsonObject(username, message))
//        .body(getJsonObject(username, image, message))
            .asStringAsync()

    private fun getJsonObject(
        username: String,
//        image: String,
        content: String
    ): String =
        "{\"username\": \"$username\", \"tts\": \"false\", \"content\": \"$content\"}"
//            "{\"username\": \"$username\", \"avatar_url\": \"$image\", \"tts\": \"false\", \"content\": \"$content\"}"
}