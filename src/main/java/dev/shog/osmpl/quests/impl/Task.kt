package dev.shog.osmpl.quests.impl

import dev.shog.osmpl.quests.inf.IProgressManager
import dev.shog.osmpl.quests.inf.IQuest
import dev.shog.osmpl.quests.inf.ITask
import java.util.*

abstract class Task(override val parent: IQuest, override val name: String) : ITask {
    override val progressManager: IProgressManager<Number> = ProgressManager(this)

    override val id: UUID = UUID.randomUUID()
}