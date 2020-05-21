package dev.shog.osmpl.quests.impl

import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.quests.inf.QuestRequirements
import org.bukkit.entity.Player

/**
 * If the user is a supporter, they can enter.
 */
object Supporter : QuestRequirements {
    override fun meets(player: Player): Boolean =
            OsmApi.isDonor(player.name)
}