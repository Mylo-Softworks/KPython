package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyDict : KPythonProxy {
    fun clear()
    fun copy(): PyDict
    fun items(): PythonProxyObject

    @DontUsePython
    fun getSize(): Long

    @DontUsePython
    operator fun get(key: Any): PythonProxyObject?

    @DontUsePython
    operator fun set(key: Any, value: Any)

    @DontUsePython
    fun createMethod(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?)

    @DontUsePython
    fun createMethodUnit(name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit)

    companion object {
        fun getSize(self: PythonProxyObject): Long {
            return self.let {
                it.env.engine.PyDict_Size(it.obj)
            }
        }

        fun get(self: PythonProxyObject, key: Any): PythonProxyObject? {
            return self.let {
                it.env.engine.PyDict_GetItem(it.obj, it.env.convertTo(key)!!.obj)?.let { it1 ->
                    it.env.createProxyObject(
                        it1, GCBehavior.IGNORE // Borrowed
                    )
                }
            }
        }

        fun set(self: PythonProxyObject, key: Any, value: Any) {
            self.let {
                it.env.engine.PyDict_SetItem(it.obj, it.env.convertTo(key)!!.obj, it.env.convertTo(value)!!.obj)
            }
        }

        fun createMethod(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
            val method = self.env.createFunction(self, name, docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }

        fun createMethodUnit(self: PythonProxyObject, name: String, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
            val method = self.env.createFunctionUnit(self, name, docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }
    }
}