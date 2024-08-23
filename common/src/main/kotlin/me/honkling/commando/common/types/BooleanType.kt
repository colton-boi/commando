package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender

object BooleanType : Type<Boolean>() {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        val regex = Regex("^(true|false)(?!\\S)", RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(input)
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<Boolean, Int> {
        return getFirst(input).toBoolean() to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return listOf("true", "false").filter { it.contains(getFirst(input)) }
    }
}