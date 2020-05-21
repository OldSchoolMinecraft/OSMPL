package dev.shog.osmpl.quests.handle.quests.task.type.block

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
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
) : QuestTask<HashMap<String, Int>, BlockPlaceEvent>(name, quests, donor, data) {
    override val status = HashMap<String, Int>()

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0

        return current >= amount
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.block-place", status.toLong(), amount, material.toString())
    }

    override fun formatData(): HashMap<String, Int> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
        )
    }

    override val invokesOn: Event.Type = Event.Type.BLOCK_PLACE

    override fun invoke(event: BlockPlaceEvent) {
        if (event.block.type == material && canContinue(event.player)) {
            val current = status[event.player.name.toLowerCase()] ?: 0

            status[event.player.name.toLowerCase()] = current + 1

            if (isComplete(event.player)) {
                onPlayerComplete.invoke(quests, event.player)
            }
        }
    }
}