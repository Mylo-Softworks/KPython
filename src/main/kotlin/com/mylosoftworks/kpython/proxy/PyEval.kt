package com.mylosoftworks.kpython.proxy

/**
 * Run python code, named fun
 * @param code The code for the function to execute
 * @param firstArgIsSelf True if the function takes a "self" argument, false if it doesn't
 * @sample
 * def fun(param1):
 *   print(param1)
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PyFun(val code: String, val firstArgIsSelf: Boolean = false)