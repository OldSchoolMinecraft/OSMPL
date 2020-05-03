package dev.shog.osmpl.events

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerListener

/**
 * Disable lava
 */
internal val BLOCK_PLACE = { osm: OsmPl ->
    osm.server.pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, object : PlayerListener() {
        override fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent?) {
            if (event != null && event.bucket == Material.LAVA_BUCKET && !event.player.hasPermissionOrOp("osm.bypasslava")) {
                event.isCancelled = true
            }
        }
    }, Event.Priority.Normal, osm)
}