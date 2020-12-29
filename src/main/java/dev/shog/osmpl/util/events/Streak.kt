package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.util.StreakHandler
import dev.shog.osmpl.util.UtilModule
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener

internal val STREAK_INVOKE = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
        override fun onPlayerJoin(event: PlayerJoinEvent?) {
            if (event != null) {
                StreakHandler.handleLogin(event, osm)
            }
        }
    }, Event.Priority.Normal, osm.pl)
}