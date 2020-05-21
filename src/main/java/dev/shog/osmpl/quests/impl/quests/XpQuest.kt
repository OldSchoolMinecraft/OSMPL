package dev.shog.osmpl.quests.impl.quests

import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.handle.ranks.user.User
import dev.shog.osmpl.quests.inf.ITask
import dev.shog.osmpl.quests.inf.QuestRequirements
import org.bukkit.entity.Player

class XpQuest(
        val amount: Long,
        module: Quests,
        name: String,
        tasks: Collection<ITask>,
        requirements: QuestRequirements
) : Quest(module, name, "$amount XP", tasks, requirements) {
    override fun onComplete(player: Player) {
        User.getUser(player.name).xp += amount
        player.sendMessage(module.messageContainer.getMessage("quests.quest-complete", name, reward))
    }
}