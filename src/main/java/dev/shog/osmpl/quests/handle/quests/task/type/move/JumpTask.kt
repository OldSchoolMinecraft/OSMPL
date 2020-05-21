package dev.shog.osmpl.quests.handle.quests.task.type.move

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
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
) : QuestTask<HashMap<String, Int>, PlayerMoveEvent>(name, quests, donor, data) {
    override val status = hashMapOf<String, Int>()

    init {
        quests.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object : PlayerListener() {
            override fun onPlayerMove(event: PlayerMoveEvent?) {
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
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.jump", status, times)
    }

    override val invokesOn: Event.Type = Event.Type.PLAYER_MOVE
    override fun invoke(event: PlayerMoveEvent) {
        if (canContinue(event.player)) {
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

    override fun formatData(): HashMap<String, Int> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
        )
    }

}