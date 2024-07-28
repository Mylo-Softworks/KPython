package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyModule : KPythonProxy {
    @DontUsePython
    fun getDict(): PyDict

    @DontUsePython
    fun createFunction(name: String, docs: String = "", code: PyEnvironment.FunctionCallParams.() -> Any?)

    @DontUsePython
    fun createFunctionUnit(name: String, docs: String = "", code: PyEnvironment.FunctionCallParams.() -> Unit)

    @DontUsePython
    fun invokeFunction(key: String, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject

    @DontUsePython
    fun createClass(name: String, parentClass: PythonProxyObject? = null, init: PyEnvironment.FunctionCallParams.() -> Unit)

    @DontUsePython
    fun addSubModule(name: String, subModule: PyModule)

    companion object {
        fun getDict(self: PythonProxyObject): PyDict {
            return self.env.quickAccess.moduleGetDict(self)
        }

        fun createFunction(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment.FunctionCallParams.() -> Any?) {
            self.env.quickAccess.moduleGetDict(self)[name] = self.env.createFunction(self, name, docs, code)
        }

        fun createFunctionUnit(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment.FunctionCallParams.() -> Unit) {
            self.env.quickAccess.moduleGetDict(self)[name] = self.env.createFunctionUnit(self, name, docs, code)
        }

        fun invokeFunction(self: PythonProxyObject, key: String, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject {
            return self.env.quickAccess.moduleGetDict(self).invokeMethod(key, *args, kwargs = kwargs)
        }

        fun createClass(self: PythonProxyObject, name: String, parentClass: PythonProxyObject? = null, init: PyEnvironment.FunctionCallParams.() -> Unit) {
            val pyClass = self.env.createClass(name, parentClass, init)
            self.env.quickAccess.moduleGetDict(self)[name] = pyClass
        }

        fun addSubModule(self: PythonProxyObject, name: String, subModule: PyModule) {
            self.env.quickAccess.moduleGetDict(self)[name]
        }
    }
}