package dev.shog.osmpl.util.commands

import com.earth2me.essentials.Util
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

private val LOC_RANGE = 100..100_000

private fun findLocation(world: World): Location {
    val xCoord = LOC_RANGE.random().toDouble()
    val zCoord = LOC_RANGE.random().toDouble()

    val location = Location(world, xCoord, 128.00, zCoord)

    val safeLocation =  Util.getSafeDestination(location)

    world.getChunkAt(safeLocation).load(true)

    return safeLocation
}

val WILD_COMMAND = Command.make("wild") {
    if (sender !is Player) {
        sendMessageHandler("wild.not-player")
        return@make true
    }

    val user = DataManager.getUserData(sender.name) ?: return@make true

    if (System.currentTimeMillis() - user.lastWild >= TimeUnit.DAYS.toMillis(1)) {
        user.lastWild = System.currentTimeMillis()
    } else {
        sendMessageHandler("wild.tries")
        return@make true
    }

    sendMessageHandler("wild.teleporting")

    sender.teleport(findLocation(sender.world))
}