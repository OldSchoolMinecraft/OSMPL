package dev.shog.osmpl.quests.quest.handle.quests.task

import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.json.JSONObject

/**
 * @param name The name of the task
 * @param Quests The [Quests] instance
 * @Param data The for the task. This is empty if there's no previous data.
 */
abstract class QuestTask(
        val name: String,
        val quests: Quests,
        val donor: Boolean,
        val data: JSONObject
) {
    /**
     * When the player is complete with the task.
     */
    var onPlayerComplete: Quests.(Player) -> Unit = {}

    /**
     * The task identifier. This should identify what type of task it is.
     * This could be something like BLOCK_BREAK
     */
    abstract val identifier: String

    /**
     * If [player] is complete with this [QuestTask].
     */
    abstract fun isComplete(player: Player): Boolean

    /**
     * Get the save data from a [QuestTask].
     */
    abstract fun getSaveData(quest: Quest): JSONObject

    /**
     * Get a status string for a player.
     *
     * This could be "5/16 blocks placed" etc.
     */
    abstract fun getStatusString(player: Player): String

    /**
     * If the user meets the requirements (donor)
     */
    fun userOk(player: Player): Boolean {
        if (!donor)
            return true

        return OsmApi.isDonor(player.name)
    }
}