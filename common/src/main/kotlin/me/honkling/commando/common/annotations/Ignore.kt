package me.honkling.commando.common.annotations

/**
 * Tells commando to ignore the method.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Ignore()
