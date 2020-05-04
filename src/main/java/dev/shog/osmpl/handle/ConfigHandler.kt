package dev.shog.osmpl.handle

import org.bukkit.util.config.Configuration

internal object ConfigHandler {
    private val requiredKeys = setOf("discordLink", "playerDeath", "discordMessage", "sleepComplete", "sleepPart", "enableSlowModeAt", "slowModeInSec", "rafflePrice", "slowModeDisableUnderThreshold", "ipHubKey", "webhook", "username", "password", "url", "botToken", "botChannel", "botWebhook")

    /**
     * Fill all properties on [configuration]
     */
    fun fillProperties(configuration: Configuration) {
        configuration.setProperty("discordLink", "https://discord.gg/ccCN5uH")
        configuration.setProperty("playerDeath", "§e{0}§6 {1}")
        configuration.setProperty("discordMessage", "§e{0}")

        configuration.setProperty("sleepComplete", "§6{0}/{1} §eplayers are in bed, time is now turning to day!")
        configuration.setProperty("sleepPart", "§6{0} §eis in bed. §6{1} §emore are required to turn it to day.")

        configuration.setProperty("ipHubKey", "Paste the IP Hub key here :)")

        configuration.setProperty("enableSlowModeAt", 17)
        configuration.setProperty("slowModeInSec", 5)
        configuration.setProperty("rafflePrice", 75)

        configuration.setProperty("slowModeDisableUnderThreshold", true)

        configuration.setProperty("webhook", "Paste the webhook here :)")

        configuration.setProperty("username", "Paste SQL username here :)")
        configuration.setProperty("password", "Paste SQL password here :)")
        configuration.setProperty("url", "Paste SQL url here :)")

        configuration.setProperty("botToken", "Paste Discord bot token here :)")
        configuration.setProperty("botChannel", "Paste #minecraft-chat channel ID here :)")
        configuration.setProperty("botWebhook", "Paste the #minecraft-chat webhook here :)")
    }

    /**
     * If [configuration] has the proper keys.
     */
    fun hasValidProperties(configuration: Configuration) =
            configuration.keys.containsAll(requiredKeys)
}