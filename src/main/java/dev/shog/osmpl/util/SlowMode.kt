package dev.shog.osmpl.util

import dev.shog.osmpl.api.OsmModule

/**
 * Manages slow-mode
 */
internal class SlowMode(private val osm: OsmModule) {
    /**
     * If slowmode is enabled.
     */
    var enabled = false
        set(value) {
            field = value

            if (value)
                osm.pl.server.broadcastMessage(osm.messageContainer.getMessage("slowmode.announce.enabled", timing / 1000))
            else
                osm.pl.server.broadcastMessage(osm.messageContainer.getMessage("slowmode.announce.disabled"))
        }

    /**
     * The allowed time between messages in milliseconds
     */
    var timing = 5000L
        set(value)  {
            field = value

            if (enabled)
                osm.pl.server.broadcastMessage(osm.messageContainer.getMessage("slowmode.announce.timing", value / 1000))
        }
}