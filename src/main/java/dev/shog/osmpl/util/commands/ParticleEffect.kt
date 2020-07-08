package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessage
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.util.particles.EffectType
import dev.shog.osmpl.util.particles.ParticleHandler
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.entity.Player

val PARTICLE_EFFECT = Command.make("particle") {
    if (sender !is Player)
        return@make false

    when {
        args.isEmpty() ->
            sendMessageHandler("particle.list")

        args.size == 1 -> {
            val particle = args[0]
            val particleObj = EffectType.values().singleOrNull { effect -> effect.name.equals(particle, true) }

            if (particleObj == null) {
                sendMessageHandler("particle.invalid-particle")
                return@make true
            }

            ParticleHandler.toggleEffect(sender, particleObj)
            sendMessageHandler("particle.toggled", particleObj.name.toLowerCase().capitalize())
        }

        else -> return@make false
    }

    true
}