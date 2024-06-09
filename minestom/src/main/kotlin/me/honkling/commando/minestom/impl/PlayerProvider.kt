package me.honkling.commando.minestom.impl

import me.honkling.commando.common.generic.IPlayer
import net.minestom.server.entity.Player

class PlayerProvider(private val player: Player) : IPlayer<Player> {
    override fun get(): Player {
        return player
    }
}