package me.honkling.commando.minestom

import me.honkling.commando.common.ListenerManager
import me.honkling.commando.minestom.impl.Plugin
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import java.lang.reflect.Modifier

class MinestomListenerManager(debugMode: Boolean = false) : ListenerManager<MinestomPlugin>(Plugin(MinestomPlugin()), debugMode) {
    override fun registerClass(clazz: Class<*>) {
        debugLog("Registering class ${clazz.name}")
        for (method in clazz.declaredMethods) {
            debugLog("Found method ${method.name}")
            val event = method.parameters.firstOrNull() ?: continue

            if (!Modifier.isStatic(method.modifiers) || !isEvent(event.type))
                continue

            debugLog("Method is static, and uses event ${event.type.name}. Registering event!")

            val eventHandler = MinecraftServer.getGlobalEventHandler()
            eventHandler.addListener(event.type as Class<out Event>) {
                debugLog("Calling ${method.name} in ${method.declaringClass.name} with args ${listOf(event)}")
                debugLog("Method parameters: ${method.parameterTypes.toList().map { it.name }}")
                method.isAccessible = true
                method.invoke(null, it)
            }
        }
    }

    override fun isEvent(clazz: Class<*>): Boolean {
        return Event::class.java.isAssignableFrom(clazz)
    }
}