package dev.shog.osmpl.quests.impl

import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.quests.inf.IProgressManager
import dev.shog.osmpl.quests.inf.ITask
import org.bukkit.entity.Player

class ProgressManager<T>(private val task: ITask) : IProgressManager<T> {
    override fun getParent(): ITask =
            task

    override fun getProgress(player: Player): T? {
        val rs = SqlHandler.getConnection(db = "quests")
                .prepareStatement("SELECT progress FROM progress WHERE quest = ? AND username = ?")
                .apply {
                    setString(1, task.id.toString())
                    setString(2, player.name)
                }
                .executeQuery()

        return if (rs.next())
            rs.getObject("progress") as? T
        else null
    }

    override fun setProgress(player: Player, progress: T) {
        SqlHandler.getConnection(db = "quests")
                .prepareStatement("UPDATE progress SET progress = ? WHERE quest = ? AND username = ?")
                .apply {
                    setObject(1, progress)
                    setString(2, task.id.toString())
                    setString(3, player.name)
                }
                .executeUpdate()
    }
}