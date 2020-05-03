package dev.shog.osmpl.tf.inf

import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent

interface TrustFactorHook<K : Event> {
    fun invoke(event: K)
}