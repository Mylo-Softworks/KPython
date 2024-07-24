package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PyFun
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyDict<K> : KPythonProxy {
    fun clear()
    fun copy(): PyDict<K>

    @DontUsePython
    operator fun get(key: K): PythonProxyObject?

    @DontUsePython
    operator fun set(self: PythonProxyObject, key: K, value: Any)

    companion object {
        fun get(self: PythonProxyObject, key: Any): PythonProxyObject? {
            return self.let {
                it.env.engine.PyDict_GetItem(it.obj, it.env.convertTo(key)!!.obj)?.let { it1 ->
                    it.env.createProxyObject(
                        it1
                    )
                }
            }
        }

        fun set(self: PythonProxyObject, key: Any, value: Any) {
            self.let {
                it.env.engine.PyDict_SetItem(it.obj, it.env.convertTo(key)!!.obj, it.env.convertTo(value)!!.obj)
            }
        }
    }
}