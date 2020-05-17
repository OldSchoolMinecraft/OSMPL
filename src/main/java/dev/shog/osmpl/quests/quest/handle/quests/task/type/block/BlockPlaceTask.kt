package dev.shog.osmpl.quests.quest.handle.quests.task.type.block

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockListener
import org.bukkit.event.block.BlockPlaceEvent
import org.json.JSONObject

/**
 * A block place task.
 * You must place a [material] block [amount] times.
 */
class BlockPlaceTask(
        val material: Material,
        val amount: Int,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask(name, quests, donor, data) {
    private val status: HashMap<String, Int>
    override val identifier: String = "BLOCK_PLACE"

    init {
        status = if (!data.isEmpty) {
            val mapper = ObjectMapper()

            mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
            )
        } else hashMapOf()

        quests.pl.server.pluginManager.registerEvent(Event.Type.BLOCK_PLACE, object : BlockListener() {
            override fun onBlockPlace(event: BlockPlaceEvent?) {
                if (event != null && event.block.type == material && !isComplete(event.player) && userOk(event.player)) {
                    val current = status[event.player.name.toLowerCase()] ?: 0

                    status[event.player.name.toLowerCase()] = current + 1

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
        val current = status[player.name.toLowerCase()] ?: 0

        return current >= amount
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

        return quests.messageContainer.getMessage("commands.view-quest.status.block-place", status.toLong(), amount, material.toString())
    }
}