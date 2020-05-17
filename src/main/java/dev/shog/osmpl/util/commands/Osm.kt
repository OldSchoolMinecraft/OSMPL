package dev.shog.osmpl.util.commands

import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.api.msg.sendMessageHandler

/**
 * The OSM command.
 *
 * /osm -> Get the version of OSM-Util.
 * /osm reload -> Reload the configuration.
 */
internal val OSM_COMMAND = Command.make("osm") {
    if (args.isNotEmpty() && args[0].equals("reload", true) && sender.hasPermission("osm.manage.reload")) {
        osmModule.pl.refreshModules()
        sendMessageHandler("osm.reloaded")
    } else {
        sendMessageHandler("osm.default", 1.0F)
    }

    true
}