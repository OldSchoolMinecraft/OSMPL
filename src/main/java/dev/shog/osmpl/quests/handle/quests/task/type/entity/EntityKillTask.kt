package dev.shog.osmpl.quests.handle.quests.task.type.entity

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
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
) : QuestTask<HashMap<String, Int>, EntityDeathEvent>(name, quests, donor, data) {
    override val status = HashMap<String, Int>()

    /**
     * If [player] has completed this task.
     */
    override fun isComplete(player: Player): Boolean {
        val current = status[player.name.toLowerCase()] ?: 0

        return current >= amount
    }

    /**
     * The player's status. "0/1 wood blocks placed" etc
     */
    override fun getStatusString(player: Player): String {
        val status = status[player.name.toLowerCase()] ?: 0

        return quests.messageContainer.getMessage("commands.view-quest.status.entity-kill", status.toLong(), amount, entity.entityName)
    }

    override fun formatData(): HashMap<String, Int> {
        val mapper = ObjectMapper()

        return mapper.readValue(
                data.toString(),
                mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Int::class.java)
        )
    }

    override val invokesOn: Event.Type = Event.Type.ENTITY_DEATH

    override fun invoke(event: EntityDeathEvent) {
        if (event.entity::class.java == entity.entityClass) {
            val ldc = event.entity.lastDamageCause

            if (ldc is EntityDamageByEntityEvent) {
                val player = when {
                    ldc.damager is Projectile && (ldc.damager as Projectile).shooter is Player ->
                        (ldc.damager as Projectile).shooter as Player

                    ldc.damager is Player ->
                        ldc.damager as Player

                    else -> null
                }

                if (player != null && canContinue(player)) {
                    val current = status[player.name.toLowerCase()] ?: 0

                    status[player.name.toLowerCase()] = current + 1

                    if (isComplete(player)) {
                        onPlayerComplete.invoke(quests, player)
                    }
                }
            }
        }
    }
}