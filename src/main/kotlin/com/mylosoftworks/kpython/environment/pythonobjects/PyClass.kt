package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.environment.pythonobjects.PyDict.Companion.set
import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Represents a python class of the provided type
 */
interface PyClass : PyCallable {
    var __name__: String

    @DontUsePython
    fun getDict(): PyDict?

    @DontUsePython
    fun createMethod(name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?)

    @DontUsePython
    fun createMethodUnit(name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit)

    companion object {
        fun getDict(self: PythonProxyObject): PythonProxyObject? {
            return self.env.quickAccess.typeGetDict(self)
        }

        fun createMethod(self: PythonProxyObject, name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
            val method = self.env.createFunction(self, name.toString(), docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }

        fun createMethodUnit(self: PythonProxyObject, name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
            val method = self.env.createFunctionUnit(self, name.toString(), docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }
    }
}

inline fun <reified T: KPythonProxy> PyClass.createTyped(vararg args: Any?): T? {
    return this(*args)?.asInterface<T>()
}