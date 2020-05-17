package dev.shog.osmpl.util.events

import dev.shog.osmpl.api.OsmModule
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Zombie
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener

/**
 * Send a message on entity death.
 */
internal val ENTITY_DEATH = { osm: OsmModule ->
    osm.pl.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
        override fun onEntityDeath(event: EntityDeathEvent?) {
            if (event != null && event.entity is Player) {
                val player = event.entity as Player

                val reason = when (player.lastDamageCause?.cause) {
                    null -> "killed themselves."

                    EntityDamageEvent.DamageCause.CONTACT -> "died."

                    EntityDamageEvent.DamageCause.ENTITY_ATTACK -> {
                        val lastDamage = player.lastDamageCause as EntityDamageByEntityEvent

                        when (lastDamage.damager) {
                            is Zombie -> "was munched on by a Zombie."
                            is Skeleton -> "was gunned down by a Skeleton."
                            is Player -> {
                                val enemy = player.lastDamageCause.entity as Player

                                "was brutally murdered by ${ChatColor.DARK_GRAY}${enemy.name}${ChatColor.GRAY}."
                            }
                            else -> "was hit too hard."
                        }
                    }
                    EntityDamageEvent.DamageCause.PROJECTILE -> "was gunned down."

                    EntityDamageEvent.DamageCause.SUFFOCATION -> "forgot to breathe."

                    EntityDamageEvent.DamageCause.FALL -> "forgot their shoes."

                    EntityDamageEvent.DamageCause.FIRE,
                    EntityDamageEvent.DamageCause.FIRE_TICK,
                    EntityDamageEvent.DamageCause.LAVA -> "was too hot."

                    EntityDamageEvent.DamageCause.DROWNING -> "forgot to breathe."

                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION -> "died to an explosion."

                    EntityDamageEvent.DamageCause.VOID -> "fell too far."

                    EntityDamageEvent.DamageCause.LIGHTNING -> "was smitten too hard"

                    EntityDamageEvent.DamageCause.SUICIDE -> "killed themselves."

                    EntityDamageEvent.DamageCause.CUSTOM -> "died."

                    else -> "died."
                }

                val message = osm.config.content.getString("playerDeath")
                        .replace("{0}", player.displayName)
                        .replace("{1}", reason)

                osm.pl.server.broadcastMessage(message)
            }
        }
    }, Event.Priority.Normal, osm.pl)
}