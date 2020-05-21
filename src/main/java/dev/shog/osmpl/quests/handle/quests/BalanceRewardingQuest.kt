package dev.shog.osmpl.quests.handle.quests

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * A quest that rewards money.
 *
 * @param name The name of the quest.
 * @param tasks The tasks you must complete to finish the quest.
 * @param Quests The OSM quests instance.
 * @param balReward The balance reward the player is rewarded with when finishing.
 * @param rewardString The reward string. This could be "500 dollaryoos"
 */
class BalanceRewardingQuest(
        name: String,
        tasks: List<QuestTask<*, *>>,
        quests: Quests,
        donor: Boolean,
        val balReward: Double,
        override val rewardString: String
) : Quest(name, tasks, donor, quests) {
    /**
     * On complete give [player] $[balReward]
     */
    override fun onComplete(player: Player) {
        Economy.add(player.name, balReward)

        player.sendMessage(quests.messageContainer.getMessage("quests.quest-complete", questName, rewardString))
    }

    override val identifier: String = "BALANCE_REWARD"
}