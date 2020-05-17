package dev.shog.osmpl.quests.quest.handle.quests.task.type

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.entity.Wolf
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.entity.EntityTameEvent
import org.json.JSONObject

/**
 * A wolf tame task.
 * You must tame a wolf [amount] times.
 */
class WolfTameTask(
        val amount: Int,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask(name, quests, donor, data) {
    private val status: HashMap<String, Int>
    override val identifier: String = "WOLF_TAME"

    init {
        status = if (!data.isEmpty && data.has("status")) {
            val status = data.getJSONObject("status")
            val mapper = ObjectMapper()

            mapper.readValue(
                status.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
            )
        } else hashMapOf()

        quests.pl.server.pluginManager.registerEvent(Event.Type.ENTITY_TAME, object : EntityListener() {
            override fun onEntityTame(event: EntityTameEvent?) {
                if (event != null) {
                    val player = event.owner as Player

                    if (event.entity is Wolf && userOk(player) && !isComplete(player)) {
                        val current = status[player.name.toLowerCase()] ?: 0

                        status[player.name.toLowerCase()] = current + 1

                        if (isComplete(player)) {
                            onPlayerComplete.invoke(quests, player)
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

        return current >= amount
    }

    /**
     * The save data for the task.
     */
    override fun getSaveData(quest: Quest): JSONObject {
        val mapper = ObjectMapper()

        val st = JSONObject(mapper.writeValueAsString(status))

        val obj = JSONObject()

        obj.put("status", st)

        return obj
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.wolf-tame", status, amount)
    }
}