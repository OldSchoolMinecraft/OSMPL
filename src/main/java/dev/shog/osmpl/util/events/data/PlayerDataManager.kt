package dev.shog.osmpl.util.events.data

import dev.shog.osmpl.*
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.api.msg.broadcastMultiline
import dev.shog.osmpl.api.msg.broadcastPermission
import dev.shog.osmpl.util.UtilModule
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
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_PRELOGIN, object : PlayerListener() {
        override fun onPlayerPreLogin(event: PlayerPreLoginEvent?) {
            if (event != null) {
                if (DataManager.isUserBanned(event.name))
                    osm.handleBan(DataManager.getUserData(event.name), event)
            }
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, object : PlayerListener() {
        override fun onPlayerChat(event: PlayerChatEvent?) {
            if (event != null && DataManager.isUserMuted(event.player.name))
                osm.handleMute(DataManager.getUserData(event.player.name), event)
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
                    player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-not-resolved"))
                    return
                }

                val checkedIp = try {
                    UtilModule.ipChecker.checkIp(ip.orElse(""))
                } catch (e: Exception) {
                    osm.pl.server.broadcastPermission(
                            osm.messageContainer.getMessage("admin.unable-check-vpn", player.name),
                            "osm.notify.ips",
                            true
                    )

                    null
                }

                if (ip != "127.0.0.1") {
                    val gate = UtilModule.ipChecker.checkVpnGate(ip.orElse(""))

                    if (gate != null) {
                        osm.pl.server.broadcastMultiline(
                            osm.messageContainer.getMessage("admin.vpn", player.name, gate.toString()),
                            "osm.notify.ips",
                            true
                        )

                        println("gate is null")
                        player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-vpn"))
                        return
                    }

                    when (checkedIp?.block) {
                        2 ->
                            osm.pl.server.broadcastMultiline(
                                osm.messageContainer.getMessage("admin.possible-vpn", player.name, checkedIp.toString()),
                                "osm.notify.ips",
                                true
                            )

                        1 -> {
                            osm.pl.server.broadcastMultiline(
                                osm.messageContainer.getMessage("admin.vpn", player.name, checkedIp.toString()),
                                "osm.notify.ips",
                                true
                            )

                            println(checkedIp)

                            player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-vpn"))
                            return
                        }

                        else -> osm.pl.server.broadcastPermission(
                            osm.messageContainer.getMessage("admin.ip-info", player.name, checkedIp.toString()),
                            "osm.ipinfo",
                            true
                        )
                    }
                }
            }
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, object : PlayerListener() {
        override fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent?) {
            if (event != null && DataManager.isUserMuted(event.player.name))
                osm.handleCommandMute(DataManager.getUserData(event.player.name), event)
        }
    }, Event.Priority.Highest, osm.pl)

    // :)
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, object : PlayerListener() {
        override fun onPlayerLogin(event: PlayerLoginEvent?) {
            if (event != null) {
                val player = event.player

                if (player != null) {
                    val ip = try {
                        player.address?.hostString
                    } catch (e: Exception) {
                        null
                    }

                    if (ip == null) {
                        player.kickPlayer(osm.messageContainer.getMessage("player-join.ip-not-resolved"))
                        return
                    }

                    val ipBanned = DataManager.isIpBanned(ip)

                    if (ipBanned.any()) {
                        osm.pl.server.broadcastPermission(
                            osm.messageContainer.getMessage("admin.ip-block", player.name, ip),
                            "osm.notify.ips",
                            true
                        )

                        return
                    }

                    if (!DataManager.userExists(player.name.toLowerCase())) {
                        try {
                            DataManager.registerUser(player)
                        } catch (ex: Exception) { // This happens if the IP could not be resolved.
                            val message = osm.messageContainer.getMessage("player-join.ip-not-resolved")

                            player.kickPlayer(message)
                        }
                    }

                    osm.handleWarn(DataManager.getUserData(player.name), player)
                } else {
                    osm.pl.server.broadcastPermission(
                        "A user logging in was null and their data processing has been stopped.",
                        "osm.ipinfo",
                        true
                    )
                }
            }
        }
    }, Event.Priority.Highest, osm.pl)

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