package me.honkling.commando.common.generic

interface IPlugin<T> {
    fun get(): T

    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}