package dev.shog.osmpl.tf.inf

import org.bukkit.event.Event

interface TrustFactorHook<K : Event> {
    fun invoke(event: K)
}