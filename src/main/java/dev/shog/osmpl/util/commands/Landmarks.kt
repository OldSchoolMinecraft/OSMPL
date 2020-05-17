package dev.shog.osmpl.util.commands

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.api.cmd.Command
import dev.shog.osmpl.hasPermissionOrOp
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.File

private val landmarkMapper = ObjectMapper()

/**
 * A landmark with a [name].
 */
private data class Landmark(
        val name: String = "EMPTY_LANDMARK",
        val worldName: String = "world",
        val x: Double = 0.0,
        val y: Double = 0.0,
        val z: Double = 0.0,
        val yaw: Float = 0.0F,
        val pitch: Float = 0.0F
)

/**
 * The landmarks. Retrieves from [landmarkFile].
 */
private val landmarks by lazy {
    try {
        landmarkMapper.readValue<ArrayList<Landmark>>(
                landmarkFile,
                landmarkMapper.typeFactory.constructCollectionType(ArrayList::class.java, Landmark::class.java)
        )
    } catch (ex: Exception) {
        arrayListOf<Landmark>()
    }
}

/**
 * landmarks.json
 */
private val landmarkFile by lazy {
    val file = File("landmarks.json")

    if (!file.exists()) {
        file.createNewFile()
        file.outputStream().write("{}".toByteArray())
    }

    file
}

/**
 * Write the landmarks
 */
private fun saveLandmarks() {
    landmarkMapper.writeValue(landmarkFile, landmarks)
}

/**
 * The landmarks command.
 *
 * /hat -> Sets block in hand to your head.
 */
internal val LANDMARKS_COMMAND = Command.make("landmarks") {
    if (sender !is Player) {
        sendMessageHandler("error.console")
        return@make true
    }

    when {
        args.size == 2 && args[0].equals("create", true) && sender.hasPermissionOrOp("osm.landmarks") -> {
            val loc = sender.location
            val name = args[1]

            landmarks.add(Landmark(name, sender.world.name, loc.x, loc.y, loc.z, loc.yaw, loc.pitch))
            saveLandmarks()

            sendMessageHandler("landmarks.create", name, loc.x.toInt(), loc.y.toInt(), loc.z.toInt())
        }

        args.size == 2 && args[0].equals("remove", true) && sender.hasPermissionOrOp("osm.landmarks") -> {
            val landmark = args[1]
            val eq = landmarks.singleOrNull { land -> land.name.equals(landmark, true) }

            if (eq == null) {
                sendMessageHandler("landmarks.not-found", landmark)
            } else {
                landmarks.remove(eq)
                sendMessageHandler("landmarks.remove", landmark)
            }
        }

        args.size == 1 -> {
            val landmark = args[0]
            val eq = landmarks.singleOrNull { land -> land.name.equals(landmark, true) }

            if (eq == null) {
                sendMessageHandler("landmarks.not-found", landmark)
            } else {
                val world = osmModule.pl.server.getWorld(eq.worldName)
                val loc = Location(world, eq.x, eq.y, eq.z, eq.yaw, eq.pitch)

                sender.teleport(loc)
            }
        }

        else -> {
            val names = landmarks
                    .joinToString { "§8${it.name}§7" }
                    .ifBlank { "None" }

            sendMessageHandler("landmarks.view", names)
        }
    }

    true
}