package dev.shog.osmpl.commands

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.commands.impl.OsmCommand
import dev.shog.osmpl.sendMessageHandler

/**
 * Slowmode command
 */
internal val SLOWMODE_COMMAND = Command.make("slowmode") {
        when {
            args.isEmpty() -> {
                if (OsmPl.slowMode.enabled) {
                    sendMessageHandler("slowmode.default", OsmPl.slowMode.timing)
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
                    OsmPl.slowMode.timing = sec
                    sendMessageHandler("slowmode.temp-set", sec)
                }

                true
            }

            args.size == 1 && args[0].equals("toggle", true) -> {
                val new = !OsmPl.slowMode.enabled

                OsmPl.slowMode.enabled = new

                sendMessageHandler("slowmode.toggle", if (new) "enabled" else "disabled")

                true
            }

            args.size == 2 && args[0].equals("set", true) -> {
                val sec = args[1].toLongOrNull()

                if (sec == null)
                    sendMessageHandler("slowmode.valid-number")
                else {
                    OsmPl.slowMode.timing = sec
                    osmPlugin.configuration.setProperty("slowModeInSec", sec)
                    osmPlugin.configuration.save()

                    sendMessageHandler("slowmode.perm-set", sec)
                }

                true
            }

            else -> false
        }
    }