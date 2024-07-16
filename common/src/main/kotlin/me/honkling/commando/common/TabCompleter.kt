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

    manager.debugLog("Tab completing argument '$last'")

    val (completionNode, count) = getNode(node, args.toList())

    manager.debugLog("Tab completion node: $completionNode (Count: $count)")

    if (completionNode is CommandNode<*>) {
        val subcommands = completionNode.children.map { it.name }.filter { it != completionNode.name }
        manager.debugLog("Node is a command node. Returning subcommands: $subcommands")
        return subcommands
    }

    val arguments = args.toMutableList()
    manager.debugLog("Node is a subcommand node. Completing with args: $args")

    for (i in 0..<count) {
        manager.debugLog("Pruned argument ${arguments.removeFirst()} ($i)")
        arguments.removeFirst()
    }

    if (arguments.isEmpty()) {
        manager.debugLog("No more arguments left. Returning empty list.")
        return emptyList()
    }

    for (parameter in (completionNode as SubcommandNode).parameters) {
        manager.debugLog("Completing parameter $parameter")
        val type = manager.types[parameter.type] ?: return emptyList()
        val input = arguments.joinToString(" ")
        manager.debugLog("Got a type $type, input is $input")

        if (arguments.isEmpty() || !type.validate(sender, input)) {
            val completions = type.complete(sender, input)
            val parent = completionNode.parent

            manager.debugLog("Last argument or type is invalid. Completing $completions & subcommands if possible.")

            return listOf(
                *completions.toTypedArray(),
                *(if (completionNode.name == parent?.name)
                      parent.children.map { it.name }.filter { it != parent.name }.toTypedArray()
                  else emptyArray())
            )
        }

        val (_, count) = type.parse(sender, input)

        for (i in 0..<count) {
            manager.debugLog("Pruning more arguments: ${arguments.first()} ($i)")
            arguments.removeFirst()
        }
    }

    manager.debugLog("Returning empty completions.")

    return emptyList()
}

private fun getNode(node: CommandNode<*>, args: List<String>, count: Int = 0): Pair<Node, Int> {
    if (args.isEmpty())
        return node to count

    val childNode = node.children.find { it.name == args[0] || it.name == node.name } ?: return node to count

    if (childNode is CommandNode<*>)
        return getNode(childNode, args.slice(1..<args.size), count + 1)

    return childNode to count + 1
}