package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import java.lang.reflect.Proxy

internal enum class GCBehavior {
    FULL, ONLY_DEC, IGNORE
}

/**
 * Represents an unknown python object in kotlin
 */
open class PythonProxyObject internal constructor(val env: PyEnvironment, val obj: PyObject, internal val gcBehavior: GCBehavior) {
    init {
        if (gcBehavior == GCBehavior.FULL) env.engine.Py_IncRef(obj)
    }

    fun finalize() {
        if (gcBehavior == GCBehavior.IGNORE) return

        env.engine.Py_DecRef(obj)
    }

    inline fun <reified T: KPythonProxy> asInterface(): T {
        return asInterface(T::class.java) as T
    }

    fun <T: KPythonProxy> asInterface(clazz: Class<T>): Any {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), PythonProxyHandler(this))
    }

    operator fun get(key: String): PythonProxyObject? {
        return env.engine.PyObject_GetAttrString(obj, key)?.let {env.createProxyObject(it) }
    }

    operator fun set(key: String, value: PythonProxyObject) {
        env.engine.PyObject_SetAttrString(obj, key, value.obj)
    }

    fun invokeMethod(key: String, vararg params: Any): PythonProxyObject? {
        val method = this[key]!!.obj
        return env.engine.PyObject_CallObject(method, env.convertArgs(*params)?.obj)?.let { env.createProxyObject(it) }
    }

    fun createMethod(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
        val method = env.createFunction(this, name, docs, code)!!
        set(name, method.getKPythonProxyBase())
    }

    fun createMethodUnit(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
        val method = env.createFunctionUnit(this, name, docs, code)!!
        set(name, method.getKPythonProxyBase())
    }

    inline fun <reified T> toJvmRepresentation(): T? {
        return toJvmRepresentation(T::class.java) as T?
    }

    fun toJvmRepresentation(type: Class<*>): Any? {
        return this.env.convertFrom(this, type)
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

    override fun toString(): String {
        return env.engine.let { it.PyObject_Str(obj)?.let { it1 -> it.PyUnicode_AsUTF8(it1) }.toString() }
    }
}