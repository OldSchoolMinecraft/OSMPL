package dev.shog.osmpl.mm.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.ChatColor

val MODULE_COMMAND = Command.make("module") {
    if (args.isEmpty()) {
        sendMessageHandler("module.list", osmModule.pl.modules.asSequence().joinToString {
            if (it.value) "${ChatColor.GREEN}${it.key.name}" else "${ChatColor.RED}${it.key.name}"
        })

        return@make true
    }

    if (args.size != 2)
        return@make false

    when (args[0].toLowerCase()) {
        "enable" -> {
            val module = osmModule.pl.modules.asSequence().singleOrNull { it.key.name.equals(args[1], true) && !it.value }

            if (module == null)
                sendMessageHandler("module.enable-not-found")
            else {
                osmModule.pl.enableModule(module.key)
                sendMessageHandler("module.enabled", module.key.name)
            }

            return@make true
        }

        "refresh" -> {
            val module = osmModule.pl.modules.asSequence().singleOrNull { it.key.name.equals(args[1], true) && it.value }

            if (module == null)
                sendMessageHandler("module.disable-not-found")
            else {
                osmModule.pl.refreshModule(module.key)
                sendMessageHandler("module.refreshed", module.key.name)
            }

            return@make true
        }

        "disable" -> {
            val module = osmModule.pl.modules.asSequence().singleOrNull { it.key.name.equals(args[1], true) && it.value }

            if (module == null)
                sendMessageHandler("module.disable-not-found")
            else {
                osmModule.pl.disableModule(module.key)
                sendMessageHandler("module.disabled", module.key.name)
            }

            return@make true
        }
    }

    false
}