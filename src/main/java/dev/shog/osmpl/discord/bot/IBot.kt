package dev.shog.osmpl.discord.bot

import discord4j.core.GatewayDiscordClient

/**
 * A bot.
 */
interface IBot {
    fun getClient(): GatewayDiscordClient
}