package dev.shog.osmpl.util

import com.earth2me.essentials.api.Economy
import dev.shog.osmpl.api.OsmModule
import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.api.msg.MessageContainer
import dev.shog.osmpl.api.msg.sendMessageHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import java.util.concurrent.TimeUnit

/**
 * A streak.
 */
object StreakHandler {
    /**
     * Data about a user's streak.
     * @param user The user's name.
     * @param streak The amount of days in a row a user's logged in.
     * @param lastLogin The last time a login upped their streak. This wouldn't be the most recent login, but the most recent time their streak has been increased.
     *
     * Streak's aren't calculated by a calendar day, but by 24 hours past the [lastLogin].
     */
    data class StreakData(
            val user: String,
            val streak: Int,
            val lastLogin: Long
    )

    /**
     * Get the reward amount for a [streak].
     */
    private fun getRewardAmount(streak: Int): Double =
            streak * 5.0

    /**
     * Get a user's streak by the day count.
     */
    fun getStreak(user: String): Int {
        val rs = SqlHandler.getConnection(db = "money")
            .prepareStatement("SELECT streak FROM dailyreward WHERE player = ?")
            .apply {
                setString(1, user.toLowerCase())
            }
            .executeQuery()

        return if (rs.next()) {
            rs.getInt("streak")
        } else {
            0
        }
    }

    /**
     * Handle a user's login event.
     */
    fun handleLogin(loginEvent: PlayerJoinEvent, osmModule: OsmModule) {
        val rs = SqlHandler.getConnection(db = "money")
                .prepareStatement("SELECT streak, lastLogin FROM dailyreward WHERE player = ?")
                .apply { setString(1, loginEvent.player.name.toLowerCase()) }
                .executeQuery()

        if (rs.next()) {
            val streak = StreakData(
                    loginEvent.player.name.toLowerCase(),
                    rs.getInt("streak"),
                    rs.getLong("lastLogin")
            )

            if (System.currentTimeMillis() - streak.lastLogin > TimeUnit.DAYS.toMillis(2)) { // they didn't login within 24 hours
                loginEvent.player.sendMessageHandler(osmModule.messageContainer, "streak.broke", streak.streak)

                SqlHandler.getConnection(db = "money")
                        .prepareStatement("UPDATE dailyreward SET lastLogin = ?, streak = ? WHERE player = ?")
                        .apply {
                            setLong(1, System.currentTimeMillis())
                            setInt(2, 1)
                            setString(3, loginEvent.player.name.toLowerCase())
                        }
                        .executeUpdate()

                Economy.add(loginEvent.player.name, getRewardAmount(1))

                return
            } else {
                if (System.currentTimeMillis() - streak.lastLogin > TimeUnit.DAYS.toMillis(1)) {
                    val amount = getRewardAmount(streak.streak + 1)

                    SqlHandler.getConnection(db = "money")
                            .prepareStatement("UPDATE dailyreward SET lastLogin = ?, streak = ? WHERE player = ?")
                            .apply {
                                setLong(1, System.currentTimeMillis())
                                setInt(2, 1)
                                setString(3, loginEvent.player.name.toLowerCase())
                            }
                            .executeUpdate()

                    Economy.add(loginEvent.player.name, amount)

                    loginEvent.player.sendMessageHandler(osmModule.messageContainer, "streak.default", streak.streak + 1, amount)
                }
            }
        } else {
            loginEvent.player.sendMessageHandler(osmModule.messageContainer, "streak.default", 1, getRewardAmount(1))

            SqlHandler.getConnection(db = "money")
                    .prepareStatement("INSERT INTO dailyreward (lastLogin, streak, player) VALUES (?, ?, ?)")
                    .apply {
                        setLong(1, System.currentTimeMillis())
                        setInt(2, 1)
                        setString(3, loginEvent.player.name.toLowerCase())
                    }
                    .executeUpdate()

            Economy.add(loginEvent.player.name, getRewardAmount(1))
        }
    }
}