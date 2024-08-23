package me.honkling.commando.common.types

import me.honkling.commando.common.generic.ICommandSender
import kotlin.math.min

abstract class Type<T> {
    abstract fun validate(sender: ICommandSender<*>, input: String): Boolean
    abstract fun parse(sender: ICommandSender<*>, input: String): Pair<T, Int>
    abstract fun complete(sender: ICommandSender<*>, input: String): List<String>

    fun getFirst(input: String, wordCount: Int = 1): String {
        val words = input.split(" ")

        return words
            .subList(0, min(words.size, wordCount))
            .joinToString(" ")
    }
}