package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.environment.pythonobjects.PyClass
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

enum class GCBehavior {
    FULL, // Copied in Kotlin, created objects shouldn't have FULL as the refcount is 1 when an object is created, can be used for "borrowed reference"
    ONLY_DEC, // Created in CPython, used for "new reference"
    IGNORE // Borrowed (And not kept in kotlin), or constant value, can be used for "borrowed reference"
}

/**
 * Represents an unknown python object in kotlin
 */
open class PythonProxyObject internal constructor(val env: PyEnvironment, val obj: PyObject, val gcBehavior: GCBehavior) {
    init {
        if (gcBehavior == GCBehavior.FULL) incRef()
    }

    fun finalize() {
        if (gcBehavior == GCBehavior.IGNORE) return

        decRef()
    }

    private fun incRef() = env.engine.Py_IncRef(obj)
    private fun decRef() = env.engine.Py_DecRef(obj)

    /**
     * Get a new reference to the same object.
     */
    fun getReference(): PythonProxyObject {
        return env.createProxyObject(obj, GCBehavior.FULL)
    }

    inline fun <reified T: KPythonProxy> asInterface(): T {
        return asInterface(T::class.java) as T
    }

    inline infix fun <reified T: KPythonProxy, U: KClass<T>> convTo(target: U): T {
        return asInterface(target.java) as T
    }

    fun <T: KPythonProxy> asInterface(clazz: Class<T>): Any {
        return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), PythonProxyHandler(this))
    }

    operator fun get(key: String): PythonProxyObject {
        return env.engine.PyObject_GetAttrString(obj, key)?.let {env.createProxyObject(it) } ?: env.quickAccess.throwAutoError()
    }

    operator fun set(key: String, value: PythonProxyObject) {
        env.engine.PyObject_SetAttrString(obj, key, value.obj)
    }

    fun hasAttribute(key: String): Boolean {
        return env.engine.PyObject_HasAttrString(obj, key)
    }

//    fun invokeMethod(key: String, vararg params: Any): PythonProxyObject? {
//        val method = this[key]!!.obj
//        return env.engine.PyObject_CallObject(method, env.convertArgs(*params)?.obj)?.let { env.createProxyObject(it) }
//    }

    fun invokeMethod(key: String, vararg params: Any?, kwargs: HashMap<String, Any?>? = null): PythonProxyObject {
        val method = this[key]
        return env.quickAccess.invoke(method, *params, kwargs = kwargs)
    }

    fun invoke(vararg params: Any?, kwargs: HashMap<String, Any?>? = null): PythonProxyObject {
        return env.quickAccess.invoke(this, *params, kwargs = kwargs)
    }

    fun createMethod(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
        val method = env.createFunction(this, name, docs, code)
        set(name, method.getKPythonProxyBase())
    }

    fun createMethodUnit(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
        val method = env.createFunctionUnit(this, name, docs, code)
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