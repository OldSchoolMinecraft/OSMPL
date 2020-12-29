package dev.shog.osmpl.util.particles

import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object ParticleHandler {
    val effects = ConcurrentHashMap<String, MutableList<EffectType>>()

    fun getEffectsForPlayer(player: Player): List<EffectType>? {
        return effects[player.name.toLowerCase()] ?: listOf()
    }

    fun toggleEffect(player: Player, effect: EffectType) {
        val playerEffects = effects[player.name.toLowerCase()]

        println(effects)

        when {
            playerEffects == null ->
                effects[player.name.toLowerCase()] = mutableListOf(effect)

            playerEffects.contains(effect) -> {
                playerEffects.remove(effect)
                effects[player.name.toLowerCase()] = playerEffects
            }

            else -> {
                playerEffects.add(effect)
                effects[player.name.toLowerCase()] = playerEffects
            }
        }
    }
}