package dev.shog.osmpl.api.data

import dev.shog.osmpl.api.data.punishments.Punishment

/**
 * A user with confirmed filled data.
 */
class User(internal val dataUser: DataUser) {
    init {
        if (dataUser.name == "null" || dataUser.ip == "null") {
            System.err.println("Cannot successfully transfer Data User to User for User ${dataUser.name}")
        }
    }

    val name: String = dataUser.name

    val ip: String = dataUser.ip

    var kills: Int = dataUser.kills
        set(value) {
            field = value
            dataUser.kills = value
            DataManager.saveUser(this)
        }

    var deaths: Int = dataUser.deaths
        set(value) {
            field = value
            dataUser.deaths = value
            DataManager.saveUser(this)
        }

    var currentBan: Punishment? = dataUser.currentBan
        set(value) {
            field = value
            dataUser.currentBan = value
            DataManager.saveUser(this)
        }

    var currentMute: Punishment? = dataUser.currentMute
        set(value) {
            field = value
            dataUser.currentMute = value
            DataManager.saveUser(this)
        }

    var punishments: ArrayList<Punishment> = dataUser.punishHistory
        set(value) {
            field = value
            dataUser.punishHistory = value
            DataManager.saveUser(this)
        }

    val firstJoin: Long = dataUser.firstJoin

    var lastLogOut: Long = dataUser.lastLogOut
        set(value) {
            field = value
            dataUser.lastLogOut = value
            DataManager.saveUser(this)
        }

    var lastLogIn: Long = dataUser.lastLogIn
        set(value) {
            field = value
            dataUser.lastLogIn = value
            DataManager.saveUser(this)
        }

    var playTime: Long = dataUser.playTime
        set(value) {
            field = value
            dataUser.playTime = value
            DataManager.saveUser(this)
        }

    fun isBanned() =
            currentBan != null

    fun isMuted() =
            currentMute != null

    var lastWild: Long = dataUser.lastWild
        set(value) {
            field = value
            dataUser.lastWild = value
            DataManager.saveUser(this)
        }

    var ignoreBroadcast: Boolean = dataUser.ignoreBroadcast
        set(value) {
            field = value
            dataUser.ignoreBroadcast = value
            DataManager.saveUser(this)
        }

    override fun toString(): String {
        return dataUser.toString()
    }
}