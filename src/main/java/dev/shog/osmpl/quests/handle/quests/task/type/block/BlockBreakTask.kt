package dev.shog.osmpl.quests.handle.quests.task.type.block

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockListener
import org.json.JSONObject

/**
 * A block break task.
 * You must break a [material] block [amount] times.
 */
class BlockBreakTask(
        val material: Material,
        val amount: Int,
        Quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask<HashMap<String, Int>, BlockBreakEvent>(name, Quests, donor, data) {
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

        return quests.messageContainer.getMessage(
            "commands.view-quest.status.block-break",
            status, amount,
            material.name.split("_").joinToString(" ") { str -> str.toLowerCase().capitalize() }
        )
    }

    override fun formatData(): HashMap<String, Int> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
        )
    }

    override val invokesOn: Event.Type = Event.Type.BLOCK_BREAK

    override fun invoke(event: BlockBreakEvent) {
        if (event.block.type == material&& !canContinue(event.player)) {
            val current = status[event.player.name.toLowerCase()] ?: 0

            status[event.player.name.toLowerCase()] = current + 1

            if (isComplete(event.player)) {
                onPlayerComplete.invoke(quests, event.player)
            }
        }
    }
}