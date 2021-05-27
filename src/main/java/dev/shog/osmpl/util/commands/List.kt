package dev.shog.osmpl.util.commands

import com.oldschoolminecraft.vanish.Invisiman
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMultiline
import ru.tehkode.permissions.PermissionGroup
import ru.tehkode.permissions.PermissionUser
import ru.tehkode.permissions.bukkit.PermissionsEx
import java.util.concurrent.ConcurrentHashMap

/**
 * The list command.
 */
internal val LIST_COMMAND = Command.make("list") {
    val players = osmModule.pl.server.onlinePlayers
            .filterNot { p -> Invisiman.instance.isVanished(p) }

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
        append(messageContainer.getMessage("list.header", players.size, osmModule.pl.server.maxPlayers))

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