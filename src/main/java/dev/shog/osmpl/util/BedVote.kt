package dev.shog.osmpl.util

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.util.commands.STAFF_PREVIOUS_STATE
import dev.shog.osmpl.util.commands.canSleep
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerListener
import kotlin.math.floor

internal val sleepingPlayers = ArrayList<Player>()

fun getSleepingData(server: Server): Triple<Int, Int, Int> {
    val playerCount = server.onlinePlayers.size - STAFF_PREVIOUS_STATE.keys.size

    return Triple(
        server.onlinePlayers.count { player -> player.isSleeping },
        floor(playerCount * REQUIRED_SLEEP_PERCENTAGE).toInt(),
        playerCount
    )
}

const val REQUIRED_SLEEP_PERCENTAGE = 0.25

internal class BedEvents(private val osmPl: OsmModule) : PlayerListener() {
    /**
     * When the player enters a bed.
     */
    override fun onPlayerBedEnter(event: PlayerBedEnterEvent?) {
        if (event != null) {
            val server = osmPl.pl.server

            val (sleepCount, requiredCount, playerCount) = getSleepingData(server)

            if (sleepCount >= requiredCount) {
                server.getWorld("world").time = 1000

                server.broadcastMessage(osmPl.messageContainer.getMessage(
                    "sleep.complete",
                    sleepCount + 1,
                    playerCount
                ))
            } else {
                server.broadcastMessage(osmPl.messageContainer.getMessage(
                    "sleep.part",
                    event.player.name,
                    requiredCount.toString()
                ))
            }
        }
    }

    /**
     * When the player leaves a bed
     */
    override fun onPlayerBedLeave(event: PlayerBedLeaveEvent?) {
        if (event != null) {
            val server = osmPl.pl.server

            if (canSleep(server.getWorld("world"))) {
                val (_, requiredCount, _) = getSleepingData(server)

                server.broadcastMessage(osmPl.messageContainer.getMessage(
                    "sleep.left",
                    event.player.name,
                    requiredCount.toString()
                ))
            }
        }
    }
}
