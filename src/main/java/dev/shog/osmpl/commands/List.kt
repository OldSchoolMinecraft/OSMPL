package dev.shog.osmpl.commands

import com.nilla.vanishnopickup.VanishNoPickup
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.sendMultiline
import org.bukkit.Bukkit.getServer
import ru.tehkode.permissions.PermissionGroup
import ru.tehkode.permissions.PermissionUser
import ru.tehkode.permissions.bukkit.PermissionsEx
import java.util.concurrent.ConcurrentHashMap

private val VANISH_NO_PICKUP by lazy {
    getServer().pluginManager.getPlugin("VanishNoPickup") as VanishNoPickup
}

/**
 * The list command.
 */
internal val LIST_COMMAND = Command.make("list") {
    val players = osmPlugin.server.onlinePlayers
            .filterNot { p -> VANISH_NO_PICKUP.isPlayerHidden(p.name) || VANISH_NO_PICKUP.isPlayerInvisible(p.name) }

    val groups = ConcurrentHashMap<PermissionGroup, ArrayList<PermissionUser>>()

    for (player in players) {
        val user = PermissionsEx.getPermissionManager().getUser(player)
        val group = user.groups.first()

        groups[group]?.add(user)
                ?: run {
                    groups[group] = arrayListOf(user)
                }
    }

    val str = buildString {
        append(messageContainer.getMessage("list.header", players.size, osmPlugin.server.maxPlayers))

        for (group in groups) {
            append(buildString {
                append(messageContainer.getMessage("list.group", group.key.name.capitalize()))

                for (player in group.value) {
                    append(messageContainer.getMessage("list.user", player.name))
                }
            }.removeSuffix("§7, "))
        }
    }.removePrefix("\n").removeSuffix("\n")

    sendMultiline(str)

    true
}