package me.honkling.commando.common.annotations

/**
 * Tells commando that the argument is optional.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Optional
