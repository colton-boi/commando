package me.honkling.commando.minestom

import me.honkling.commando.common.CommandManager
import me.honkling.commando.common.tabComplete
import me.honkling.commando.common.tree.CommandNode
import me.honkling.commando.minestom.impl.Plugin
import me.honkling.commando.minestom.impl.SenderProvider
import me.honkling.commando.minestom.types.PlayerType
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission

class MinestomCommandManager(plugin: MinestomPlugin) : CommandManager<MinestomPlugin>(Plugin(plugin)) {
    init {
        types[Player::class.java] = PlayerType
    }

    override fun isValidSender(clazz: Class<*>): Boolean {
        return clazz in listOf(
            Player::class.java,
            ConsoleSender::class.java,
            CommandSender::class.java
        )
    }

    override fun registerToPlatform(node: CommandNode<*>) {
        for ((_, node) in commands) {
            val command = createPluginCommand(node)
            MinecraftServer.getCommandManager().register(command)
        }
    }

    private fun onCommand(sender: CommandSender, simpleCommand: Command, args: Array<String>): Boolean {
        val provider = SenderProvider(sender)
        val command = commands[simpleCommand.name.lowercase()] ?: return false
        val result = getCommand(provider, command, args.toList())

        if (result == null) {
            if (command.usageHandler != null) {
               (command.usageHandler as (CommandSender) -> Unit).invoke(provider.get())
                return true
            }

            return false
        }

        val (subcommand, parameters) = result
        subcommand.method.invoke(null, sender, *parameters.toTypedArray())

        return true
    }

    private fun createPluginCommand(node: CommandNode<*>): Command {
        return object : Command(node.name, *node.aliases.toTypedArray()) {
            init {
                condition = CommandCondition { sender, command -> hasAccess(sender) }

                defaultExecutor = CommandExecutor { sender, context ->
                    process(sender, emptyArray())
                }

                val params = ArgumentType.StringArray("params")
                addSyntax({ sender, context ->
                    process(sender, context.get(params))
                }, params)

                params.setSuggestionCallback { sender, context, suggestion ->
                    tabComplete(this@MinestomCommandManager, SenderProvider(sender), node, context.get(params))
                }
            }

            fun process(sender: CommandSender, args: Array<String>): Boolean {
                return onCommand(sender, this, args)
            }

            fun hasAccess(sender: CommandSender): Boolean {
                return sender.hasPermission(Permission(node.permission))
            }
        }
    }
}