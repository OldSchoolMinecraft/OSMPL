package dev.shog.osmpl.quests.inf

import org.bukkit.entity.Player

interface IQuest {
    val name: String
    val reward: String
    val tasks: Collection<ITask>
    val requirements: QuestRequirements

    fun onComplete(player: Player)

    fun isComplete(player: Player): Boolean

    /**
     * When a task is complete. This is called from [ITask]
     */
    fun finishTask(player: Player, task: ITask)
}