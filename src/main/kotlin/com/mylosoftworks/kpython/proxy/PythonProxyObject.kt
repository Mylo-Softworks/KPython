package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import java.lang.reflect.Proxy
import java.util.Dictionary

/**
 * Represents an unknown python object in kotlin
 */
open class PythonProxyObject internal constructor(val env: PyEnvironment, val obj: PyObject) {
    inline fun <reified T: KPythonProxy> asInterface(): T {
        return asInterface(T::class.java) as T
    }

    fun <T: KPythonProxy> asInterface(clazz: Class<T>): Any {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), PythonProxyHandler(this))
    }

    operator fun get(key: String): Any {
        TODO()
    }

    operator fun set(key: String, value: PythonProxyObject?) {
        TODO()
    }

    fun invokeMethod(key: String, vararg params: Any, dict: Dictionary<*, *>? = null): Any {
        TODO()
    }

    override fun equals(other: Any?): Boolean {
        if (other is PythonProxyObject) {
            return obj == other.obj
        }
        return obj == other
    }

    override fun hashCode(): Int {
        return obj.hashCode()
    }
}