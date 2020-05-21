package dev.shog.osmpl.quests.handle.parser

import dev.shog.osmpl.quests.handle.QUEST_DATA_DIR
import dev.shog.osmpl.quests.handle.quests.BalanceRewardingQuest
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.XpRewardingQuest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.handle.quests.task.type.block.BlockBreakTask
import dev.shog.osmpl.quests.handle.quests.task.type.move.MoveTask
import dev.shog.osmpl.quests.handle.quests.task.type.WolfTameTask
import dev.shog.osmpl.quests.handle.quests.task.type.entity.EntityKillTask
import dev.shog.osmpl.quests.handle.quests.task.type.entity.EntityType
import dev.shog.osmpl.quests.handle.quests.task.type.move.BoatMoveTask
import dev.shog.osmpl.quests.handle.quests.task.type.move.JumpTask
import dev.shog.osmpl.quests.handle.quests.task.type.move.MinecartMoveTask
import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.handle.quests.task.type.block.BlockPlaceTask
import org.bukkit.Material
import org.bukkit.event.Event
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Manage the saving of quests.
 */
class QuestParser(val quests: Quests) {
    /**
     * Get all of the quests from [QUEST_DATA_DIR].
     */
    fun getAllQuests(): LinkedList<Quest> {
        val files = QUEST_DATA_DIR.listFiles()

        val list = files
            ?.filter { file -> file.extension.equals("json", true) }
            ?.map { file -> getQuest(file) }
            ?: listOf()

        return LinkedList(list)
    }

    /**
     * Save [quest] to [QUEST_DATA_DIR].
     */
    fun saveQuest(quest: Quest) {
        val data = quest.getSaveData()
        val file = try {
            File(QUEST_DATA_DIR.path + File.separator + "${quest.questName.replace(" ", "_")}.json")
        } catch (e: Exception) {
            System.err.println("[OSMPL:QUESTS] Failed to create file for ${quest.questName}.")
            return
        }

        if (!file.exists())
            file.createNewFile()

        try {
            file.outputStream().write(data.toString().toByteArray())
        } catch (e: Exception) {
            System.err.println("[OSMPL:QUESTS] Failed to save quest: ${quest.questName}")
        }
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

        val parsedTasks = mutableListOf<QuestTask<*, *>>()
        val tasks = data.getJSONArray("tasks")

        for (i in 0 until tasks.length()) {
            val task = tasks.getJSONObject(i)

            val taskName = task.getString("name")
            val taskType = task.getString("type")
            val taskData = task.getJSONObject("data")

            val builtTask = when (taskType) {
                BlockBreakTask::class.java.simpleName -> {
                    val material = Material.getMaterial(task.getString("material"))
                    val amount = task.getInt("amount")

                    BlockBreakTask(material, amount, quests, taskName, donor, taskData)
                }

                WolfTameTask::class.java.simpleName -> {
                    val amount = task.getInt("amount")

                    WolfTameTask(amount, quests, taskName, donor, taskData)
                }

                BlockPlaceTask::class.java.simpleName -> {
                    val material = Material.getMaterial(task.getString("material"))
                    val amount = task.getInt("amount")

                    BlockBreakTask(material, amount, quests, taskName, donor, taskData)
                }

                MoveTask::class.simpleName -> {
                    val distance = task.getLong("distance")

                    MoveTask(distance, quests, taskName, donor, taskData)
                }

                BoatMoveTask::class.java.simpleName -> {
                    val distance = task.getLong("distance")

                    BoatMoveTask(distance, quests, taskName, donor, taskData)
                }

                MinecartMoveTask::class.java.simpleName -> {
                    val distance = task.getLong("distance")

                    MinecartMoveTask(distance, quests, taskName, donor, taskData)
                }

                JumpTask::class.java.simpleName -> {
                    val times = task.getLong("times")

                    JumpTask(times, quests, taskName, donor, taskData)
                }

                EntityKillTask::class.java.simpleName -> {
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
                XpRewardingQuest(name, parsedTasks as MutableList<QuestTask<HashMap<*, *>, Event>>, quests, donor, data.getLong("xpReward"), reward)
            }

            "balance_reward" -> {
                BalanceRewardingQuest(name, parsedTasks as MutableList<QuestTask<HashMap<*, *>, Event>>, quests, donor, data.getDouble("balanceReward"), reward)
            }

            else -> throw Exception("Invalid quest identifier.")
        }
    }
}