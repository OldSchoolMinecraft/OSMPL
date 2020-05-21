package dev.shog.osmpl.quests.handle.quests.task

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.json.JSONObject

/**
 * @param name The name of the task
 * @param Quests The [Quests] instance
 * @Param data The for the task. This is empty if there's no previous data.
 */
abstract class QuestTask<T, E : Event>(val name: String, val quests: Quests, val donor: Boolean, val data: JSONObject) {
    abstract val status: T
    abstract val invokesOn: Event.Type

    abstract fun invoke(event: E)

    /**
     * When the player is complete with the task.
     */
    var onPlayerComplete: Quests.(Player) -> Unit = {}

    /**
     * If [player] is complete with this [QuestTask].
     */
    abstract fun isComplete(player: Player): Boolean

    /**
     * Get [data] as [T]
     */
    abstract fun formatData(): T

    /**
     * Get a status string for a player.
     *
     * This could be "5/16 blocks placed" etc.
     */
    abstract fun getStatusString(player: Player): String

    /**
     * If the user meets the requirements (donor)
     */
    fun canContinue(player: Player): Boolean {
        if (!donor)
            return !isComplete(player)

        return OsmApi.isDonor(player.name) && !isComplete(player)
    }
}