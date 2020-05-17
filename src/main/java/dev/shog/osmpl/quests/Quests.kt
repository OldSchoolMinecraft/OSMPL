package dev.shog.osmpl.quests

import dev.shog.osmpl.quests.quest.handle.parser.QuestParser
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.questss.quest.handle.ranks.QuestLadder
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.OsmPlugin
import dev.shog.osmpl.api.cfg.Configuration
import dev.shog.osmpl.api.msg.MessageContainer
import java.util.*

class Quests(pl: OsmPlugin) : OsmModule("Quests", 1.0F, pl) {
    override val messageContainer: MessageContainer = MessageContainer.fromFile("messages/quests.json")
    override val config: Configuration = Configuration(this)
    var lastQuestSave = System.currentTimeMillis()
    var quests = LinkedList<Quest>()

    val parser = QuestParser(this)
    val ladder = QuestLadder(this)

    override fun onEnable() {
        quests = parser.getAllQuests()
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

        quests.forEach { quest -> parser.saveQuest(quest) }
        lastQuestSave = System.currentTimeMillis()

        println("[OSMPL:QUESTS] Quests have been saved!")
    }
}