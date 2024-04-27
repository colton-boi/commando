package me.honkling.commando.common

import me.honkling.commando.common.annotations.DESCRIPTION
import me.honkling.commando.common.annotations.PERMISSION
import me.honkling.commando.common.annotations.PERMISSION_MESSAGE
import me.honkling.commando.common.annotations.USAGE
import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.generic.IPlugin
import me.honkling.commando.common.tree.CommandNode
import me.honkling.commando.common.tree.SubcommandNode
import me.honkling.commando.common.types.*
import java.lang.reflect.Array.*

abstract class CommandManager<T>(val plugin: IPlugin<T>) {
    val commands = mutableMapOf<String, CommandNode<*>>()
    val types = mutableMapOf<Class<*>, Type<*>>(
        Boolean::class.java to BooleanType,
        Double::class.java to DoubleType,
        Int::class.java to IntegerType,
        java.lang.Boolean::class.java to BooleanType,
        java.lang.Double::class.java to DoubleType,
        java.lang.Integer::class.java to IntegerType,
        String::class.java to StringType
    )

    fun registerCommands(vararg packages: String) {
        for (pkg in packages)
            for (command in scanForCommands(this, pkg))
                commands[command.name.lowercase()] = command

        for ((_, node) in commands)
            registerToPlatform(node)
    }

    fun <T> registerCommand(
        name: String,
        vararg aliases: String,
        description: String = DESCRIPTION,
        usage: String = USAGE,
        usageHandler: (T) -> Unit = {},
        permission: String = PERMISSION,
        permissionMessage: String = PERMISSION_MESSAGE
    ): CommandNode<T> {
        return CommandNode<T>(null, name, aliases.toList(), description, usage, permission, permissionMessage, usageHandler)
    }

    abstract fun isValidSender(clazz: Class<*>): Boolean
    abstract fun registerToPlatform(node: CommandNode<*>)

    fun getCommand(sender: ICommandSender<*>, command: CommandNode<*>, args: List<String>): Pair<SubcommandNode, List<Any>>? {
        val postFirst = args.slice(1 until args.size)

        for (node in command.children) {
            when (node) {
                is CommandNode<*> -> {
                    if (args.isEmpty())
                        continue

                    return getCommand(sender, node, postFirst) ?: continue
                }
                is SubcommandNode -> {
                    if (node.name != command.name && (args.isEmpty() || args.first().lowercase() != node.name))
                        continue

                    val (isValid, parameters) = parseArguments(sender, node, if (node.name == command.name) args else postFirst)

                    if (!isValid)
                        continue

                    return node to parameters
                }
            }
        }

        return null
    }

    private fun parseArguments(sender: ICommandSender<*>, command: SubcommandNode, args: List<String>): Pair<Boolean, List<Any>> {
        @Suppress("NAME_SHADOWING") var args = args
        val parameters = mutableListOf<Any>()

        for ((index, parameter) in command.parameters.withIndex()) {
            if (parameter.greedy && index + 1 < command.parameters.size) {
                plugin.warn("Vararg parameter '${parameter.name}' must be the last parameter")
                return false to parameters
            }

            if (args.isEmpty())
                return !parameter.required to parameters

            val type = types[parameter.type]

            if (type == null) {
                plugin.warn("Failed to find type for class '${parameter.type.name}'. Did you delete the type after registering commands?")
                return false to parameters
            }

            val spread = mutableListOf<Any>()
            var doneOnce = false

            while ((args.isNotEmpty() && parameter.greedy) || !doneOnce) {
                val input = args.joinToString(" ")

                if (!type.validate(sender, input))
                    return false to parameters

                val (parsed, size) = type.parse(sender, input)
                args = args.slice(size until args.size)

                if (parameter.greedy) spread.add(parsed!!)
                else parameters.add(parsed!!)

                doneOnce = true
            }

            if (parameter.greedy) {
                val array = newInstance(parameter.type, spread.size)

                for ((i, parsed) in spread.withIndex())
                    set(array, i, parsed)

                parameters.add(array)
            }
        }

        return true to parameters
    }
}