package dev.shog.osmpl

import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cmd.CommandRunner
import dev.shog.osmpl.api.data.DataManager
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.commands.*
import dev.shog.osmpl.commands.impl.OsmCommand
import dev.shog.osmpl.commands.punish.*
import dev.shog.osmpl.commands.raffle.RAFFLE_COMMAND
import dev.shog.osmpl.discord.DiscordLink
import dev.shog.osmpl.events.*
import dev.shog.osmpl.events.data.PLAYER_DATA_MANAGER
import dev.shog.osmpl.handle.*
import dev.shog.osmpl.handle.BedEvents
import dev.shog.osmpl.handle.ConfigHandler
import dev.shog.osmpl.handle.IpChecker
import dev.shog.osmpl.handle.SlowMode
import dev.shog.osmpl.handle.SqlHandler
import dev.shog.osmpl.tf.MANAGE_TRUST_FACTOR
import dev.shog.osmpl.tf.TrustFactorHookHandler
import dev.shog.osmpl.tf.VIEW_PROGRESS
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin
import ru.tehkode.permissions.bukkit.PermissionsEx
import java.util.concurrent.TimeUnit

class OsmPl : OsmPlugin() {
    override val defaultMessageContainer: MessageContainer = MessageContainer.fromFile("messages.json")

    init {
        commands.addAll(setOf(
                DISCORD, HAT_COMMAND, LIST_COMMAND, OSM_COMMAND, SLEEPING_COMMAND, SLOWMODE_COMMAND, STAFF_COMMAND, DONATE_COMMAND, RAFFLE_COMMAND, BAN_COMMAND, TEMP_BAN_COMMAND, SEEN_COMMAND, UN_BAN_COMMAND, PLAYER_MANAGER, LANDMARKS_COMMAND, MUTE_COMMAND, TEMP_MUTE_COMMAND, UN_MUTE_COMMAND, PLAY_TIME_TOP, BAL_TOP, MANAGE_TRUST_FACTOR, VIEW_PROGRESS
        ))
    }

    companion object {
        const val VERSION = 2.8F

        internal lateinit var slowMode: SlowMode
        internal lateinit var ipChecker: IpChecker
        internal lateinit var discordLink: DiscordLink
    }

    override fun onEnable() {
        println("OSMPL has been enabled!")
        DataManager

        slowMode = SlowMode(this)

        try {
            PermissionsEx.getPermissionManager()
        } catch (ex: Exception) {
            System.err.println("PermissionsEX was not found, disabling OSM!")
            pluginLoader.disablePlugin(this)
        }

        configuration.load()

        if (!ConfigHandler.hasValidProperties(configuration)) {
            System.err.println("OSMPL: Please fill out the config file!")

            ConfigHandler.fillProperties(configuration)
            configuration.save()
            pluginLoader.disablePlugin(this)

            return
        }

        SqlHandler.url = configuration.getString("url")
        SqlHandler.username = configuration.getString("username")
        SqlHandler.password = configuration.getString("password")

        ipChecker = IpChecker(configuration.getString("ipHubKey"))
        webHook = configuration.getString("webhook")

        slowMode.timing = configuration.getInt("slowModeInSec", 5).toLong()

        sequenceOf(ENTITY_DEATH, STAFF_DISABLE, PLAYER_DATA_MANAGER, PLAYER_CHAT, SLOW_MODE_AUTO_TOGGLE, ON_VIEW_LOCKETTE, BLOCK_PLACE)
                .forEach { it.invoke(this) }

        val ev = BedEvents(this)

        server.pluginManager.registerEvent(Event.Type.PLAYER_BED_LEAVE, ev, Event.Priority.Normal, this)
        server.pluginManager.registerEvent(Event.Type.PLAYER_BED_ENTER, ev, Event.Priority.Normal, this)

        discordLink = DiscordLink(this)
        CommandRunner(this)

        TrustFactorHookHandler.fillRemaining(this) // Currently not hooking into OSMQ
    }

    /**
     * Add commands to OSM
     */
    private fun addCommands(vararg command: OsmCommand) {
        command.forEach { cmd ->
            getCommand(cmd.name).executor = cmd.getCommandExecutor(this)
        }
    }

    override fun onDisable() {
        println("OSMPL has been disabled")
        DataManager.saveAll()
        TrustFactorHookHandler.save()
    }
}