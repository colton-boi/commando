package me.honkling.commando.common.annotations

const val DESCRIPTION = "hello"
const val USAGE = "<gray>Invalid usage."
const val PERMISSION = ""
const val PERMISSION_MESSAGE = "<gray>You don't have access to <white>/{0}</white>."

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
        val name: String,
        vararg val aliases: String,
		val description: String = DESCRIPTION,
		val usage: String = USAGE,
		val permission: String = PERMISSION,
		val permissionMessage: String = PERMISSION_MESSAGE
)
