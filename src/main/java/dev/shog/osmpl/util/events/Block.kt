package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.hasPermissionOrOp
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockListener
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerListener

/**
 * Disable lava
 */
internal val BLOCK_PLACE = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, object : PlayerListener() {
        override fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent?) {
            if (event != null && event.bucket == Material.LAVA_BUCKET && !event.player.hasPermissionOrOp("osm.bypasslava")) {
                event.isCancelled = true
            }
        }
    }, Event.Priority.Normal, osm.pl)
}