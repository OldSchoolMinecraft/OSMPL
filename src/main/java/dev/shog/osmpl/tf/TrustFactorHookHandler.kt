package dev.shog.osmpl.tf

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.tf.inf.TrustFactorHook
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockListener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * This manages hooks for trust factor.
 */
object TrustFactorHookHandler {
    enum class RequiredHookTypes {
        MOVE, BREAK, PLACE, KILL
    }

    val mapper = ObjectMapper()
    private val hooks = hashMapOf<RequiredHookTypes, Class<*>>()

    /**
     * The trust factor progress holder.
     */
    private val folder = File("tfProgress")

    val moveFile = File("${folder.path}${File.separator}move.json")
    val breakFile = File("${folder.path}${File.separator}break.json")
    val placeFile = File("${folder.path}${File.separator}place.json")
    val killFile = File("${folder.path}${File.separator}kill.json")

    init {
        folder.mkdir()

        sequenceOf(breakFile, moveFile, placeFile, killFile).forEach { file ->
            file.createNewFile()
            file.outputStream().write("{}".toByteArray())
        }
    }


    /**
     * A player's username and their movement progress.
     */
    val moveProgress = object: HashMap<String, Double>() {
        init {
            mapper.readValue<HashMap<String, Double>>(
                    moveFile,
                    mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Double::class.java)
            )
        }
    }

    /**
     * A player's username and how many blocks they're broken.
     */
    val breakProgress = object: HashMap<String, Long>() {
        init {
            mapper.readValue<HashMap<String, Long>>(
                    breakFile,
                    mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Long::class.java)
            )
        }
    }

    /**
     * A player's username and how many blocks they've placed.
     */
    val placeProgress = object: HashMap<String, Long>() {
        init {
            mapper.readValue<HashMap<String, Long>>(
                    placeFile,
                    mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Long::class.java)
            )
        }
    }

    /**
     * A player's username and how many entities they've killed.
     */
    val killProgress = object: HashMap<String, Long>() {
        init {
            mapper.readValue<HashMap<String, Long>>(
                    killFile,
                    mapper.typeFactory.constructMapType(HashMap::class.java, String::class.java, Long::class.java)
            )
        }
    }

    /**
     * Save progress to file.
     */
    fun save() {
        moveFile.outputStream().write(mapper.writeValueAsString(moveProgress).toByteArray())
        breakFile.outputStream().write(mapper.writeValueAsString(breakProgress).toByteArray())
        placeFile.outputStream().write(mapper.writeValueAsString(placeProgress).toByteArray())
        killFile.outputStream().write(mapper.writeValueAsString(killProgress).toByteArray())
    }

    /**
     * Fill the remaining [hooks].
     */
    fun fillRemaining(plugin: JavaPlugin) {
        if (!hooks.containsKey(RequiredHookTypes.MOVE)) {
            plugin.server.pluginManager.registerEvent(Event.Type.PLAYER_MOVE, object: PlayerListener() {
                override fun onPlayerMove(event: PlayerMoveEvent?) {
                    if (event != null) {
                        invokeHook(RequiredHookTypes.MOVE, TrustFactorHookHandler::class.java, event)
                    }
                }
            }, Event.Priority.Normal, plugin)
        }

        if (!hooks.containsKey(RequiredHookTypes.BREAK)) {
            plugin.server.pluginManager.registerEvent(Event.Type.BLOCK_BREAK, object: BlockListener() {
                override fun onBlockBreak(event: BlockBreakEvent?) {
                    if (event != null) {
                        invokeHook(RequiredHookTypes.BREAK, TrustFactorHookHandler::class.java, event)
                    }
                }
            }, Event.Priority.Normal, plugin)
        }

        if (!hooks.containsKey(RequiredHookTypes.PLACE)) {
            plugin.server.pluginManager.registerEvent(Event.Type.BLOCK_PLACE, object: BlockListener() {
                override fun onBlockPlace(event: BlockPlaceEvent?) {
                    if (event != null) {
                        invokeHook(RequiredHookTypes.PLACE, TrustFactorHookHandler::class.java, event)
                    }
                }
            }, Event.Priority.Normal, plugin)
        }

        if (!hooks.containsKey(RequiredHookTypes.KILL)) {
            plugin.server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object: EntityListener() {
                override fun onEntityDeath(event: EntityDeathEvent?) {
                    if (event != null) {
                        invokeHook(RequiredHookTypes.KILL, TrustFactorHookHandler::class.java, event)
                    }
                }
            }, Event.Priority.Normal, plugin)
        }
    }

    /**
     * Invoke a hook.
     *
     * @param self The self class.
     */
    fun invokeHook(hook: RequiredHookTypes, self: Class<*>, event: Event) {
        if (hooks.containsKey(hook) && hooks[hook] != self)
            return

        when (hook) {
            RequiredHookTypes.MOVE -> {
                if (event is PlayerMoveEvent) {
                    val distance = event.from.distance(event.to)
                    val userName = event.player.name.toLowerCase()
                    val newDistance =  (moveProgress[userName] ?: 0.0) + distance

                    moveProgress[userName] = newDistance

                    if (newDistance >= 10000) {
                        DefaultTrustFactorHandler.giveTrust(userName, 50) // ADD 50 EVERY 10000 BLOCKS
                        moveProgress[userName] = 0.0
                    }
                }
            }

            RequiredHookTypes.BREAK -> {
                if (event is BlockBreakEvent) {
                    val userName = event.player.name.toLowerCase()
                    val newDistance = (breakProgress[userName] ?: 0) + 1

                    breakProgress[userName] = newDistance

                    if (newDistance >= 1000) {
                        DefaultTrustFactorHandler.giveTrust(userName, 10) // ADD 10 EVERY 1000 BLOCKS
                        breakProgress[userName] = 0
                    }
                }
            }

            RequiredHookTypes.PLACE -> {
                if (event is BlockPlaceEvent) {
                    val userName = event.player.name.toLowerCase()
                    val newDistance = (placeProgress[userName] ?: 0) + 1

                    placeProgress[userName] = newDistance

                    if (newDistance >= 500) {
                        DefaultTrustFactorHandler.giveTrust(userName, 5) // ADD 10 EVERY 500 BLOCKS
                        placeProgress[userName] = 0
                    }
                }
            }

            RequiredHookTypes.KILL -> {
                if (event is EntityDeathEvent && event.entity.lastDamageCause is EntityDamageByEntityEvent) {
                    val lastDamage = event.entity.lastDamageCause as EntityDamageByEntityEvent

                    if (lastDamage.damager is Player) {
                        val player = lastDamage.damager as Player

                        val userName = player.name.toLowerCase()
                        val newDistance = (killProgress[userName] ?: 0) + 1

                        killProgress[userName] = newDistance

                        if (newDistance >= 100) {
                            DefaultTrustFactorHandler.giveTrust(userName, 10) // ADD 10 EVERY 100 KILLS
                            killProgress[userName] = 0
                        }
                    }
                }
            }
        }
    }
}