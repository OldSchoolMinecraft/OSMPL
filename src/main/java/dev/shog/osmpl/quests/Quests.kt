package dev.shog.osmpl.quests

import dev.shog.osmpl.quests.handle.parser.QuestParser
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.questss.quest.handle.ranks.QuestLadder
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.quests.handle.commands.DEBUG_COMMAND
import dev.shog.osmpl.quests.handle.commands.RANK
import dev.shog.osmpl.quests.handle.commands.RANK_UP
import dev.shog.osmpl.quests.handle.commands.VIEW_QUESTS
import dev.shog.osmpl.quests.handle.quests.BalanceRewardingQuest
import dev.shog.osmpl.quests.handle.quests.XpRewardingQuest
import dev.shog.osmpl.quests.handle.quests.task.type.WolfTameTask
import dev.shog.osmpl.quests.handle.quests.task.type.block.BlockBreakTask
import dev.shog.osmpl.quests.handle.quests.task.type.entity.EntityKillTask
import dev.shog.osmpl.quests.handle.quests.task.type.entity.EntityType
import dev.shog.osmpl.quests.handle.quests.task.type.move.BoatMoveTask
import dev.shog.osmpl.quests.handle.quests.task.type.move.JumpTask
import dev.shog.osmpl.quests.handle.quests.task.type.move.MoveTask
import dev.shog.osmpl.questss.quest.handle.commands.VIEW_XP
import org.bukkit.Material
import org.json.JSONObject
import java.util.*

class Quests(pl: OsmPlugin) : OsmModule("Quests", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/quests.json")
    override val config: Configuration = Configuration(this)
    var lastQuestSave = System.currentTimeMillis()
    var quests = LinkedList<Quest>()

    val parser = QuestParser(this)
    val ladder = QuestLadder(this)

    override fun onEnable() {
//        quests = parser.getAllQuests()

        commands.addAll(setOf(VIEW_XP, VIEW_QUESTS, DEBUG_COMMAND, RANK_UP, RANK))

        quests.add(BalanceRewardingQuest("Slay the Squid Kingdom", listOf(
                BoatMoveTask(500L, this, "Locate their base", true, JSONObject()),
                EntityKillTask(EntityType.SQUID, 16, this, "Assassinate them", true, JSONObject())
        ), this, true, 200.00, "200 dollas"))

        quests.add(BalanceRewardingQuest("Overthrow the Coop", listOf(
                JumpTask(15L, this, "Jump on their dead bodies", true, JSONObject()),
                EntityKillTask(EntityType.CHICKEN, 24, this, "Kill the Members", true, JSONObject())
        ), this, true, 125.00, "125 cash"))

        quests.add(XpRewardingQuest("Go Mining", listOf(
                BlockBreakTask(Material.IRON_ORE, 64, this, "Mine some Iron", false, JSONObject()),
                BlockBreakTask(Material.GOLD_ORE, 16, this, "Mine some Gold", false, JSONObject()),
                BlockBreakTask(Material.REDSTONE_ORE, 64, this, "Mine some Redstone", false, JSONObject()),
                BlockBreakTask(Material.DIAMOND_ORE, 4, this, "Mine some Gems", false, JSONObject())
        ), this, false, 500L, "500 XP"))

        quests.add(XpRewardingQuest("Defend your Base", listOf(
                EntityKillTask(EntityType.ZOMBIE, 16, this, "Murder some Zombies", false, JSONObject()),
                EntityKillTask(EntityType.SKELETON, 24, this, "Shoot the Skeletons", false, JSONObject()),
                EntityKillTask(EntityType.CREEPER, 12, this, "Destroy the Creepers", false, JSONObject())
        ), this, false, 200L, "200 XP"))

        quests.add(XpRewardingQuest("Create an Army", listOf(
                WolfTameTask(4, this, "Get 4 wolves to create an army", false, JSONObject()),
                EntityKillTask(EntityType.PIG, 16, this, "Feed your Army", false, JSONObject())
        ), this, false, 150L, "150 XP"))

        quests.add(BalanceRewardingQuest("Who rules the underworld again?", listOf(
                EntityKillTask(EntityType.GHAST, 2, this, "Kill 2 ghasts", false, JSONObject()),
                EntityKillTask(EntityType.MANLY_PIG, 6, this, "Get some fresh pork", false, JSONObject())
        ), this, false, 175.00, "$175"))

        quests.add(XpRewardingQuest("Jumping Jacks", listOf(
                JumpTask(100L, this, "Do some Jumping Jacks", false, JSONObject())
        ), this, false, 50L, "50 XP"))

        quests.add(BalanceRewardingQuest("Time for a Marathon", listOf(
                MoveTask(5000, this, "Run 5000 blocks by foot", false, JSONObject())
        ), this, false, 250.00, "$250 dollas"))

        quests.add(XpRewardingQuest("Lets be Pirates", listOf(
                BoatMoveTask(2000L, this, "ARR!!", false, JSONObject()),
                EntityKillTask(EntityType.SQUID, 1, this, "Slay the Kraken", true, JSONObject())
        ), this, false, 300L, "300 XP"))

        quests.add(XpRewardingQuest("Wholesome", listOf(
                BlockBreakTask(Material.WHEAT, 10, this, "Harvest some Wheat", false, JSONObject())
        ), this, false, 50L, "50 XP"))

        saveQuests()
    }

    override fun onDisable() {
        config.save()
        saveQuests()
    }

    override fun onRefresh() {
        config.refreshContent()
    }

    /**
     * Save quests to file.
     */
    fun saveQuests() {
        println("[OSMPL:QUESTS] Saving quests...")

        quests.forEach { quest ->
            pl.server.scheduler.scheduleAsyncDelayedTask(pl) { parser.saveQuest(quest) }
        }
        lastQuestSave = System.currentTimeMillis()

        println("[OSMPL:QUESTS] Quests have been saved!")
    }
}