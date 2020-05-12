package dev.shog.osmpl.remote

import dev.shog.osmpl.OsmPl
import dev.shog.osmpl.handle.SqlHandler
import fi.iki.elonen.NanoHTTPD
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * This handles: localhost:8010/restart
 */
class RemoteRestart(private val pl: OsmPl) : NanoHTTPD(8010) {
    init {
        start(SOCKET_READ_TIMEOUT, false)
    }

    override fun serve(session: IHTTPSession?): Response {
        if (session != null) {
            return when {
                session.uri.equals("/restart", true) && session.method == Method.POST -> {
                    if (session.remoteIpAddress != "127.0.0.1")
                        return newFixedLengthResponse(
                                Response.Status.UNAUTHORIZED,
                                "application/json",
                                "{\"msg\": \"You cannot access this!\"}"
                        )

                    val postData = getPostData(session)

                    if (!postData.containsKey("username") || !postData.containsKey("password")) {
                        return newFixedLengthResponse(
                                Response.Status.UNAUTHORIZED,
                                "application/json",
                                "{\"msg\": \"Invalid username or password. \"}"
                        )
                    }

                    val username = postData["username"]!!
                    val password = postData["password"]!!

                    val rs = SqlHandler.getConnection("accounts")
                            .prepareStatement("SELECT * FROM user WHERE username = ?")
                            .apply { setString(1, username) }
                            .executeQuery()

                    if (rs.next()) {
                        val sqlPass = rs.getString("password")

                        if (rs.getInt("admin") == 1 && sqlPass.equals(password, true)) {
                            pl.server.broadcastMessage(pl.defaultMessageContainer.getMessage("restart"))

                            Timer().schedule(timerTask {
                                pl.server.shutdown()
                            }, 60 * 1000)

                            return newFixedLengthResponse(
                                    Response.Status.OK,
                                    "application/json",
                                    "{\"msg\": \"OK\"}"
                            )
                        }
                    }

                    return newFixedLengthResponse(
                            Response.Status.UNAUTHORIZED,
                            "application/json",
                            "{\"msg\": \"Invalid username or password. \"}"
                    )
                }

                else ->
                    newFixedLengthResponse(
                            Response.Status.NOT_FOUND,
                            "application/json",
                            "{\"msg\": \"Resource not found.\"}"
                    )
            }
        }

        return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                "{\"msg\": \"Invalid session.\"}"
        )
    }

    private fun getPostData(ses: NanoHTTPD.IHTTPSession): ConcurrentHashMap<String, String> {
        val contentLength = try {
            Integer.parseInt(ses.headers["content-length"])
        } catch (e: Exception) {
            return ConcurrentHashMap()
        }

        val buffer = ByteArray(contentLength)
        ses.inputStream.read(buffer, 0, contentLength)

        val data = ConcurrentHashMap<String, String>()

        // It's in the format "x=x&x=x", so split into x=x, x=x
        val splits = String(buffer).split("&")

        for (split in splits) {
            if (!split.contains("="))
                continue

            val equalSplit = split.split("=")

            if (equalSplit.size == 2) {
                val key = equalSplit[0]
                val value = equalSplit[1]

                if (key != "" && value != "")
                    data[key] = value
            }
        }

        return data
    }
}