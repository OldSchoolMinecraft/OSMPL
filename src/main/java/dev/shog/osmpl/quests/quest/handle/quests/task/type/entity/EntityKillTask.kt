package dev.shog.osmpl.quests.quest.handle.quests.task.type.entity

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.quest.handle.quests.Quest
import dev.shog.osmpl.quests.quest.handle.quests.task.QuestTask
import dev.shog.osmpl.quests.Quests
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.json.JSONObject

/**
 * A entity kill event.
 * You must kill an [entity] [amount] amount of times.
 */
class EntityKillTask(
        val entity: EntityType,
        val amount: Int,
        quests: Quests,
        name: String,
        donor: Boolean,
        data: JSONObject
) : QuestTask(name, quests, donor, data) {
    private val status: HashMap<String, Int>
    override val identifier: String = "ENTITY_KILL"

    init {
        status = if (!data.isEmpty) {
            val mapper = ObjectMapper()

            mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
            )
        } else hashMapOf()

        quests.pl.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
            override fun onEntityDeath(event: EntityDeathEvent?) {
                if (event != null && event.entity::class.java == entity.entityClass) {
                    val ldc = event.entity.lastDamageCause

                    if (ldc is EntityDamageByEntityEvent) {
                        val player = when {
                            ldc.damager is Projectile && (ldc.damager as Projectile).shooter is Player ->
                                (ldc.damager as Projectile).shooter as Player

                            ldc.damager is Player ->
                                ldc.damager as Player

                            else -> null
                        }

                        if (player != null && userOk(player) && !isComplete(player)) {
                            val current = status[player.name.toLowerCase()] ?: 0

                            status[player.name.toLowerCase()] = current + 1

                            if (isComplete(player)) {
                                onPlayerComplete.invoke(quests, player)
                            }
                        }
                    }
                }
            }
        }, Event.Priority.Low, quests.pl)
    }

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0

        return current >= amount
    }

    /**
     * The save data for the task.
     */
    override fun getSaveData(quest: Quest): JSONObject {
        val mapper = ObjectMapper()

        return JSONObject(mapper.writeValueAsString(status))
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.entity-kill", status.toLong(), amount, entity.entityName)
    }
}