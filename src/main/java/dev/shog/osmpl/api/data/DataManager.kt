package dev.shog.osmpl.api.data

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.osmpl.api.data.punishments.Punishment
import dev.shog.osmpl.api.data.punishments.PunishmentType
import dev.shog.osmpl.defaultFormat
import dev.shog.osmpl.fancyDate
import dev.shog.osmpl.sendWebhookMessage
import dev.shog.osmpl.tf.DefaultTrustFactorHandler
import org.bukkit.Server
import org.bukkit.entity.Player
import java.io.File

object DataManager {
    /**
     * Users
     */
    val data: MutableList<User> by lazy {
        val users = LOCATION.listFiles()

        if (users != null) {
            val mapper = ObjectMapper()
            val dataUsers = mutableListOf<DataUser>()

            users.forEach { file ->
                try {
                    val user = mapper.readValue(file, DataUser::class.java)

                    dataUsers.add(user)
                } catch (e: Exception) {
                    System.err.println("There was an issue reading the file for user: \"${file.nameWithoutExtension}\"")
                }
            }

            dataUsers.asSequence().map { it.getUser() }.toMutableList()
        } else {
            System.err.println("There was an issue loading player data!")
            mutableListOf()
        }
    }

    private val LOCATION by lazy {
        val folder = File("playerdata")

        if (!folder.exists())
            folder.mkdirs()

        folder
    }

    /**
     * Save the player data to file.
     */
    fun saveAll() {
        val mapper = ObjectMapper()

        synchronized(data) {
            for (user in data) {
                saveUser(user, mapper)
            }
        }
    }

    /**
     * Save [user] to file.
     */
    fun saveUser(user: User, mapper: ObjectMapper = ObjectMapper()) {
        val file = user.dataUser.getFile()

        if (data.contains(user)) {
            data.remove(user)
            data.add(user)
        } else data.add(user)

        try {
            mapper.writeValue(file, user.dataUser)
        } catch (ex: Exception) {
            System.err.println("Failed to save ${user.name}'s data")
        }
    }

    /**
     * If a user is banned.
     *
     * @param user The username of the user to check.
     * @return If the user is banned.
     */
    fun isUserBanned(user: String): Boolean =
            getUserData(user)?.isBanned() == true

    /**
     * If a user is muted.
     *
     * @param user The username of the user to check.
     * @return If the user is muted.
     */
    fun isUserMuted(user: String): Boolean =
            getUserData(user)?.isMuted() == true

    /**
     * If an IP is banned.
     *
     * @param ip The IP to check for.
     * @return If the IP is banned.
     */
    fun isIpBanned(ip: String): Boolean =
            data.asSequence()
                    .filter { user -> user.isBanned() }
                    .any { user -> user.ip != "" && user.ip == ip }

    /**
     * Get a user's data.
     *
     * @param name The user's name
     * @return The data user instance.
     */
    fun getUserData(name: String): User? =
            data.asSequence()
                    .filter { user -> user.name.equals(name, true) }
                    .firstOrNull()

    /**
     * Get a user's data, or create it.
     *
     * @param name The user's name.
     * @param server The server instance. This allows it to grab the user's data from the online players.
     * @return A user instance.
     */
    fun getOrCreate(name: String, server: Server): User? {
        val user = getUserData(name)

        if (user != null)
            return user

        val player = server.onlinePlayers
                .singleOrNull { player -> player.name.equals(name, true) }
                ?: return null

        val ip = player
                .address
                ?.hostString
                ?: ""

        val dataUser = DataUser(
                name.toLowerCase(),
                ip,
                System.currentTimeMillis(),
                -1,
                0,
                System.currentTimeMillis(),
                arrayListOf(),
                null
        )

        val userInst = dataUser.getUser()

        saveUser(userInst)

        return userInst
    }

    /**
     * Register a user.
     *
     * @param player The player to register.
     * @return The data user created from the user.
     */
    fun registerUser(player: Player): User {
        val data = DataUser(
                name = player.name.toLowerCase(),
                ip = player.address?.hostString ?: throw Exception("An IP could not be resolved for ${player.name}"),
                lastLogIn = System.currentTimeMillis(),
                lastLogOut = -1,
                playTime = 0,
                firstJoin = System.currentTimeMillis(),
                punishHistory = arrayListOf(),
                currentBan = null
        )

        val user = data.getUser()

        saveUser(user)

        return user
    }

    /**
     * Get if a user exists
     *
     * @param user The user's name
     * @return If the user exists.
     */
    fun userExists(user: String): Boolean =
            getUserData(user) != null

    /**
     * Punish a user.
     *
     * @param user The user to punish
     * @param punishment The punishment to give. If this is a ban, all people on the same IP are banned.
     */
    fun punishUser(user: String, admin: String, punishment: Punishment) {
        val data = getUserData(user.toLowerCase())

        sendWebhookMessage("Username: `${user}`, " +
                "Punishment: `${punishment.type}`, " +
                "Reason: `${punishment.reason}`, " +
                "Expire: `${
                    if (punishment.expire == -1L)
                        "Permanent" 
                    else punishment.expire.defaultFormat()
                }`, " +
                "Period: `${
                    if (punishment.expire == -1L) 
                        "Forever" 
                    else (punishment.expire - System.currentTimeMillis()).fancyDate()
                }`", admin)

        if (data != null) {
            val punishments = data.punishments

            punishments.add(punishment)

            data.punishments = punishments

            DefaultTrustFactorHandler.handlePunishment(user, punishment.type, punishment.expire == -1L) // handle TF update

            when (punishment.type) {
                PunishmentType.BAN -> data.currentBan = punishment

                PunishmentType.MUTE -> data.currentMute = punishment
            }
        }
    }
}