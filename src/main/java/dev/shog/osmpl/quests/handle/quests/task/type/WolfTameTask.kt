package dev.shog.osmpl.quests.handle.quests.task.type

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import org.bukkit.entity.Player
import org.bukkit.entity.Wolf
import org.bukkit.event.Event
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
) : QuestTask<HashMap<String, Int>, EntityTameEvent>(name, quests, donor, data) {
    override val status = formatData()

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

        return quests.messageContainer.getMessage("commands.view-quest.status.wolf-tame", status, amount)
    }

    override fun formatData(): HashMap<String, Int> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
        )
    }

    override val invokesOn: Event.Type = Event.Type.ENTITY_TAME

    override fun invoke(event: EntityTameEvent) {
        val player = event.owner as Player

        if (event.entity is Wolf && canContinue(player)) {
            val current = status[player.name.toLowerCase()] ?: 0

            status[player.name.toLowerCase()] = current + 1

            if (isComplete(player)) {
                onPlayerComplete.invoke(quests, player)
            }
        }
    }
}