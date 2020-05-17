package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.discord.handle.CursedDataHandler
import dev.shog.osmpl.discord.handle.WebhookHandler
import dev.shog.osmpl.util.UtilModule
import me.moderator_man.fo.FakeOnline
import org.bukkit.ChatColor
import org.bukkit.entity.Player
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
                if (UtilModule.slowMode.enabled && LAST_SENT_MESSAGE.containsKey(event.player.name.toLowerCase())) {
                    val time = LAST_SENT_MESSAGE[event.player.name.toLowerCase()] ?: 0

                    val wait = UtilModule.slowMode.timing

                    if (System.currentTimeMillis() - time < wait && !event.player.hasPermission("osm.slowbypass")) {
                        var calc = (wait - (System.currentTimeMillis() - time))
                        var type = "seconds"

                        if (calc >= 1000)
                            calc /= 1000
                        else type = "milliseconds"

                        event.player.sendMessage("${ChatColor.RED}Please wait $calc more $type before sending another message.")
                        event.isCancelled = true
                        return
                    }
                }

                LAST_SENT_MESSAGE[event.player.name.toLowerCase()] = System.currentTimeMillis()

                val user = PermissionsEx.getPermissionManager().getUser(event.player)

                event.format = "%2\$s"

                val prefix = user.getPrefix("world").replace("&", "§")
                val suffix = user.getSuffix("world").replace("&", "§")

                if (event.player.hasPermission("osm.coloredchat") || event.player.isOp)
                    event.message = event.message.replace("&", "§")
                else event.message = ChatColor.stripColor(event.message)

                WebhookHandler.sendDiscordMessage(event.player, event.message)

                event.message = osm.defaultMessageContainer
                        .getMessage("chat.default", prefix, event.player.displayName, suffix, event.message)
            }
        }
    }, Event.Priority.Normal, osm.pl)
}