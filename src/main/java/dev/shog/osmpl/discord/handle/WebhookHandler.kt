package dev.shog.osmpl.discord.handle

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import kong.unirest.Config
import kong.unirest.Unirest
import me.moderator_man.fo.FakeOnline
import org.bukkit.entity.Player
import org.bukkit.util.config.Configuration
import reactor.core.publisher.toMono

/**
 * Manage the webhook
 */
object WebhookHandler {
    /**
     * Invoke for a listener.
     */
    fun invokeForListener(message: String, name: String?, hook: String) {
        if (name != null) {
                ImageHandler.getUserImage(name)
                    .flatMap { image -> sendMessage(message, name, image, hook) }
                    .subscribe()
        }
    }

    /**
     * Send the message.
     */
    fun sendDiscordMessage(player: Player, message: String) {
        if (!FakeOnline.instance.um.isAuthenticated(player.name) || DataManager.isUserMuted(player.name))
            return

        if (CursedDataHandler.isCursed(message.split(" "))) {
            player.sendMessage(OsmPl.discordLink.defaultMessageContainer.getMessage("errors.everyone"))
            return
        }

        invokeForListener(
                OsmPl.discordLink.defaultMessageContainer.getMessage("discord.default", message),
                player.name,
                OsmPl.discordLink.config.content.getString("url")
        )
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
            .asEmptyAsync()
            .toMono()

    private fun getJsonObject(username: String, image: String, content: String): String =
        "{\"username\": \"$username\", \"avatar_url\": \"$image\", \"tts\": \"false\", \"content\": \"$content\"}"
}