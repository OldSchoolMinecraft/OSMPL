package dev.shog.osmpl.discord.bot

import dev.kord.core.Kord

/**
 * A bot.
 */
interface IBot {
    suspend fun getClient(): Kord
}