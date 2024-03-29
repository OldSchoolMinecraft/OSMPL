package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.discord.handle.WebhookHandler
import dev.shog.osmpl.util.UtilModule
import dev.shog.osmpl.util.commands.applyRainbow
import dev.shog.osmpl.util.commands.colorful
import kotlinx.coroutines.runBlocking
import org.bukkit.ChatColor
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerListener
import ru.tehkode.permissions.bukkit.PermissionsEx
import java.util.concurrent.ConcurrentHashMap

internal val LAST_SENT_MESSAGE = ConcurrentHashMap<String, Long>()

/**
 * Player chat.
 */
internal val PLAYER_CHAT = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, object : PlayerListener() {
        override fun onPlayerChat(event: PlayerChatEvent?) {
            if (event != null) {
                LAST_SENT_MESSAGE[event.player.name.toLowerCase()] = System.currentTimeMillis()

                val user = PermissionsEx.getPermissionManager().getUser(event.player)

                event.format = "%2\$s"

                val prefix = user.getPrefix("world").replace("&", "§")
                val suffix = user.getSuffix("world").replace("&", "§")

                if (event.player.hasPermission("osm.coloredchat") || event.player.isOp)
                    event.message = event.message.replace("&", "§")
                else
                    event.message = ChatColor.stripColor(event.message)

                /*runBlocking {
                    WebhookHandler.sendDiscordMessage(event.player, ChatColor.stripColor(event.message))
                }*/

                val name = if (colorful.contains(event.player.name.toLowerCase()))
                    applyRainbow(event.player.displayName)
                else event.player.displayName

                event.message = osm.messageContainer.getMessage(
                        "chat.default",
                        prefix,
                        name.replace("&", "§"),
                        suffix,
                        event.message
                )
            }
        }
    }, Event.Priority.Normal, osm.pl)
}