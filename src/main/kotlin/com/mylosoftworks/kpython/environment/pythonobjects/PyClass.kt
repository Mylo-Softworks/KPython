package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.KPythonProxy

/**
 * Represents a python class of the provided type
 */
interface PyClass : PyCallable {
    val __name__: String
}

inline fun <reified T: KPythonProxy> PyClass.createTyped(vararg args: Any?): T? {
    return this(*args)?.asInterface<T>()
}