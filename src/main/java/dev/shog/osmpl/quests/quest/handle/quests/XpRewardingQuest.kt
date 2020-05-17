package dev.shog.osmpl.quests.quest.handle.quests

import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.quest.handle.ranks.user.User
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player

/**
 * A quest that rewards money.
 *
 * @param name The name of the quest.
 * @param tasks The tasks you must complete to finish the quest.
 * @param quests The OSM quests instance.
 * @param xpReward The XP reward the player is rewarded with when finishing.
 * @param rewardString The reward string. This could be "25 experiences"
 */
class XpRewardingQuest(
        name: String,
        tasks: List<QuestTask>,
        quests: Quests,
        donor: Boolean,
        val xpReward: Long,
        override val rewardString: String
) : Quest(name, tasks, donor, quests) {
    /**
     * On complete give [player] [xpReward] xp
     */
    override fun onComplete(player: Player) {
        val user = User.getUser(player.name)

        user.xp += xpReward

        player.sendMessage(quests.messageContainer.getMessage("quests.quest-complete", questName, rewardString))
    }

    override val identifier: String = "REWARD_XP"
}