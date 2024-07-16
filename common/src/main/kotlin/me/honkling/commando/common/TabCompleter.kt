package me.honkling.commando.common

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.tree.CommandNode
import me.honkling.commando.common.tree.Node
import me.honkling.commando.common.tree.SubcommandNode

fun tabComplete(manager: CommandManager<*>, sender: ICommandSender<*>, node: CommandNode<*>, args: Array<String>): List<String> {
    val last = args.last()

    // /example
    // /example one (int)
    // /example two

    // /example (one, two) (command, args(0) -> command)
    // /example o (command, args(1) -> command)
    // /example one 1 (subcommand, args(1) -> subcommand)
    // /example two (subcommand, args(0) -> subcommand)


    // /invsee <player>
    // /invsee lilrosa

    manager.debugLog("Tab completing argument '$last'")

    val (completionNode, count) = getNode(node, args.toList())

    manager.debugLog("Tab completion node: $completionNode (Count: $count)")

    if (completionNode is CommandNode<*>) {
        // todo: just add to completions
        val subcommands = completionNode.children.map { it.name }.filter { it != completionNode.name }
        manager.debugLog("Node is a command node. Returning subcommands: $subcommands")
        return subcommands
    }

    val mutableArgs = args.toMutableList()

    // Prune arguments for reaching subcommand node
    for (i in 0..<count) {
        manager.debugLog("Pruning argument '${mutableArgs.first()}' ($i)")
        mutableArgs.removeFirst()
    }

    manager.debugLog("Arguments list post prune: $mutableArgs")

    // Prune passed parameters or complete current one
    val parameters = (completionNode as SubcommandNode).parameters
    for (parameter in parameters) {
        val type = manager.types[parameter.type] ?: return emptyList()
        val input = mutableArgs.joinToString("")

        manager.debugLog("Trying parameter $parameter")
        manager.debugLog("Input: '$input'")

        // If we have input & the type validates the input, then parse & prune arguments
        if (input.isNotEmpty() && type.validate(sender, input)) {
            val (_, parseCount) = type.parse(sender, input)

            for (i in 0..<parseCount) {
                manager.debugLog("Pruning parameter '${mutableArgs.first()}' ($i)")
                mutableArgs.removeFirst()
            }

            manager.debugLog("Arguments: $mutableArgs")

            // If there are no more args left, then return an empty list.
            if (mutableArgs.isEmpty() || mutableArgs.first().isEmpty()) {
                manager.debugLog("Parsed all args that we could, waiting on user for input.")
                return emptyList()
            }

            continue
        }

        if (input.isEmpty()) {
            manager.debugLog("Waiting on user for input.")
            return emptyList()
        }

        // This is the argument we need to complete.
        val parent = completionNode.parent
        val completions = type.complete(sender, input).toTypedArray()
        val subcommands =
            if (parent?.name == completionNode.name)
                parent.children.map { it.name }.filter { input.lowercase() in it.lowercase() }.toTypedArray()
            else emptyArray()

        manager.debugLog("Adding completions: ${completions.toList()}")

        if (parent?.name == completionNode.name)
            manager.debugLog("Adding parent subcommands: ${subcommands.toList()}")

        return listOf(*completions, *subcommands)
    }

    manager.debugLog("We're done. No more arguments.")
    return emptyList()
}

private fun getNode(node: CommandNode<*>, args: List<String>, count: Int = 0): Pair<Node, Int> {
    if (args.isEmpty())
        return node to count

    val childNode = node.children.find { it.name == args[0] || it.name == node.name } ?: return node to count

    if (childNode is CommandNode<*>)
        return getNode(childNode, args.slice(1..<args.size), count + 1)

    return childNode to count + if (childNode.name == node.name) 0 else 1
}