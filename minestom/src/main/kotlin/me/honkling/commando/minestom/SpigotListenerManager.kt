package me.honkling.commando.minestom

import me.honkling.commando.common.ListenerManager
import me.honkling.commando.minestom.impl.Plugin
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import java.lang.reflect.Modifier

class MinestomListenerManager : ListenerManager<MinestomPlugin>(Plugin(MinestomPlugin())) {
    override fun registerClass(clazz: Class<*>) {
        for (method in clazz.declaredMethods) {
            val event = method.parameters.firstOrNull() ?: continue

            if (!Modifier.isStatic(method.modifiers) || !isEvent(event.type))
                continue

            val eventHandler = MinecraftServer.getGlobalEventHandler()
            eventHandler.addListener(event.type as Class<out Event>) {
                method.isAccessible = true
                method.invoke(null, it)
            }
        }
    }

    override fun isEvent(clazz: Class<*>): Boolean {
        return Event::class.java.isAssignableFrom(clazz)
    }
}