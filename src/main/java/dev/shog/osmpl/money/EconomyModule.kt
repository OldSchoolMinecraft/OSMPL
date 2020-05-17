package dev.shog.osmpl.money

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.money.commands.BANKS
import dev.shog.osmpl.money.commands.SAVINGS_COMMAND

class EconomyModule(pl: OsmPlugin) : OsmModule("Economy", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/econ.json")

    override fun onEnable() {
        SqlHandler.initValues(pl) // TODO move to osmpl

        commands.addAll(setOf(SAVINGS_COMMAND, BANKS))

        BankHandler.scheduleInterestAll()
    }

    override fun onDisable() {
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
        BankHandler.refreshValues()
        BankHandler.refreshBanks()
    }

    override val config: Configuration = Configuration(this)
}