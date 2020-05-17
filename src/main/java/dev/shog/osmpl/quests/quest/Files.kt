package dev.shog.osm.quest

import java.io.File

/**
 * The directory where quests are stored.
 */
val DIR by lazy {
    val file = File("questData")

    if (!file.exists())
        file.mkdirs()

    file
}