package dev.shog.osmpl.quests.inf

import org.bukkit.entity.Player

/**
 * The requirements for entering a quest.
 */
interface QuestRequirements {
    /**
     * If [player] can enter the quest.
     */
    fun meets(player: Player): Boolean
}