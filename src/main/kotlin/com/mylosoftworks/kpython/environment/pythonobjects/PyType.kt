package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Represents a python class type of the provided type
 */
interface PyType : PyCallable {
    var __name__: String

    @DontUsePython
    fun getDict(): PyDict

    companion object {
        fun getDict(self: PythonProxyObject): PythonProxyObject {
            return self.env.quickAccess.typeGetDict(self)
        }
    }
}

inline fun <reified T: KPythonProxy> PyType.createTyped(vararg args: Any?): T? {
    return this(*args)?.asInterface<T>()
}