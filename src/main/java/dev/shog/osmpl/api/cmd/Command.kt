package dev.shog.osmpl.api.cmd

abstract class Command(val name: String) {
    abstract fun execute(commandContext: CommandContext): Boolean

    companion object {
        /**
         * Make a [Command].
         */
        fun make(name: String, command: CommandContext.() -> Boolean): Command =
                object: Command(name) {
                    override fun execute(commandContext: CommandContext): Boolean {
                        return command.invoke(commandContext)
                    }
                }
    }
}