package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player

val colors = listOf(
        ChatColor.RED,
        ChatColor.GOLD,
        ChatColor.YELLOW,
        ChatColor.GREEN,
        ChatColor.BLUE,
        ChatColor.DARK_BLUE,
        ChatColor.DARK_RED
)

val colorful = mutableListOf<String>()

/**
 * The player manager command.
 */
internal val RAINBOW = Command.make("rainbow") {
    if (sender !is Player)
        return@make true

    val name = sender.name.toLowerCase()

    if (colorful.contains(name)) {
        colorful.remove(name)
        sendMessageHandler("rainbow.remove-complete")
    } else {
        colorful.add(name)
        sendMessageHandler("rainbow.complete")
    }

    true
}

fun applyRainbow(displayName: String): String {
    val name = ChatColor.stripColor(displayName)

    var colorIndex = -1
    var newName = ""

    for (c in name) {
        colorIndex++

        if (colorIndex >= colors.size)
            colorIndex = 0

        newName += "${colors[colorIndex]}$c"
    }

    return newName
}