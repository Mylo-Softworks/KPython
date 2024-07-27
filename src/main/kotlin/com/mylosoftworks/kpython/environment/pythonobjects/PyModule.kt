package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyModule : KPythonProxy {
    @DontUsePython
    fun getDict(): PyDict

    @DontUsePython
    fun createFunction(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?)

    @DontUsePython
    fun createFunctionUnit(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit)

    @DontUsePython
    fun invokeMethod(key: String, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject

    companion object {
        fun getDict(self: PythonProxyObject): PythonProxyObject {
            return self.env.quickAccess.moduleGetDict(self)
        }

        fun createFunction(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
            self.env.quickAccess.moduleGetDict(self).asInterface<PyDict>()[name] = self.env.createFunction(self, name, docs, code)
        }

        fun createFunctionUnit(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
            self.env.quickAccess.moduleGetDict(self).asInterface<PyDict>()[name] = self.env.createFunctionUnit(self, name, docs, code)
        }

        fun invokeMethod(self: PythonProxyObject, key: String, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject {
            return getDict(self).invokeMethod(key, *args, kwargs = kwargs)
        }
    }
}