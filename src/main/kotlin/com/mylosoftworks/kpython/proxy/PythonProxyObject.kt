package com.mylosoftworks.kpython.proxy

import java.lang.reflect.Proxy

open class PythonProxyObject {
    inline fun <reified T> asInterface(): T {
        return asInterface(T::class.java) as T
    }

    fun asInterface(clazz: Class<*>): Any {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), PythonProxyHandler(this))
    }

    operator fun get(key: String): Any {
        TODO()
    }

    operator fun set(key: String, value: Any) {
        TODO()
    }

    fun invokeMethod(key: String, vararg params: Any): Any {
        TODO()
    }
}