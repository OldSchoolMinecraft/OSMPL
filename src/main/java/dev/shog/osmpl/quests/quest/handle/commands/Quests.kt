package dev.shog.osmpl.quests.quest.handle.commands

import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osm.quest.sendMultiline
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player

private fun getQuestNameLink(quest: Quest, complete: Boolean) =
    if (complete)
        "commands.quests.${if (quest.donor) "supporter-" else ""}complete-quest"
    else "commands.quests.${if (quest.donor) "supporter-" else ""}uncomplete-quest"

private fun getTaskNameLink(complete: Boolean) =
    if (complete)
        "commands.quests.quest-viewer.complete-task"
    else "commands.quests.quest-viewer.uncomplete-task"

/**
 * View your quests.
 */
val VIEW_QUESTS = Command.make("quests") {
    val quests = osmModule as Quests

    if (sender !is Player) {
        sender.sendMessage("You must be a player for this.")
        return@make true
    }

    if (args.size == 1) {
        val quest = try {
            quests.quests[args[0].toInt()]
        } catch (ex: Exception) {
            null
        }

        if (quest == null) {
            sender.sendMessage(quests.messageContainer.getMessage("commands.quests.invalid-quest"))
        } else {
            val tasks = quest.tasks

            val taskList = buildString {
                tasks.map { task ->
                    quests.messageContainer.getMessage(
                            getTaskNameLink(task.isComplete(sender)),
                            task.name,
                            task.getStatusString(sender)
                    )
                }.forEach { append(it) }
            }

            sender.sendMultiline(
                    quests.messageContainer.getMessage(
                            "commands.quests.quest-viewer.${if (quest.isComplete(sender)) "complete" else "uncomplete"}-quest",
                            quest.questName,
                            quest.rewardString
                    ) + taskList
            )
        }

        return@make true
    }

    var message = quests.messageContainer.getMessage("commands.quests.header")

    quests.quests.forEachIndexed { i, quest ->
        message += "${quests.messageContainer.getMessage(
                getQuestNameLink(quest, quest.isComplete(sender)),
                quest.questName,
                i
        )}, "
    }

    sender.sendMultiline(message.removeSuffix(", "))

    true
}