package dev.shog.osmpl.mm

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.mm.commands.MODULE_COMMAND

class ModuleManager(pl: OsmPlugin) : OsmModule("ModuleManager", 1.0F, pl) {
    override val defaultMessageContainer: MessageContainer = MessageContainer.fromFile("messages/mm.json")

    override fun onEnable() {
        commands.add(MODULE_COMMAND)
    }

    override fun onDisable() {
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
    }

    override val config: Configuration = Configuration(this)
}