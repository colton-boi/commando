package me.honkling.commando.minestom.impl

import me.honkling.commando.common.generic.IPlugin
import me.honkling.commando.minestom.MinestomPlugin

class Plugin(private val plugin: MinestomPlugin) : IPlugin<MinestomPlugin> {

    override fun get(): MinestomPlugin = plugin

    override fun warn(message: String) {
        plugin.logger.warning(message)
    }

    override fun error(message: String) {
        plugin.logger.severe(message)
    }
}