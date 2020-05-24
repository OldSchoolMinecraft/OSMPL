package dev.shog.osmpl

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.mm.ModuleManager
import dev.shog.osmpl.money.EconomyModule
import dev.shog.osmpl.api.RemoteRestart
import dev.shog.osmpl.tf.TrustFactorModule
import dev.shog.osmpl.util.UtilModule


/**
 * The main plugin.
 */
class OsmPl : OsmPlugin() {
    companion object {
        /**
         * The [DiscordLink] module. This is in the companion due to it being required at the WebhookHandler.
         */
        internal lateinit var discordLink: DiscordLink

        /**
         * [RemoteRestart]
         */
        internal lateinit var remoteRestart: RemoteRestart
    }

    private val container = MessageContainer.fromFile("messages/osmpl.json")

    init {
        discordLink = DiscordLink(this)

        initRemoteRestart()

        configuration.load()

        configuration.getStringList("disabled", listOf())?.forEach { disable ->
            modules
                    .filter { module -> module.key.name.equals(disable, true) }
                    .forEach { disableModule(it.key) }
        }
    }

    /**
     * Set [remoteRestart]
     */
    private fun initRemoteRestart() {
        remoteRestart = RemoteRestart(this, container)
        println("[OSMPL] Remote restart is open at http://localhost:8010/restart")
    }

    override val modules: HashMap<OsmModule, Boolean> =
            hashMapOf(
                    UtilModule(this) to true,
                    discordLink to true,
                    EconomyModule(this) to true,
                    ModuleManager(this) to true,
                    TrustFactorModule(this) to true
            )

    override val requiredConfig: Collection<String> = setOf("username", "password", "url", "disabled")
}