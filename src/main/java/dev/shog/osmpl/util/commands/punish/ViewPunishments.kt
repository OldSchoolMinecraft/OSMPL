package dev.shog.osmpl.util.commands.punish

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.cmd.CommandContext
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.User
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.api.msg.sendMultiMessageHandler
import dev.shog.osmpl.defaultFormat
import dev.shog.osmpl.unPunishmentWebhook
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * View a user's punishments
 */
internal val VIEW_PUNISHMENTS = Command.make("viewpunish") {
    when {
        args.isEmpty() ->
            return@make false

        args.size == 1 -> {
            val user = args[0]
            val punishments = getPunishmentsFor(user)

            if (punishments == null) {
                sendMessageHandler("view-punishments.invalid-user")
                return@make true
            }

            sendPunishments(user, punishments)
            return@make true
        }

        // They're using an index.
        args.size == 2 && args[1].toIntOrNull() != null -> {
            val index = args[1].toIntOrNull()

            val user = args[0]
            val punishments = getPunishmentsFor(user)
            val punishment = punishments?.singleOrNull { it.first == index }?.second

            when {
                index == null ->
                    sendMessageHandler("view-punishments.invalid-index")

                punishments == null ->
                    sendMessageHandler("view-punishments.invalid-user")

                punishment == null ->
                    sendMessageHandler("view-punishments.invalid-index")

                else -> {
                    sendMultiMessageHandler("view-punishments.view",
                            punishment.type,
                            user.toLowerCase(),
                            punishment.reason,
                            if (punishment.expire == -1L)
                                "Permanent"
                            else punishment.expire.defaultFormat(),
                            punishment.time.defaultFormat()
                    )
                }
            }
        }

        // They're using a filter.
        args.size == 2 -> {
            val filter = args[1]

            val type = PunishmentType.values()
                    .singleOrNull { it.toString().equals(filter, true) }

            if (type == null) {
                sendMessageHandler("view-punishments.invalid-filter")
                return@make true
            }

            val user = args[0]
            val punishments = getPunishmentsFor(user, type)

            if (punishments == null) {
                sendMessageHandler("view-punishments.invalid-user")
                return@make true
            }

            sendPunishments(user, punishments)
            return@make true
        }

        else -> return@make false
    }

    true
}

/**
 * Send a list of punishments to [CommandContext.sender].
 */
private fun CommandContext.sendPunishments(name: String, punishments: List<Pair<Int, Punishment>>) {
    val punishParsed = if (punishments.isNotEmpty()) {
        punishments
                .joinToString("") { pair ->
                    osmModule.messageContainer.getMessage("view-punishments.viewer.entry", pair.second.time.defaultFormat(), pair.second.type, pair.first)
                }
    } else "§7none"

    sendMultiMessageHandler("view-punishments.viewer.header", name.toLowerCase(), punishParsed)
}

/**
 * Get the punishments and [filter] if needed.
 *
 * Returns list of index to the punishment to maintain the index throughout filters.
 */
private fun getPunishmentsFor(name: String, filter: PunishmentType? = null): List<Pair<Int, Punishment>>? {
    val user = DataManager.getUserData(name)
            ?: return null

    val punishments = user.punishments
            .mapIndexed { index, punish -> index to punish }

    return if (filter != null)
        punishments.filter { punish -> punish.second.type == filter }
    else punishments
}