package dev.shog.osmpl.questss.quest.handle.ranks

import dev.shog.osmpl.quests.quest.handle.ranks.user.User
import dev.shog.osmpl.quests.Quests

class QuestLadder(val quests: Quests) {
    private val LADDER = arrayOf(
        Rank("Player", quests.messageContainer.getMessage("prefix.player"), RankRequirements(0, 0.0, 0)),
        Rank("Newcomer", quests.messageContainer.getMessage("prefix.newcomer"), RankRequirements(10, 150.0, 5)),
        Rank("Traveller", quests.messageContainer.getMessage("prefix.traveller"), RankRequirements(50, 250.0, 25)),
        Rank("Known", quests.messageContainer.getMessage("prefix.known"), RankRequirements(150, 500.0, 75)),
        Rank("Expert", quests.messageContainer.getMessage("prefix.expert"), RankRequirements(300, 750.0, 150)),
        Rank("Elite", quests.messageContainer.getMessage("prefix.elite"), RankRequirements(500, 1000.0, 250)),
        Rank("Virtuoso", quests.messageContainer.getMessage("prefix.virtuoso"), RankRequirements(800, 1015.0, 400)),
        Rank("Veteran", quests.messageContainer.getMessage("prefix.veteran"), RankRequirements(1000, 5000.8925, 700))
    )

    /**
     * Get the rank above [user]'s rank.
     */
    fun getUpperRank(user: User): Rank? {
        return getRank(user.rank + 1)
    }

    /**
     * Get the ladder position of [rank].
     */
    fun getPos(rank: Rank): Int =
        LADDER.indexOf(rank)

    /**
     * Get a rank by it's ladder position.
     * Ladder starts at 0 (default)
     */
    fun getRank(pos: Int): Rank? =
        LADDER[pos]
}