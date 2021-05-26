package dev.shog.osmpl.joinsplus

import dev.shog.osmpl.translateAlternateColorCodes
import dev.shog.osmpl.util.StreakHandler
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * When a player joins, change their join message.
 */
fun onPlayerJoin(event: PlayerJoinEvent) {
    val msg = loadMessage(event.player.name) ?: Message()

    event.joinMessage = translateAlternateColorCodes(
        '&',
        translateVariables(msg.join, event.player)
    )
}

/**
 * When a player leaves, change their leave message.
 */
fun onPlayerQuit(event: PlayerQuitEvent) {
    val msg = loadMessage(event.player.name) ?: Message()

    event.quitMessage = translateAlternateColorCodes(
        '&',
        translateVariables(msg.quit, event.player)
    )
}

/**
 * Translate [player] into [message].
 */
fun translateVariables(message: String, player: Player): String {
    var newMessage = message.replace("%player%", player.name)

    when {
        message.contains("%streak%") ->
            newMessage = newMessage.replace("%streak%", StreakHandler.getStreak(player.name).toString())
    }

    return newMessage
}