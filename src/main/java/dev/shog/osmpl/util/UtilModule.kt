package dev.shog.osmpl.util

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.util.commands.BAL_TOP
import dev.shog.osmpl.util.commands.DISCORD
import dev.shog.osmpl.util.commands.DONATE_COMMAND
import dev.shog.osmpl.util.commands.HAT_COMMAND
import dev.shog.osmpl.util.commands.LANDMARKS_COMMAND
import dev.shog.osmpl.util.commands.LIST_COMMAND
import dev.shog.osmpl.util.commands.OSM_COMMAND
import dev.shog.osmpl.util.commands.PLAYER_MANAGER
import dev.shog.osmpl.util.commands.PLAY_TIME_TOP
import dev.shog.osmpl.util.commands.SEEN_COMMAND
import dev.shog.osmpl.util.commands.SLEEPING_COMMAND
import dev.shog.osmpl.util.commands.SLOWMODE_COMMAND
import dev.shog.osmpl.util.commands.STAFF_COMMAND
import dev.shog.osmpl.util.commands.raffle.RAFFLE_COMMAND
import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.tf.TrustFactorHookHandler
import dev.shog.osmpl.util.commands.punish.*
import dev.shog.osmpl.util.commands.punish.BAN_COMMAND
import dev.shog.osmpl.util.commands.punish.MUTE_COMMAND
import dev.shog.osmpl.util.commands.punish.TEMP_BAN_COMMAND
import dev.shog.osmpl.util.commands.punish.TEMP_MUTE_COMMAND
import dev.shog.osmpl.util.commands.punish.UN_BAN_COMMAND
import dev.shog.osmpl.util.commands.punish.UN_MUTE_COMMAND
import dev.shog.osmpl.util.commands.punish.VIEW_PUNISHMENTS
import dev.shog.osmpl.util.events.*
import dev.shog.osmpl.util.events.ENTITY_DEATH
import dev.shog.osmpl.util.events.PLAYER_CHAT
import dev.shog.osmpl.util.events.SLOW_MODE_AUTO_TOGGLE
import dev.shog.osmpl.util.events.STAFF_DISABLE
import dev.shog.osmpl.util.events.data.PLAYER_DATA_MANAGER
import dev.shog.osmpl.webHook
import org.bukkit.event.Event
import ru.tehkode.permissions.bukkit.PermissionsEx

class UtilModule(pl: OsmPlugin) : OsmModule("Util", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/util.json")

    init {
        commands.addAll(setOf(
                DISCORD, HAT_COMMAND, LIST_COMMAND, OSM_COMMAND, SLEEPING_COMMAND, SLOWMODE_COMMAND, STAFF_COMMAND, DONATE_COMMAND, RAFFLE_COMMAND, BAN_COMMAND, TEMP_BAN_COMMAND, SEEN_COMMAND, UN_BAN_COMMAND, PLAYER_MANAGER, LANDMARKS_COMMAND, MUTE_COMMAND, TEMP_MUTE_COMMAND, UN_MUTE_COMMAND, PLAY_TIME_TOP, BAL_TOP, VIEW_PUNISHMENTS, WARN_COMMAND
        ))
    }

    companion object {
        internal lateinit var slowMode: SlowMode
        internal lateinit var ipChecker: IpChecker
    }

    override fun onEnable() {
        println("OSMPL has been enabled!")
        DataManager

        slowMode = SlowMode(this)

        try {
            PermissionsEx.getPermissionManager()
        } catch (ex: Exception) {
            System.err.println("PermissionsEX was not found, disabling Util module!")
            pl.disableModule(this)
        }

        if (!config.has("discordLink", "discordMessage", "enableSlowModeAt", "slowModeInSec", "rafflePrice", "slowModeDisableUnderThreshold", "ipHubKey", "webhook")) {
            System.err.println("[OSMPL:Util] Please fill out the configuration file!")

            config.content.put("discordLink", "https://discord.gg/ccCN5uH")
            config.content.put("discordMessage", "§e{0}")

            config.content.put("ipHubKey", "Paste the IP Hub key here :)")

            config.content.put("enableSlowModeAt", 17)
            config.content.put("slowModeInSec", 5)
            config.content.put("rafflePrice", 75)

            config.content.put("slowModeDisableUnderThreshold", true)

            config.content.put("webhook", "Paste the webhook here :)")

            config.save()
            pl.disableModule(this)

            return
        }

        ipChecker = IpChecker(config.content.getString("ipHubKey"))
        webHook = config.content.getString("webhook")

        slowMode.timing =config.content.getInt("slowModeInSec").toLong()

        sequenceOf(ENTITY_DEATH, STAFF_DISABLE, PLAYER_DATA_MANAGER, PLAYER_CHAT, SLOW_MODE_AUTO_TOGGLE, ON_VIEW_LOCKETTE, BLOCK_PLACE)
                .forEach { it.invoke(this) }

        val ev = BedEvents(this)

        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_BED_LEAVE, ev, Event.Priority.Normal, this.pl)
        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_BED_ENTER, ev, Event.Priority.Normal, this.pl)
    }

    override fun onDisable() {
        DataManager.saveAll()
        TrustFactorHookHandler.save()
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
    }

    override val config: Configuration = Configuration(this)
}