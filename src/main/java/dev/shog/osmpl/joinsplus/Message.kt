package dev.shog.osmpl.joinsplus

data class Message(
    var join: String = JoinsPlus.DEFAULT_JOIN,
    var quit: String = JoinsPlus.DEFAULT_QUIT
)