package me.honkling.commando.minestom.impl

import me.honkling.commando.common.generic.ICommandSender
import net.minestom.server.command.CommandSender

class SenderProvider(private val sender: CommandSender) : ICommandSender<CommandSender> {
    override fun get(): CommandSender {
        return sender
    }
}