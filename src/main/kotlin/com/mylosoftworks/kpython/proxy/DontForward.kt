package com.mylosoftworks.kpython.proxy

/**
 * Indicates that this function should not be forwarded to python, instead run the kotlin code
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DontForward
