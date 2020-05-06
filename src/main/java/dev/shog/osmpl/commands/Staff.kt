package dev.shog.osmpl.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.either
import dev.shog.osmpl.sendMessage
import dev.shog.osmpl.sendMessageHandler
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

/**
 * The staff command.
 *
 * /staff -> Toggle staff mode.
 * /staff view -> View people in staff mode.
 */
internal val STAFF_COMMAND = Command.make("staff") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    when {
        args.isEmpty() -> {
            STAFF_PREVIOUS_STATE.containsKey(sender.name.toLowerCase())
                    .either(disableStaffMode(), enableStaffMode())
        }

        args.size == 1 && args[0].equals("view", true) -> {
            val prevStr = STAFF_PREVIOUS_STATE
                    .asSequence()
                    .joinToString { messageContainer.getMessage("staff-mode.view-entry", it) }

            sendMessageHandler("staff-mode.view-header", prevStr)
        }

        else -> return@make false
    }

    return@make true
}

/**
 * When enabling staff mode, this is that player's previous inventory.
 */
private val STAFF_PREVIOUS_STATE = ConcurrentHashMap<String, Pair<Array<ItemStack>, Array<ItemStack>>>()

/**
 * Enable a staff mode for [player].
 */
internal fun CommandContext.enableStaffMode() {
    require(sender is Player)

    STAFF_PREVIOUS_STATE[sender.name.toLowerCase()] = Pair(sender.inventory.contents, sender.inventory.armorContents)

    sender.inventory.contents = Array(36) { ItemStack(0) }
    sender.inventory.armorContents = Array(4) { ItemStack(0) }

    sender.inventory.addItem(
            ItemStack(Material.BEDROCK, 1),
            ItemStack(Material.WOOD_AXE, 1),
            ItemStack(Material.COMPASS, 1),
            ItemStack(Material.STRING, 1)
    )

    sendMessageHandler("staff-mode.enabled")
}

/**
 * Disable without sending a message.
 */
internal fun disableStaffMode(player: Player) {
    if (STAFF_PREVIOUS_STATE.containsKey(player.name.toLowerCase())) {
        player.inventory.contents = Array(36) { ItemStack(0) }
        player.inventory.armorContents = Array(4) { ItemStack(0) }

        val previous = STAFF_PREVIOUS_STATE[player.name.toLowerCase()]
        if (previous != null) {
            player.inventory.armorContents = previous.second
            player.inventory.contents = previous.first
        }

        STAFF_PREVIOUS_STATE.remove(player.name.toLowerCase())
    }
}

/**
 * Disable a player's staff mode and regain their previous inventory.
 */
internal fun CommandContext.disableStaffMode() {
    if (STAFF_PREVIOUS_STATE.containsKey(sender.name.toLowerCase())) {
        disableStaffMode(sender as Player)
        sendMessageHandler("staff-mode.disabled")
    }
}