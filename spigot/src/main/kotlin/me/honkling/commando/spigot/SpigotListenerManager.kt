package me.honkling.commando.spigot

import me.honkling.commando.common.ListenerManager
import me.honkling.commando.spigot.impl.Plugin
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Modifier

class SpigotListenerManager(plugin: JavaPlugin, debugMode: Boolean = false) : ListenerManager<JavaPlugin>(Plugin(plugin), debugMode) {
    override fun registerClass(clazz: Class<*>) {
        val listenerInstance = object : Listener {} as Listener
        debugLog("Registering class ${clazz.name}")

        for (method in clazz.declaredMethods) {
            debugLog("Found method ${method.name}")
            val event = method.parameters.firstOrNull() ?: continue

            if (!Modifier.isStatic(method.modifiers) || !isEvent(event.type) || method.parameterCount != 1 || method.name.contains("\$lambda"))
                continue

            debugLog("Method is static, and uses event ${event.type.name}. Registering event!")

            @Suppress("UNCHECKED_CAST")
            Bukkit.getPluginManager().registerEvent(
                event.type as Class<out Event>,
                listenerInstance,
                EventPriority.NORMAL,
                { _, evt ->
                    debugLog("Calling ${method.name} in ${method.declaringClass.name} with args ${listOf(evt)}")
                    debugLog("Method parameters: ${method.parameterTypes.toList().map { it.name }}")
                    method.isAccessible = true
                    method.invoke(null, evt)
                },
                plugin.get()
            )
        }
    }

    override fun isEvent(clazz: Class<*>): Boolean {
        return Event::class.java.isAssignableFrom(clazz)
    }
}