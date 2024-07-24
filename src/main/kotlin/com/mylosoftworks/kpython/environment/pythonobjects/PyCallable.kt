package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Indicates that an object is callable
 */
interface PyCallable : KPythonProxy {
    @DontUsePython
    operator fun invoke(vararg args: Any?): PythonProxyObject?

    companion object {
        fun invoke(self: PythonProxyObject, vararg args: Any?): PythonProxyObject? {
            self.let {
                val argsVal = if (args.isEmpty()) null else it.env.convertArgs(*args)?.obj
                return it.env.engine.PyObject_CallObject(it.obj, argsVal)
                    ?.let { it1 -> PythonProxyObject(it.env, it1) }
            }
        }
    }
}

