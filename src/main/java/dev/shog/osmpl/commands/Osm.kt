package dev.shog.osmpl.commands

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.sendMessageHandler
import org.bukkit.command.CommandSender

/**
 * The OSM command.
 *
 * /osm -> Get the version of OSM-Util.
 * /osm reload -> Reload the configuration.
 */
internal val OSM_COMMAND = Command.make("osm") {
    if (args.isNotEmpty() && args[0].equals("reload", true) && sender.hasPermission("osm.manage.reload")) {
        osmPlugin.configuration.load()
        sendMessageHandler("osm.reloaded")
    } else {
        sendMessageHandler("osm.default", OsmPl.VERSION)
    }

    true
}