package dev.shog.osmpl.money.user

import dev.shog.osmpl.api.SqlHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * A bank user. This person has savings and is most likely registered to a bank.
 */
class BankUser(val username: String, bank: Int, savings: Double) {
    /**
     * A bank's ID.
     */
    var bank: Int = bank
        set(value) {
            SqlHandler.getConnection(db = "money")
                    .prepareStatement("UPDATE users SET bank = ? WHERE username = ?")
                    .apply {
                        setInt(1, value)
                        setString(2, username)
                    }
                    .executeUpdate()

            field = value
        }

    /**
     * A user's savings.
     */
    var savings: Double = savings
        set(value) {
            SqlHandler.getConnection(db = "money")
                    .prepareStatement("UPDATE users SET savings = ? WHERE username = ?")
                    .apply {
                        setDouble(1, value)
                        setString(2, username)
                    }
                    .executeUpdate()

            field = value
        }

    companion object {
        private val cache = ConcurrentHashMap<String, BankUser>()

        /**
         * Get a user by their [username].
         */
        fun getUser(username: String): BankUser {
            if (cache.containsKey(username))
                return cache[username]!!

            val con = SqlHandler.getConnection(db = "money")

            val rs = con.prepareStatement("SELECT * FROM users WHERE username = ?")
                    .apply { setString(1, username) }
                    .executeQuery()

            val bankUser =  if (rs.next()) {
                BankUser(username, rs.getInt("bank"), rs.getDouble("savings"))
            } else {
                con.prepareStatement("INSERT INTO users (username, bank, savings) VALUES (?, ?, ?)")
                        .apply {
                            setString(1, username)
                            setInt(2, -1)
                            setDouble(3, -1.0)
                        }
                        .executeUpdate()

                BankUser(username, -1, -1.0)
            }

            cache[username] = bankUser
            return bankUser
        }
    }
}