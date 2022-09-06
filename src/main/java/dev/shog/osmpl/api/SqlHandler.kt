package dev.shog.osmpl.api

import com.mysql.cj.jdbc.MysqlDataSource
import java.sql.Connection

/**
 * Manages SQL.
 */
internal object SqlHandler {
    var url: String = ""
    var username: String = ""
    var password: String = ""

    /**
     * Init [url], [username], [password].
     */
    fun initValues(pl: OsmPlugin) {
        url = pl.configuration.getString("url")
        username = pl.configuration.getString("username")
        password = pl.configuration.getString("password")
    }

    /**
     * Form a connection using [url], [username], and [password].
     */
    fun getConnection(db: String = "quests"): Connection {
        if (url == "" || username == "" || password == "")
            throw Exception("SQL credentials are not properly filled out")
        else {
            val source = MysqlDataSource()

            source.user = username
            source.password = password
            source.serverName = url
            source.databaseName = db

            val con = source.connection

            if (con.isValid(5))
                return con
            else throw Exception("Failed connecting to SQL")
        }
    }
}