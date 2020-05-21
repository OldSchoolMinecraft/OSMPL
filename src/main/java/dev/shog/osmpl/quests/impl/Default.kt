package dev.shog.osmpl.quests.impl

import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.quests.inf.QuestRequirements
import org.bukkit.entity.Player

/**
 * Any user can enter.
 */
object Default : QuestRequirements {
    override fun meets(player: Player): Boolean =
            true
}