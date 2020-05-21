package dev.shog.osmpl.quests.inf

import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.*

interface ITask {
    val parent: IQuest
    val name: String
    val listener: ITaskListener<Event>
    val progressManager: IProgressManager<Number>
    val id: UUID

    fun onComplete(player: Player)

    fun getStatusString(player: Player)

    fun isComplete(player: Player): Boolean
}