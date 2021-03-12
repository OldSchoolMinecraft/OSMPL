package dev.shog.osmpl.util.events.data

import dev.shog.osmpl.*
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastMultiline
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.util.UtilModule
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.*

/**
 * Manages a player's data when they join.
 * Also manages bans.
 */
internal val PLAYER_DATA_MANAGER = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_PRELOGIN, PunishHandler.BAN_HANDLE(osm), Event.Priority.Highest, osm.pl)
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, PunishHandler.MUTE_HANDLE(osm), Event.Priority.Highest, osm.pl)
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, PunishHandler.MUTE_COMMAND_HANDLE(osm), Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_PRELOGIN, object : PlayerListener() {
        override fun onPlayerPreLogin(event: PlayerPreLoginEvent?) {
            if (event != null) {
                val ip = try {
                    event.address.hostAddress
                } catch (e: Exception) {
                    null
                }

                println(ip)
                println(event.name)

                if (ip == null) {
                    event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "${ChatColor.RED}Your IP couldn't be resolved.")
                    return
                }

                val ipBanned = DataManager.isIpBanned(ip)

                if (ipBanned.any()) {
                    osm.pl.server.broadcastPermission(
                        "${ChatColor.RED}Someone has tried to connect to ${ipBanned.first().name} on a banned ip ($ip), they have been kicked.",
                        "osm.notify.ips",
                        true
                    )

                    event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, "${ChatColor.RED}You have connected on an IP that has been previously banned.")
                    return
                }
            }
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
        override fun onPlayerJoin(event: PlayerJoinEvent?) {
            val player = event?.player

            if (player != null) {
                val ip = try {
                    event.player.address?.hostString
                } catch (e: Exception) {
                    null
                }

                if (ip == null) {
                    player.kickPlayer("${ChatColor.RED}Your IP could not be resolved.")
                    return
                }

                if (!DataManager.userExists(player.name.toLowerCase())) {
                    try {
                        DataManager.registerUser(player)
                    } catch (ex: Exception) { // This happens if the IP could not be resolved.
                        player.kickPlayer("${ChatColor.RED}Your IP could not be resolved.")

                        osm.pl.server.broadcastPermission(
                            "${ChatColor.RED}${player.name}'s IP couldn't be resolved and was kicked.",
                            "osm.notify.ips",
                            true
                        )
                    }
                }

                osm.handleWarn(DataManager.getUserData(player.name), player)

                val checkedIp = try {
                    UtilModule.ipChecker.checkIp(ip.orElse(""))
                } catch (e: Exception) {
                    osm.pl.server.broadcastPermission(
                        "§c${player.name} was unable to be checked for a VPN.",
                        "osm.notify.ips",
                        true
                    )

                    null
                }

                if (ip != "127.0.0.1") {
                    val gate = UtilModule.ipChecker.checkVpnGate(ip.orElse(""))

                    if (gate != null) {
                        osm.pl.server.broadcastMultiline(
                            "§c${player.name} has a VPN (kicked):\n${gate.toString()}",
                            "osm.notify.ips",
                            true
                        )

                        player.kickPlayer("§cThe IP you are using is most likely a VPN. If you think this is wrong, message us on Discord.")
                        return
                    }

                    when (checkedIp?.block) {
                        2 ->
                            osm.pl.server.broadcastMultiline(
                                "§c${player.name} most likely has a VPN:\n$checkedIp",
                                "osm.notify.ips",
                                true
                            )

                        1 -> {
                            osm.pl.server.broadcastMultiline(
                                "§c${player.name} has a VPN (kicked):\n$checkedIp",
                                "osm.notify.ips",
                                true
                            )

                            player.kickPlayer("§cThe IP you are using is most likely a VPN. If you think this is wrong, message us on Discord.")
                            return
                        }

                        else -> osm.pl.server.broadcastPermission(
                            "§c${player.name}: $checkedIp",
                            "osm.ipinfo",
                            true
                        )
                    }
                }
            } else {
                osm.pl.server.broadcastPermission(
                    "${ChatColor.RED}A user logging in was null and their data processing has been stopped.",
                    "osm.ipinfo",
                    true
                )
            }
        }
    }, Event.Priority.Highest, osm.pl)

    // Update a user's join time
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, DataManager.PLAYER_JOIN_EVENT, Event.Priority.Lowest, osm.pl)

    // Update a user's leave time
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, DataManager.PLAYER_LEAVE_EVENT, Event.Priority.Lowest, osm.pl)

    // Update KD leaderboard.
    osm.pl.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
        override fun onEntityDeath(event: EntityDeathEvent?) {
            val player = event?.entity

            if (event != null && player is Player) {
                if (event.entity.lastDamageCause is EntityDamageByEntityEvent) {
                    val cause = event.entity.lastDamageCause as EntityDamageByEntityEvent

                    if (cause.damager is Player) {
                        val damagerPlayer = cause.damager as Player

                        val userData = DataManager.getUserData(damagerPlayer.name)

                        if (userData != null) {
                            userData.kills++
                        }
                    }
                }

                val userData = DataManager.getUserData(player.name)

                if (userData != null) {
                    userData.deaths++
                }
            }
        }
    }, Event.Priority.Lowest, osm.pl)
}