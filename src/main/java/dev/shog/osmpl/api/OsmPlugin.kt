package dev.shog.osmpl.api

import dev.shog.osmpl.api.cmd.CommandContext
import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin

abstract class OsmPlugin : JavaPlugin() {
    /**
     * A hash map of if a module is enabled.
     */
    abstract val modules: HashMap<OsmModule, Boolean>

    /**
     * The required configuration keys.
     */
    abstract val requiredConfig: Collection<String>

    override fun onEnable() {
        configuration.load()

        if (!configuration.keys.containsAll(requiredConfig)) {
            System.err.println("[OSMPL] Disabling plugin due to miss-filled config. Requires: $requiredConfig")

            pluginLoader.disablePlugin(this)
            return
        }

        getEnabledModules().forEach { m ->
            server.scheduler.scheduleAsyncDelayedTask(this) {
                enableModule(m)
            }
        }
    }

    override fun onDisable() {
        getEnabledModules().forEach { m ->
            server.scheduler.scheduleAsyncDelayedTask(this) {
                disableModule(m)
            }
        }
    }

    /**
     * Refresh a module
     */
    fun refreshModule(module: OsmModule) {
        try {
            module.onRefresh()
        } catch (ex: Exception) {
            System.err.println("[OSMPL] There was an issue disabling ${module.name}")
            modules[module] = false
        }
    }

    /**
     * Disable a module.
     */
    fun disableModule(module: OsmModule) {
        try {
            module.onDisable()
            unRegisterCommands(module)

            modules[module] = false
        } catch (ex: Exception) {
            System.err.println("[OSMPL] There was an issue disabling ${module.name}")
            modules[module] = false
        }
    }

    /**
     * Enable a module.
     */
    fun enableModule(module: OsmModule) {
        try {
            module.onEnable()
            registerCommands(module)

            if (modules[module] == false)
                modules[module] = true
        } catch (ex: Exception) {
            System.err.println("[OSMPL] There was an issue enabling ${module.name}")
            modules[module] = false

            ex.printStackTrace()
        }
    }

    /**
     * Get all enabled modules.
     */
    fun getEnabledModules(): List<OsmModule> =
            modules.filterValues { it }.map { it.key }

    /**
     * Refresh all enabled modules.
     */
    fun refreshModules() {
        getEnabledModules().forEach {
            server.scheduler.scheduleAsyncDelayedTask(this) {
                try {
                    it.onRefresh()
                } catch (ex: Exception) {
                    System.err.println("[OSMPL] There was an issue refreshing ${it.name}!")
                }
            }
        }
    }

    /**
     * Register [module]'s commands.
     */
    fun registerCommands(module: OsmModule) {
        module.commands.forEach { osmCmd ->
            val command = module.pl.getCommand(osmCmd.name)

            if (command == null)
                System.err.println("[OSMPL] The command '${osmCmd.name}' was null!")
            else {
                command.executor = CommandExecutor { sender, cmd, _, args ->
                    osmCmd.execute(
                            CommandContext(sender, args.toList(), cmd, module, module.messageContainer)
                    )
                }
            }
        }
    }

    /**
     * Unregister [module]'s commands.
     */
    fun unRegisterCommands(module: OsmModule) {
        module.commands.forEach { osmCmd ->
            module.pl.getCommand(osmCmd.name).executor = null
        }
    }
}