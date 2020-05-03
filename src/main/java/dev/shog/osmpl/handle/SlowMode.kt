package dev.shog.osmpl.handle

import dev.shog.osmpl.OsmPl

/**
 * Manages slow-mode
 */
internal class SlowMode(private val osm: OsmPl) {
    /**
     * If slowmode is enabled.
     */
    var enabled = false
        set(value) {
            field = value

            if (value)
                osm.server.broadcastMessage(osm.defaultMessageContainer.getMessage("slowmode.announce.enabled", timing / 1000))
            else
                osm.server.broadcastMessage(osm.defaultMessageContainer.getMessage("slowmode.announce.disabled"))
        }

    /**
     * The allowed time between messages in milliseconds
     */
    var timing = 5000L
        set(value)  {
            field = value

            if (enabled)
                osm.server.broadcastMessage(osm.defaultMessageContainer.getMessage("slowmode.announce.timing", value / 1000))
        }
}