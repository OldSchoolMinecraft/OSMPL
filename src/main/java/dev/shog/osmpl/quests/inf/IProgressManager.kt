package dev.shog.osmpl.quests.inf

import org.bukkit.entity.Player

/**
 * A task's progress
 */
interface IProgressManager<T> {
    fun getParent(): ITask

    fun getProgress(player: Player): T?

    fun setProgress(player: Player, progress: T)
}