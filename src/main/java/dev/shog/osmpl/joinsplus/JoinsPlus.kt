package dev.shog.osmpl.joinsplus

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerQuitEvent
import org.json.JSONObject
import java.io.File

class JoinsPlus(pl: OsmPlugin) : OsmModule("JoinsPlus", 1.0F, pl) {
    companion object {
        lateinit var DEFAULT_JOIN: String
        lateinit var DEFAULT_QUIT: String
    }

    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/joinsplus.json")
    private var isListenerDisabled = false

    private val listener = object: PlayerListener() {
        override fun onPlayerJoin(event: PlayerJoinEvent?) {
            if (event != null && !isListenerDisabled)
                dev.shog.osmpl.joinsplus.onPlayerJoin(event)
        }

        override fun onPlayerQuit(event: PlayerQuitEvent?) {
            if (event != null && !isListenerDisabled)
                dev.shog.osmpl.joinsplus.onPlayerQuit(event)
        }
    }

    override fun onEnable() {
        loadConfig()

        isListenerDisabled = false
        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, listener, Event.Priority.Normal, pl);
        pl.server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, listener, Event.Priority.Normal, pl);

        commands.addAll(arrayListOf(
            JOINS_PLUS, CUSTOM_JOIN_MESSAGE, CUSTOM_LEAVE_MESSAGE
        ))
    }

    fun loadConfig() {
        val file = File("plugins/JoinsPlus/config.json")

        if (!file.exists()) {
            log("There's no config, resorting to some hardcoded messages.")

            DEFAULT_JOIN = "%player% joined."
            DEFAULT_QUIT = "%player% disconnected."
        } else {
            val data = JSONObject(String(file.inputStream().readBytes()))

            DEFAULT_JOIN = data.getString("default_join")
            DEFAULT_QUIT = data.getString("default_quit")

            log("Found config, using join: $DEFAULT_JOIN, quit: $DEFAULT_QUIT")
        }
    }

    override fun onDisable() {
        isListenerDisabled = true
    }

    override fun onRefresh() {
        loadConfig()
    }

    override val config: Configuration = Configuration(this)
}