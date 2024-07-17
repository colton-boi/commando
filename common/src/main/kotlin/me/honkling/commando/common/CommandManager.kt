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

abstract class CommandManager<T>(val plugin: IPlugin<T>, val debugMode: Boolean = false) {
    val commands = mutableMapOf<String, CommandNode<*>>()
    val types = mutableMapOf<Class<*>, Type<*>>(
        Boolean::class.java to BooleanType,
        Double::class.java to DoubleType,
        Int::class.java to IntegerType,
        Float::class.java to FloatType,
        java.lang.Float::class.java to FloatType,
        java.lang.Boolean::class.java to BooleanType,
        java.lang.Double::class.java to DoubleType,
        java.lang.Integer::class.java to IntegerType,
        String::class.java to StringType
    )

    fun registerCommands(vararg packages: String) {
        debugLog("Registering commands with packages \"${packages.joinToString("\", \"")}\"")

        for (pkg in packages)
            for (command in scanForCommands(this, pkg)) {
                debugLog("Found command $command")
                commands[command.name.lowercase()] = command
            }

        for ((_, node) in commands) {
            debugLog("Registering command node $node")
            registerToPlatform(node)
        }
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
        return CommandNode(null, name, aliases.toList(), description, usage, permission, permissionMessage, usageHandler)
    }

    abstract fun isValidSender(clazz: Class<*>): Boolean
    abstract fun registerToPlatform(node: CommandNode<*>)

    fun getCommand(sender: ICommandSender<*>, command: CommandNode<*>, args: List<String>): Pair<SubcommandNode, List<Any?>>? {
        debugLog("Getting command with node $command from args $args")
        val postFirst = args.slice(1 until args.size)

        for (node in command.children) {
            debugLog("Node: $node")

            when (node) {
                is CommandNode<*> -> {
                    if (args.isEmpty())
                        continue

                    debugLog("Node is a command. Recursing!")
                    return getCommand(sender, node, postFirst) ?: continue
                }
                is SubcommandNode -> {
                    debugLog("Node is a subcommand.")

                    if (node.name != command.name && (args.isEmpty() || args.first().lowercase() != node.name.lowercase()))
                        continue

                    debugLog("First parameter is valid.")

                    val (isValid, parameters) = parseArguments(sender, node, if (node.name == command.name) args else postFirst)

                    debugLog("Is valid: $isValid, parameters: $parameters")

                    if (!isValid)
                        continue

                    debugLog("Command matching success!")

                    return node to parameters
                }
            }
        }

        debugLog("Command failed to match.")

        return null
    }

    private fun parseArguments(sender: ICommandSender<*>, command: SubcommandNode, args: List<String>): Pair<Boolean, List<Any?>> {
        @Suppress("NAME_SHADOWING") var args = args
        val parameters = mutableListOf<Any?>()

        for ((index, parameter) in command.parameters.withIndex()) {
            if (parameter.greedy && index + 1 < command.parameters.size)
                return false to parameters

            if (args.isEmpty()) {
                if (parameter.greedy) {
                    parameters.add(createGreedyArray(parameter.type, mutableListOf()))
                    return true to parameters
                }

                if (!parameter.required)
                    for (i in index..<command.parameters.size)
                        parameters.add(null)

                return !parameter.required to parameters
            }

            val type = types[parameter.type]

            if (type == null) {
                plugin.warn("Failed to find type for class '${parameter.type.name}'. Did you delete the type after registering commands?")
                return false to parameters
            }

            val spread = mutableListOf<Any>()
            var doneOnce = false

            while ((args.isNotEmpty() && parameter.greedy) || !doneOnce) {
                val input = args.joinToString(" ")

                if (input.isEmpty())
                    return false to parameters

                if (!type.validate(sender, input))
                    return false to parameters

                val (parsed, size) = type.parse(sender, input)
                args = args.slice(size until args.size)

                if (parameter.greedy) spread.add(parsed!!)
                else parameters.add(parsed!!)

                doneOnce = true
            }

            if (parameter.greedy)
                parameters.add(createGreedyArray(parameter.type, spread))
        }

        if (args.isNotEmpty())
            return false to parameters

        return true to parameters
    }

    private fun createGreedyArray(type: Class<*>, spread: MutableList<Any>): Any {
        val array = newInstance(type, spread.size)

        for ((i, parsed) in spread.withIndex())
            set(array, i, parsed)

        return array
    }

    fun debugLog(message: String) {
        if (debugMode)
            plugin.info(message)
    }
}