package dev.shog.osmpl.quests.quest.handle.commands

import dev.shog.osmpl.api.OsmApi
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player

/**
 * Debug
 */
val DEBUG_COMMAND = Command.make("osmq_debug") {
    if (sender !is Player)
        return@make true

    sender.sendMessage("S: ${OsmApi.isDonor(sender.name)}, LS: ${(osmModule as Quests).lastQuestSave}")

    true
}