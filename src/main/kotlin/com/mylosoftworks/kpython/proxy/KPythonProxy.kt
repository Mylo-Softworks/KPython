package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.pythonobjects.PyType
import com.mylosoftworks.kpython.environment.pythonobjects.PyList

interface KPythonProxy {
    /**
     * Get the base object this proxy belongs to
     */
    @GetBaseProxy
    fun getKPythonProxyBase(): PythonProxyObject

    val __class__: PyType

    fun __dir__(): PyList

    @DontUsePython
    fun getAttribute(name: String): PythonProxyObject

    @DontUsePython
    fun setAttribute(name: String, value: Any?)

    @DontUsePython
    fun hasAttribute(name: String): Boolean

    companion object {
        fun getAttribute(self: PythonProxyObject, name: String): PythonProxyObject {
            return self[name]
        }

        fun setAttribute(self: PythonProxyObject, name: String, value: Any?) {
            self[name] = self.env.convertTo(value)
        }

        fun hasAttribute(self: PythonProxyObject, name: String): Boolean {
            return self.hasAttribute(name)
        }
    }
}