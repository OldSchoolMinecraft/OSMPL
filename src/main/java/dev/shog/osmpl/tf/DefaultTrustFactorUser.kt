package dev.shog.osmpl.tf

import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.tf.inf.TrustFactorUser

/**
 * A default trust factor user. This is created to work with the "trustFactor" table in the "quests" database.
 *
 * @param username The user's username. This is put into lowercase.
 * @param trustFactor The user's trust factor.
 */
class DefaultTrustFactorUser internal constructor(override val username: String, trustFactor: Int): TrustFactorUser {
    companion object {
        /**
         * Get [name] from the database as a [DefaultTrustFactorUser]. If the user doesn't exist, create the user.
         */
        fun getUser(name: String): DefaultTrustFactorUser {
            val sql = SqlHandler.getConnection()

            val rs = sql.prepareStatement("SELECT * FROM `trustFactor` WHERE username=?")
                    .apply { setString(1, name.toLowerCase()) }
                    .executeQuery()

            while (rs.next()) {
                val trust = rs.getInt("trust")

                return DefaultTrustFactorUser(name.toLowerCase(), trust)
            }

            sql.prepareStatement("INSERT INTO `trustFactor` (username, trust) VALUES (?, ?)")
                    .apply {
                        setString(1, name)
                        setInt(2, 0)
                    }
                    .executeUpdate()

            return DefaultTrustFactorUser(name.toLowerCase(), 0)
        }
    }

    /**
     * A user's trust factor. When setting the property
     */
    override var trustFactor: Int = trustFactor
        set(value) {
            val prepared = SqlHandler.getConnection()
                    .prepareStatement("UPDATE `trustFactor` SET `trust`=? WHERE username=?")

            prepared.setInt(1, value)
            prepared.setString(2, username.toLowerCase())

            prepared.execute()

            field = value
        }
}