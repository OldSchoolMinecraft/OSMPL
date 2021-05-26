package dev.shog.osmpl.autobroadcast

import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.money.BankHandler
import dev.shog.osmpl.money.commands.BANKS
import dev.shog.osmpl.money.commands.SAVINGS_COMMAND
import dev.shog.osmpl.translateAlternateColorCodes
import org.bukkit.ChatColor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class AutoBroadcaster(pl: OsmPlugin) : OsmModule("AutoBroadcaster", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/autobroadcaster.json")

    val messages = arrayListOf(
        ":)"
    )

    var timer = Timer()

    /**
     * Broadcast a random message.
     */
    private val announcer: TimerTask.() -> Unit = {
        broadcastMessage(this@AutoBroadcaster, messages.random())
    }

    var time = TimeUnit.MINUTES.toMillis(5)

    /**
     * Cancel the timer, create a new one and assign [announcer]
     */
    private fun reschedule() {
        timer.cancel()
        timer = Timer()
        timer.schedule(timerTask(announcer), 0, time)
    }

    /**
     * Load data from file.
     */
    private fun loadFile() {
        val obj = config.content

        val fileMessages = if (!obj.has("messages"))
            JSONArray()
        else
            obj.getJSONArray("messages")
        messages.clear()
        messages.addAll(
            fileMessages.toList()
                .map { msg -> msg.toString() }
                .map { msg -> translateAlternateColorCodes('&', msg) }
        )

        val delay = if (!obj.has("delay"))
            1000 * 60 * 5 // 5 minutes
        else
            obj.getLong("delay")

        time = delay

        println("Auto-broadcaster data have been successfully reloaded.")
    }

    override fun onEnable() {
        config.refreshContent()
        loadFile()
        reschedule()

        println("Loaded auto-broadcaster with a delay of $time and ${messages.size} messages.")

        commands.addAll(setOf(IGNORE_COMMAND))
    }

    override fun onDisable() {
        timer.cancel()
        timer = Timer()
        config.save()
    }

    override fun onRefresh() {
        config.refreshContent()
        loadFile()
        reschedule()
    }

    override val config: Configuration = Configuration(
        this,
        JSONObject()
            .put("delay", 1000 * 60 * 5)
            .put("messages",
                JSONArray()
                    .put("Hello")
            )
    )
}