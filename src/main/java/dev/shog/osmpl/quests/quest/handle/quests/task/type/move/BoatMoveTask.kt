package dev.shog.osmpl.quests.quest.handle.quests.task.type.move

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerMoveEvent
import org.json.JSONObject

/**
 * A move task.
 * You must travel [distance] blocks in a boat.
 */
class BoatMoveTask(
        val distance: Long,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask(name, quests, donor, data) {
    private val status: HashMap<String, Double>
    override val identifier: String = "MOVE_BOAT"

    init {
        status = if (!data.isEmpty) {
            val mapper = ObjectMapper()

            mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Double::class.java)
            )
        } else hashMapOf()

        quests.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object : PlayerListener() {
            override fun onPlayerMove(event: PlayerMoveEvent?) {
                if (event != null
                    && !isComplete(event.player)
                    && event.player.isInsideVehicle
                    && event.player.vehicle is Boat
                    && userOk(event.player)
                ) {
                    val current = status[event.player.name.toLowerCase()] ?: 0.0

                    status[event.player.name.toLowerCase()] = current + event.to.distance(event.from)

                    if (isComplete(event.player)) {
                        onPlayerComplete.invoke(quests, event.player)
                    }
                }
            }
        }, Event.Priority.Low, quests.pl)
    }

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0.0

        return current.toLong() >= distance
    }

    /**
     * The save data for the task.
     */
    override fun getSaveData(quest: Quest): JSONObject {
        val mapper = ObjectMapper()

        return JSONObject(mapper.writeValueAsString(status))
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0.0

        return quests.messageContainer.getMessage("commands.view-quest.status.move-boat", status.toInt(), distance)
    }
}