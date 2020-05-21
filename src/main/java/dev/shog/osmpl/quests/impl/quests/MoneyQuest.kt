package dev.shog.osmpl.quests.impl.quests

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.inf.ITask
import dev.shog.osmpl.quests.inf.QuestRequirements
import org.bukkit.entity.Player

/**
 * A quest that rewards [amount] of money when complete.
 */
class MoneyQuest(
        val amount: Double,
        module: Quests,
        name: String,
        tasks: Collection<ITask>,
        requirements: QuestRequirements
) : Quest(module, name, "$$amount", tasks, requirements) {
    override fun onComplete(player: Player) {
        Economy.add(player.name, amount)
        player.sendMessage(module.messageContainer.getMessage("quests.quest-complete", name, reward))
    }
}