package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler
import dev.shog.osmpl.util.UtilModule

/**
 * Slowmode command
 */
internal val SLOWMODE_COMMAND = Command.make("slowmode") {
        when {
            args.isEmpty() -> {
                if (UtilModule.slowMode.enabled) {
                    sendMessageHandler("slowmode.default", UtilModule.slowMode.timing)
                } else {
                    sendMessageHandler("slowmode.disabled")
                }

                true
            }

            args.size == 2 && args[0].equals("temp", true) -> {
                val sec = args[1].toLongOrNull()

                if (sec == null)
                    sendMessageHandler("slowmode.valid-number")
                else {
                    UtilModule.slowMode.timing = sec
                    sendMessageHandler("slowmode.temp-set", sec)
                }

                true
            }

            args.size == 1 && args[0].equals("toggle", true) -> {
                val new = !UtilModule.slowMode.enabled

                UtilModule.slowMode.enabled = new

                sendMessageHandler("slowmode.toggle", if (new) "enabled" else "disabled")

                true
            }

            args.size == 2 && args[0].equals("set", true) -> {
                val sec = args[1].toLongOrNull()

                if (sec == null)
                    sendMessageHandler("slowmode.valid-number")
                else {
                    UtilModule.slowMode.timing = sec
                    osmModule.config.content.put("slowModeInSec", sec)
                    osmModule.config.save()

                    sendMessageHandler("slowmode.perm-set", sec)
                }

                true
            }

            else -> false
        }
    }