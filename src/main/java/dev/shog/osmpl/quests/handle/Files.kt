package dev.shog.osmpl.quests.handle

import java.io.File

/**
 * The directory where quests are stored.
 */
val QUEST_DATA_DIR by lazy {
    val file = File("questData")

    if (!file.exists())
        file.mkdirs()

    file
}