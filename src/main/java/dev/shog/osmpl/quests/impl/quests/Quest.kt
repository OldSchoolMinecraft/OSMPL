package dev.shog.osmpl.quests.impl.quests

import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.inf.IQuest
import dev.shog.osmpl.quests.inf.ITask
import dev.shog.osmpl.quests.inf.QuestRequirements
import org.bukkit.entity.Player

abstract class Quest(
        val module: Quests,
        override val name: String,
        override val reward: String,
        override val tasks: Collection<ITask>,
        override val requirements: QuestRequirements
) : IQuest {
    override fun isComplete(player: Player): Boolean {
        return tasks.all { it.isComplete(player) }
    }

    override fun finishTask(player: Player, task: ITask) {
        player.sendMessage(module.messageContainer.getMessage("quests.task-complete", task.name, name))

        if (isComplete(player))
            onComplete(player)
    }
}