package dev.shog.osmpl.handle

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.commands.canSleep
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBedLeaveEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.plugin.java.JavaPlugin

internal val sleepingPlayers = ArrayList<Player>()

internal class BedEvents(private val osmPl: OsmPl) : PlayerListener() {
    /**
     * When the player enters a bed.
     */
    override fun onPlayerBedEnter(event: PlayerBedEnterEvent?) {
        if (event != null) {
           synchronized(sleepingPlayers) {
               if (!sleepingPlayers.contains(event.player))
                   sleepingPlayers.add(event.player)

               val sleeping = sleepingPlayers.size
               val size = osmPl.server.onlinePlayers.size

               if (sleeping >= size / 2) {
                   osmPl.server.getWorld("world").time = 1000

                   osmPl.server.broadcastMessage(osmPl.defaultMessageContainer.getMessage("sleep.complete", sleeping.toString(), size.toString()))
               } else {
                   osmPl.server.broadcastMessage(osmPl.defaultMessageContainer.getMessage("sleep.part", event.player.name, "${size / 2 - sleeping}"))
               }
           }
        }
    }

    /**
     * When the player leaves a bed
     */
    override fun onPlayerBedLeave(event: PlayerBedLeaveEvent?) {
        if (event != null) {
            synchronized(sleepingPlayers) {
                if (sleepingPlayers.contains(event.player))
                    sleepingPlayers.remove(event.player)

                if (canSleep(osmPl.server.getWorld("world"))) {
                    val size = osmPl.server.onlinePlayers.size

                    osmPl.server.broadcastMessage(
                            osmPl.defaultMessageContainer.getMessage("sleep.left", event.player.name, "${size / 2 - sleepingPlayers.size}"))
                }
            }
        }
    }
}