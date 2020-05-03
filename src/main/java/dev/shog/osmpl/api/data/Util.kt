package dev.shog.osmpl.api.data

import dev.shog.osmpl.api.data.punishments.Punishment
import java.io.File

fun DataUser.getFile(): File =
        File("playerdata/${name.toLowerCase()}.json")

fun DataUser.deleteUser() =
        getFile().delete()

fun User.deleteUser() =
        dataUser.deleteUser()

fun DataUser.getUser(): User =
        User(this)

fun DataUser.isBanned(): Boolean =
        currentBan != null

fun Punishment.isExpired(): Boolean =
        if (expire != -1L)
            System.currentTimeMillis() - expire > 0
        else false