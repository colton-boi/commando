package me.honkling.commando.minestom.impl

import me.honkling.commando.common.generic.IConsoleSender
import net.minestom.server.MinecraftServer
import net.minestom.server.command.ConsoleSender

class ConsoleProvider : IConsoleSender<ConsoleSender> {
    override fun get(): ConsoleSender {
        return MinecraftServer.getCommandManager().consoleSender
    }
}