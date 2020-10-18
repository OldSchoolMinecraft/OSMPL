package dev.shog.osmpl.util.commands

import com.earth2me.essentials.Util
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

private val WILD_LIMIT = hashMapOf<String, Pair<Long, Int>>()
private val LOC_RANGE = 100..100_000

private fun findLocation(world: World): Location {
    val xCoord = LOC_RANGE.random().toDouble()
    val zCoord = LOC_RANGE.random().toDouble()

    val location = Location(world, xCoord, 128.00, zCoord)

    return Util.getSafeDestination(location)
}

val WILD_COMMAND = Command.make("wild") {
    if (sender !is Player) {
        sendMessageHandler("wild.not-player")
        return@make true
    }

    val user = WILD_LIMIT[sender.name.toLowerCase()]

    if (user != null) {
        if (System.currentTimeMillis() - user.first >= TimeUnit.DAYS.toMillis(1))
            WILD_LIMIT[sender.name.toLowerCase()] = System.currentTimeMillis() to 1
        else {
            if (user.second >= 3) {
                sendMessageHandler("wild.tries")
                return@make true
            } else {
                WILD_LIMIT[sender.name.toLowerCase()] = user.first to user.second + 1
            }
        }
    } else
        WILD_LIMIT[sender.name.toLowerCase()] = System.currentTimeMillis() to 1

    sendMessageHandler("wild.teleporting")

    sender.teleport(findLocation(sender.world))
}