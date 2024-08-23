package me.honkling.commando.minestom.types

import me.honkling.commando.common.generic.ICommandSender
import me.honkling.commando.common.types.Type
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

object PlayerType : Type<Player>() {
    override fun validate(sender: ICommandSender<*>, input: String): Boolean {
        val first = input.split(" ")[0]
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(first) != null
    }

    override fun parse(sender: ICommandSender<*>, input: String): Pair<Player, Int> {
        val first = input.split(" ")[0]
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(first)!! to 1
    }

    override fun complete(sender: ICommandSender<*>, input: String): List<String> {
        return MinecraftServer.getConnectionManager()
                .getOnlinePlayers()
                .map { it.username }
                .filter { input.split(" ")[0].lowercase() in it.lowercase() }
    }
}