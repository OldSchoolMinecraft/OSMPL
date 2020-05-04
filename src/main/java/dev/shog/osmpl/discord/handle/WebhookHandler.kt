package dev.shog.osmpl.discord.handle

import kong.unirest.Unirest
import org.bukkit.util.config.Configuration
import reactor.core.publisher.toMono

/**
 * Manage the webhook
 */
object WebhookHandler {
    /**
     * Invoke for a listener.
     */
    fun invokeForListener(message: String, name: String?, configuration: Configuration) {
        if (name != null) {
                ImageHandler.getUserImage(name)
                    .flatMap { image -> sendMessage(message, name, image, configuration) }
                    .subscribe()
        }
    }

    /**
     * Send a message through the webhook./
     */
    private fun sendMessage(
        message: String,
        username: String,
        image: String,
        cfg: Configuration
    ) = Unirest.post(cfg.getString("botWebhook"))
            .header("Content-Type", "application/json")
            .body(getJsonObject(username, image, message))
            .asEmptyAsync()
            .toMono()

    private fun getJsonObject(username: String, image: String, content: String): String =
        "{\"username\": \"$username\", \"avatar_url\": \"$image\", \"tts\": \"false\", \"content\": \"$content\"}"
}