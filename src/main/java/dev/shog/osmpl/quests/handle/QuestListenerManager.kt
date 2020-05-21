package dev.shog.osmpl.quests.handle

import dev.shog.osmpl.quests.Quests
import dev.shog.osmpl.quests.handle.quests.Quest
import dev.shog.osmpl.quests.handle.quests.task.QuestTask
import dev.shog.osmpl.tf.TrustFactorHookHandler
import dev.shog.osmpl.tf.inf.TrustFactorHook
import net.minecraft.server.EntityList
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockListener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerMoveEvent

object QuestListenerManager {
    private val hooks = HashMap<Event.Type, (Event) -> Unit>()

    /**
     * Register a task into [hooks].
     */
    fun registerQuestTask(task: QuestTask<*, *>) {
        task as QuestTask<*, Event>

        hooks[task.invokesOn] = { task.invoke(it) }
    }

    /**
     * Register all listeners using a [Quests] instance.
     *
     * When a listener is invoked, [hooks] is searched through for the proper type then it invokes it.
     */
    fun initHooks(quests: Quests) {
        quests.pl.server.pluginManager.registerEvent(Event.Type.BLOCK_BREAK, object : BlockListener() {
            override fun onBlockPlace(event: BlockPlaceEvent?) {
                if (event != null) {
                    TrustFactorHookHandler.invokeHook(TrustFactorHookHandler.RequiredHookTypes.PLACE, this::class.java, event)

                    hooks.filterKeys { it == Event.Type.BLOCK_PLACE }
                            .forEach { (_, v) -> v.invoke(event)}
                }
            }

            override fun onBlockBreak(event: BlockBreakEvent?) {
                if (event != null) {
                    TrustFactorHookHandler.invokeHook(TrustFactorHookHandler.RequiredHookTypes.BREAK, this::class.java, event)

                    hooks.filterKeys { it == Event.Type.BLOCK_BREAK }
                            .forEach { (_, v) -> v.invoke(event)}
                }
            }
        }, Event.Priority.Low, quests.pl)

        quests.pl.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object : PlayerListener() {
            override fun onPlayerMove(event: PlayerMoveEvent?) {
                if (event != null) {
                    TrustFactorHookHandler.invokeHook(TrustFactorHookHandler.RequiredHookTypes.MOVE, this::class.java, event)

                    hooks.filterKeys { it == Event.Type.PLAYER_MOVE }
                            .forEach { (_, v) -> v.invoke(event)}
                }
            }
        }, Event.Priority.Low, quests.pl)


        quests.pl.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
            override fun onEntityDeath(event: EntityDeathEvent?) {
                if (event != null) {
                    TrustFactorHookHandler.invokeHook(TrustFactorHookHandler.RequiredHookTypes.KILL, this::class.java, event)

                    hooks.filterKeys { it == Event.Type.ENTITY_DEATH }
                            .forEach { (_, v) -> v.invoke(event)}
                }
            }
        }, Event.Priority.Low, quests.pl)
    }
}