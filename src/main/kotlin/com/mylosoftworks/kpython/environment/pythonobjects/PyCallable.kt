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
            return self.invoke(*args)
        }
    }
}

