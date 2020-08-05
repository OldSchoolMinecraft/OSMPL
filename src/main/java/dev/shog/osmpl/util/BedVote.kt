package dev.shog.osmpl.util

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.util.commands.STAFF_PREVIOUS_STATE
import dev.shog.osmpl.util.commands.canSleep
import dev.shog.osmpl.util.commands.disableStaffMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerListener

internal val sleepingPlayers = ArrayList<Player>()

internal class BedEvents(private val osmPl: OsmModule) : PlayerListener() {
    /**
     * When the player enters a bed.
     */
    override fun onPlayerBedEnter(event: PlayerBedEnterEvent?) {
        if (event != null) {
            val server = osmPl.pl.server

            synchronized(sleepingPlayers) {
                if (!sleepingPlayers.contains(event.player))
                    sleepingPlayers.add(event.player)

                val sleeping = sleepingPlayers.size
                val size = server.onlinePlayers.size - STAFF_PREVIOUS_STATE.keys.size

                if (sleeping >= size / 2) {
                    server.getWorld("world").time = 1000

                    server.broadcastMessage(osmPl.messageContainer.getMessage(
                            "sleep.complete",
                            sleeping.toString(),
                            size.toString()
                    ))
                } else {
                    server.broadcastMessage(osmPl.messageContainer.getMessage(
                            "sleep.part",
                            event.player.name, "${size / 2 - sleeping}"
                    ))
                }
            }
        }
    }

    /**
     * When the player leaves a bed
     */
    override fun onPlayerBedLeave(event: PlayerBedLeaveEvent?) {
        if (event != null) {
            val server = osmPl.pl.server

            synchronized(sleepingPlayers) {
                if (sleepingPlayers.contains(event.player))
                    sleepingPlayers.remove(event.player)

                if (canSleep(server.getWorld("world"))) {
                    val size = server.onlinePlayers.size - STAFF_PREVIOUS_STATE.size

                    server.broadcastMessage(osmPl.messageContainer.getMessage(
                            "sleep.left",
                            event.player.name,
                            "${size / 2 - sleepingPlayers.size}"
                    ))
                }
            }
        }
    }
}