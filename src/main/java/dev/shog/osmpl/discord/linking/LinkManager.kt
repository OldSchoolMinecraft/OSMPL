package dev.shog.osmpl.discord.linking

import dev.shog.osmpl.api.SqlHandler.getConnection
import dev.shog.osmpl.api.SqlHandler
import dev.shog.osmpl.generateRandomString
import java.lang.Exception

object LinkManager {
    fun isLinked(discord_id: String?): Boolean {
        return try {
            val con = getConnection("hydra")
            val stmt = con.prepareStatement("SELECT * FROM dc_links WHERE discord_id = ?")
            stmt.setString(1, discord_id)
            if (stmt.execute())
                return stmt.resultSet.fetchSize > 0
            false
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun commitVerificationCode(discord_id: String?, code: String?): Boolean {
        return try {
            val con = getConnection("hydra")
            val stmt = con.prepareStatement("INSERT INTO dc_verify (discord_id, code) VALUES (?, ?)")
            stmt.setString(1, discord_id)
            stmt.setString(2, code)
            stmt.execute()
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}