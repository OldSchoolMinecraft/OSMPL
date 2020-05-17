package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.util.commands.disableStaffMode
import dev.shog.osmpl.hasPermissionOrOp
import dev.shog.osmpl.util.UtilModule
import org.bukkit.ChatColor
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockListener
import org.bukkit.event.player.*
import org.yi.acru.bukkit.Lockette.Lockette

/**
 * Disable staff mode on leave
 */
internal val STAFF_DISABLE = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
        override fun onPlayerQuit(event: PlayerQuitEvent?) {
            if (event != null)
                disableStaffMode(event.player)
        }
    }, Event.Priority.Highest, osm.pl)
}

/**
 * Override lockettte for moderators
 */
internal val ON_VIEW_LOCKETTE = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, object : PlayerListener() {
        override fun onPlayerInteract(event: PlayerInteractEvent?) {
            if (event != null
                    && event.clickedBlock != null
                    && Lockette.isProtected(event.clickedBlock)
                    && !Lockette.isOwner(event.clickedBlock, event.player.name)
                    && event.action == Action.RIGHT_CLICK_BLOCK
                    && event.player.hasPermissionOrOp("osm.ovl")) {
                event.player.sendMessage("${ChatColor.YELLOW}You have bypassed Lockette due to your moderator status.")
                event.isCancelled = false
            }
        }
    }, Event.Priority.Highest, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.BLOCK_BREAK, object : BlockListener() {
        override fun onBlockBreak(event: BlockBreakEvent?) {
            if (event != null
                    && event.block != null
                    && Lockette.isProtected(event.block)
                    && !Lockette.isOwner(event.block, event.player.name)
                    && event.player.hasPermissionOrOp("osm.ovl")
            ) {
                event.player.sendMessage("${ChatColor.YELLOW}You have bypassed Lockette due to your moderator status.")
                event.isCancelled = false
            }
        }
    }, Event.Priority.Highest, osm.pl)
}

/**
 * Enable slow mod on player count.
 */
internal val SLOW_MODE_AUTO_TOGGLE = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
        override fun onPlayerJoin(event: PlayerJoinEvent?) {
            if (event != null && osm.pl.server.onlinePlayers.size >= osm.config.content.getInt("enableSlowModeAt")) {
                UtilModule.slowMode.enabled = true
            }
        }
    }, Event.Priority.Normal, osm.pl)

    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
        override fun onPlayerQuit(event: PlayerQuitEvent?) {
            if (
                    event != null
                    && osm.config.content.getBoolean("slowModeDisableUnderThreshold")
                    && osm.pl.server.onlinePlayers.size < osm.config.content.getInt("enableSlowModeAt")
                    && UtilModule.slowMode.enabled
            ) {
                UtilModule.slowMode.enabled = false
            }
        }
    }, Event.Priority.Normal, osm.pl)
}