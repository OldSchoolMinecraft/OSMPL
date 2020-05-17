package dev.shog.osmpl.quests.quest.handle.quests.task.type.move

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerMoveEvent
import org.json.JSONObject

/**
 * A jump task.
 * You must jump [times] times.
 */
class JumpTask(
        val times: Long,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask(name, quests, donor, data) {
    private val status: HashMap<String, Int>
    override val identifier: String = "JUMP"

    init {
        status = if (!data.isEmpty) {
            val mapper = ObjectMapper()

            mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
            )
        } else hashMapOf()

        quests.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object : PlayerListener() {
            override fun onPlayerMove(event: PlayerMoveEvent?) {
                if (event != null && !isComplete(event.player) && userOk(event.player)) {
                    val current = status[event.player.name.toLowerCase()] ?: 0

                    val to = event.to.block.location
                    val from = event.from.block.location

                    // Checking if they moved to the block right above previous block.
                    if (to.blockX == from.blockX && to.blockZ == from.blockZ && from.blockY + 1 == to.blockY) {
                        status[event.player.name.toLowerCase()] = current + 1

                        if (isComplete(event.player)) {
                            onPlayerComplete.invoke(quests, event.player)
                        }
                    }
                }
            }
        }, Event.Priority.Low, quests.pl)
    }

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0

        return current.toLong() >= times
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
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.jump", status, times)
    }
}