package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.util.commands.disableStaffMode
import dev.shog.osmpl.hasPermissionOrOp
import dev.shog.osmpl.util.UtilModule
import dev.shog.osmpl.util.particles.EffectType
import dev.shog.osmpl.util.particles.ParticleHandler
import org.bukkit.ChatColor
import org.bukkit.Effect
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
    /*osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
        override fun onPlayerQuit(event: PlayerQuitEvent?) {
            if (event != null)
                disableStaffMode(event.player)
        }
    }, Event.Priority.Highest, osm.pl)*/
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
                //event.player.sendMessage("${ChatColor.YELLOW}You have bypassed Lockette due to your moderator status.")
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
                //event.player.sendMessage("${ChatColor.YELLOW}You have bypassed Lockette due to your moderator status.")
                event.isCancelled = false
            }
        }
    }, Event.Priority.Highest, osm.pl)
}

/**
 * Handles players moving.
 */
internal val MOVE_HANDLER = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object : PlayerListener() {
        override fun onPlayerMove(event: PlayerMoveEvent?) {
            if (event != null) {
                handleParticles(event)
            }
        }
    }, Event.Priority.Highest, osm.pl) // highest due to frozen
}

private fun handleParticles(event: PlayerMoveEvent) {
    val effects = ParticleHandler.getEffectsForPlayer(event.player)

    if (effects != null && !event.isCancelled) {
        val location = event.player.location

        for (effect in effects) {
            when (effect) {
                EffectType.TRAIL -> {
                    event.player.world.playEffect(
                            location,
                            Effect.SMOKE,
                            2000
                    )
                }

                EffectType.HALO -> {
                    event.player.world.playEffect(
                            location.add(0.0, 1.5, 0.0),
                            Effect.SMOKE,
                            2000
                    )
                }
            }
        }
    }
}