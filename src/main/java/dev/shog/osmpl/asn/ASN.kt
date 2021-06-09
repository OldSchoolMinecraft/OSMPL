package dev.shog.osmpl.asn

import dev.shog.osmpl.api.msg.MessageContainer.Companion.fromFile
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer

class ASN(pl: OsmPlugin) : OsmModule("ASN", 1.0f, pl) {
    override val messageContainer: MessageContainer
        get() = fromFile("messages/asn.json")

    override fun onEnable() {}
    override fun onDisable() {
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
    }

    override val config = Configuration(this)
}