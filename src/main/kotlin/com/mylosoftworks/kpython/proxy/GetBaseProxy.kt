package com.mylosoftworks.kpython.proxy

/**
 * This annotation is used to indicate that the function should return the base object that is being proxied.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GetBaseProxy
