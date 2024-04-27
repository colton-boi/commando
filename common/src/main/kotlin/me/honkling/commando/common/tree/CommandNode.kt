package me.honkling.commando.common.tree

class CommandNode<T>(
    parent: Node?,
    name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val permissionMessage: String,
    val usageHandler: ((T) -> Unit)? = null
) : Node(parent, name) {
    override fun toString(): String {
        return "CommandNode(name=$name)"
    }
}