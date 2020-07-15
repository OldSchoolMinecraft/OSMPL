package dev.shog.osmpl.util.events.data

import dev.shog.osmpl.*
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastMultiline
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.util.UtilModule
import me.moderator_man.fo.FOLoginEvent
import me.moderator_man.fo.FakeOnline
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.*

/**
 * Manages a player's data when they join.
 * Also manages bans.
 */
internal val PLAYER_DATA_MANAGER = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, object : PlayerListener() {
        override fun onPlayerLogin(event: PlayerLoginEvent?) {
            if (event != null && DataManager.isUserBanned(event.player.name))
                osm.handleBan(DataManager.getUserData(event.player.name), event)
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, object : PlayerListener() {
        override fun onPlayerChat(event: PlayerChatEvent?) {
            if (event != null && DataManager.isUserMuted(event.player.name))
                osm.handleMute(DataManager.getUserData(event.player.name), event)
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, object : PlayerListener() {
        override fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent?) {
            if (event != null && DataManager.isUserMuted(event.player.name))
                osm.handleCommandMute(DataManager.getUserData(event.player.name), event)
        }
    }, Event.Priority.Highest, osm.pl)

    // Manage IP checking and if a user is banned
    val ipBanChecker = object : FOLoginEvent {
        override fun loggedIn(player: Player?) {
            if (player != null) {
                val ip = try {
                    player.address?.hostString
                } catch (e: Exception) {
                    null
                }

                when {
                    ip == null -> {
                        player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-not-resolved"))
                        return
                    }

                    DataManager.isIpBanned(ip) -> {
                        osm.pl.server.broadcastPermission(osm.messageContainer.getMessage("admin.ip-ban", player.name, ip), "osm.notify.ips")

                        DataManager.punishUser(
                                player.name,
                                "Console",
                                Punishment(
                                        System.currentTimeMillis(),
                                        osm.messageContainer.getMessage("default-ban-messages.ip-ban-auto"),
                                        PunishmentType.BAN,
                                        -1
                                )
                        )

                        player.kickPlayer(osm.messageContainer.getMessage("banned.ip-banned"))
                        return
                    }
                }

                val checkedIp = try {
                    UtilModule.ipChecker.checkIp(ip.orElse(""))
                } catch (e: Exception) {
                    osm.pl.server.broadcastPermission(
                            osm.messageContainer.getMessage("admin.unable-check-vpn", player.name),
                            "osm.notify.ips"
                    )

                    null
                }

                val gate = UtilModule.ipChecker.checkVpnGate(ip.orElse(""))

                if (gate != null) {
                    osm.pl.server.broadcastMultiline(
                            osm.messageContainer.getMessage("admin.vpn", player.name, gate.toString()),
                            "osm.notify.ips"
                    )

                    player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-vpn"))
                    return
                }


                when (checkedIp?.block) {
                    2 ->
                        osm.pl.server.broadcastMultiline(
                                osm.messageContainer.getMessage("admin.possible-vpn", player.name, checkedIp.toString()),
                                "osm.notify.ips"
                        )

                    1 -> {
                        osm.pl.server.broadcastMultiline(
                                osm.messageContainer.getMessage("admin.vpn", player.name, checkedIp.toString()),
                                "osm.notify.ips"
                        )

                        player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-vpn"))
                        return
                    }

                    else -> osm.pl.server.broadcastPermission(
                            osm.messageContainer.getMessage("admin.ip-info", player.name, checkedIp.toString()),
                            "osm.ipinfo"
                    )
                }

                if (!DataManager.userExists(player.name.toLowerCase())) {
                    try {
                        DataManager.registerUser(player)
                    } catch (ex: Exception) { // This happens if the IP could not be resolved.
                        val message = osm.messageContainer.getMessage("player-join.ip-not-resolved")

                        player.kickPlayer(message)
                    }
                }
            } else {
                osm.pl.server.broadcastPermission(
                        "A user logging in was null and their data processing has been stopped.",
                        "osm.ipinfo"
                )
            }
        }
    }

    FakeOnline.instance.loginEventHandlers.add(ipBanChecker)

    // Update a player's playtime
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
        override fun onPlayerJoin(event: PlayerJoinEvent?) {
            if (event != null) {
                val data = DataManager.getUserData(event.player.name)

                data?.lastLogIn = System.currentTimeMillis()
            }
        }
    }, Event.Priority.Lowest, osm.pl)

    // Update a player's playtime
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
        override fun onPlayerQuit(event: PlayerQuitEvent?) {
            if (event != null) {
                val data = DataManager.getUserData(event.player.name)

                if (data != null) {
                    val time = System.currentTimeMillis()

                    data.lastLogOut = time
                    data.playTime = (time - data.lastLogIn) + data.playTime
                }
            }
        }
    }, Event.Priority.Lowest, osm.pl)
}