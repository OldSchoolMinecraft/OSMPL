package dev.shog.osmpl.quests.handle.quests.task.type.move

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerMoveEvent
import org.json.JSONObject

/**
 * A move task.
 * You must walk [distance] blocks.
 */
class MoveTask(
        val distance: Long,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask<HashMap<String, Double>, PlayerMoveEvent>(name, quests, donor, data) {
    override val status = formatData()

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0.0

        return current.toLong() >= distance
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0.0

        return quests.messageContainer.getMessage("commands.view-quest.status.move", status.toInt(), distance)
    }

    override val invokesOn: Event.Type = Event.Type.PLAYER_MOVE

    override fun invoke(event: PlayerMoveEvent) {
        if (canContinue(event.player)) {
            val current = status[event.player.name.toLowerCase()] ?: 0.0

            val to = event.to.block
            val aboveTo = event.player.server.getWorld("world").getBlockAt(to.x, to.y + 1, to.z)

            if (!to.isLiquid && !aboveTo.isLiquid) {
                status[event.player.name.toLowerCase()] = current + event.to.distance(event.from)

                if (isComplete(event.player)) {
                    onPlayerComplete.invoke(quests, event.player)
                }
            }
        }
    }

    override fun formatData(): HashMap<String, Double> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Double::class.java)
        )
    }
}