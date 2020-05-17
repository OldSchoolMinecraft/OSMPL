package dev.shog.osmpl.quests.quest.handle.parser

import dev.shog.osm.quest.DIR
import dev.shog.osmpl.quests.quest.handle.quests.BalanceRewardingQuest
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.XpRewardingQuest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.block.BlockBreakTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.move.MoveTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.WolfTameTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.entity.EntityKillTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.entity.EntityType
import dev.shog.osmpl.quests.quest.handle.quests.task.type.move.BoatMoveTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.move.JumpTask
import dev.shog.osmpl.quests.quest.handle.quests.task.type.move.MinecartMoveTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.Material
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Manage the saving of quests.
 */
class QuestParser(val quests: Quests) {
    /**
     * Get all of the quests from [DIR].
     */
    fun getAllQuests(): LinkedList<Quest> {
        val files = DIR.listFiles()

        val list = files
            ?.filter { file -> file.extension.equals("json", true) }
            ?.map { file -> getQuest(file) }
            ?: listOf()

        return LinkedList(list)
    }

    /**
     * Save [quest] to [DIR].
     */
    fun saveQuest(quest: Quest, usePretty: Boolean = false) {
        val data = quest.getSaveData()
        val file = File(DIR.path + File.separator + "${quest.questName.replace(" ", "_")}.json")

        if (!file.exists())
            file.createNewFile()

        val json = if (usePretty) data.toString(4) else data.toString()

        file.outputStream().write(json.toByteArray())
    }

    /**
     * Parse a quest from [file].
     */
    private fun getQuest(file: File): Quest {
        val data = JSONObject(String(file.inputStream().readBytes()))

        val identifier = data.getString("identifier")
        val name = data.getString("name")
        val reward = data.getString("reward")
        val donor = data.getBoolean("donor")

        val parsedTasks = mutableListOf<QuestTask>()
        val tasks = data.getJSONArray("tasks")

        for (i in 0 until tasks.length()) {
            val task = tasks.getJSONObject(i)

            val taskName = task.getString("name")
            val taskIdentifier = task.getString("identifier")
            val taskData = task.getJSONObject("data")

            val builtTask = when (taskIdentifier.toLowerCase()) {
                "block_break" -> {
                    val material = Material.getMaterial(task.getString("material"))
                    val amount = task.getInt("amount")

                    BlockBreakTask(material, amount, quests, taskName, donor, taskData)
                }

                "wolf_tame" -> {
                    val amount = task.getInt("amount")

                    WolfTameTask(amount, quests, taskName, donor, taskData)
                }

                "block_place" -> {
                    val material = Material.getMaterial(task.getString("material"))
                    val amount = task.getInt("amount")

                    BlockBreakTask(material, amount, quests, taskName, donor, taskData)
                }

                "move" -> {
                    val distance = task.getLong("distance")

                    MoveTask(distance, quests, taskName, donor, taskData)
                }

                "move_boat" -> {
                    val distance = task.getLong("distance")

                    BoatMoveTask(distance, quests, taskName, donor, taskData)
                }

                "move_minecart" -> {
                    val distance = task.getLong("distance")

                    MinecartMoveTask(distance, quests, taskName, donor, taskData)
                }

                "jump" -> {
                    val times = task.getLong("times")

                    JumpTask(times, quests, taskName, donor, taskData)
                }

                "entity_kill" -> {
                    val type = task.getString("type")
                    val parsedType = EntityType.valueOf(type)

                    val amount = task.getInt("amount")

                    EntityKillTask(parsedType, amount, quests, taskName, donor, taskData)
                }

                else -> throw Exception("Invalid task.")
            }

            parsedTasks.add(builtTask)
        }

        return when (identifier.toLowerCase()) {
            "reward_xp" -> {
                XpRewardingQuest(name, parsedTasks, quests, donor, data.getLong("xpReward"), reward)
            }

            "balance_reward" -> {
                BalanceRewardingQuest(name, parsedTasks, quests, donor, data.getDouble("balanceReward"), reward)
            }

            else -> throw Exception("Invalid quest identifier.")
        }
    }
}