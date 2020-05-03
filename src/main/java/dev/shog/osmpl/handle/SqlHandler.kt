package dev.shog.osmpl.handle

import com.mysql.cj.jdbc.MysqlDataSource
import java.lang.Exception
import java.sql.Connection

/**
 * Manages SQL.
 */
internal object SqlHandler {
    var url: String = ""
    var username: String = ""
    var password: String = ""

    /**
     * Form a connection using [url], [username], and [password].
     */
    fun getConnection(): Connection {
        if (url == "" || username == "" || password == "")
            throw Exception("SQL credentials are not properly filled out")
        else {
            val source = MysqlDataSource()

            source.user = username
            source.password = password
            source.serverName = url
            source.databaseName = "quests"

            val con = source.connection

            if (con.isValid(5))
                return con
            else throw Exception("Failed connecting to SQL")
        }
    }
}