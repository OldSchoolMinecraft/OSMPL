package dev.shog.osmpl.tf

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.tf.cmd.MANAGE_TRUST_FACTOR
import dev.shog.osmpl.tf.cmd.VIEW_PROGRESS

class TrustFactorModule(pl: OsmPlugin) : OsmModule("TrustFactor", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/tfm.json")

    override fun onEnable() {
        commands.addAll(setOf(VIEW_PROGRESS, MANAGE_TRUST_FACTOR))

        TrustFactorHookHandler.fillRemaining(pl)
    }

    override fun onDisable() {
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
    }

    override val config: Configuration = Configuration(this)
}