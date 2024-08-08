package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender

object FloatType : Type<Float> {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        val regex = Regex("^-?\\d+(\\.\\d+(?!\\S))?")
        return regex.containsMatchIn(input)
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<Float, Int> {
        return input.split(" ")[0].toFloat() to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return emptyList()
    }
}