package dev.shog.osmpl.api.data

import dev.shog.osmpl.api.data.punishments.Punishment

/**
 * User data skeleton. This information may be null or not filled in.
 */
data class DataUser(
        val name: String = "null",
        var ip: String = "null",
        var lastLogIn: Long = -1,
        var lastLogOut: Long = -1,
        var playTime: Long = -1,
        var firstJoin: Long = -1,
        var punishHistory: ArrayList<Punishment> = arrayListOf(),
        var currentBan: Punishment? = null,
        var currentMute: Punishment? = null,
        var kills: Int = 0,
        var deaths: Int = 0
)