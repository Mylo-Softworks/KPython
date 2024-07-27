package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Indicates that an object is callable
 */
interface PyCallable : KPythonProxy {
    @DontUsePython
    operator fun invoke(vararg args: Any?, kwargs: HashMap<String, Any?>? = null): PythonProxyObject?

    companion object {
        fun invoke(self: PythonProxyObject, vararg args: Any?, kwargs: HashMap<String, Any?>? = null): PythonProxyObject? {
            return self.invoke(*args, kwargs = kwargs)
        }
    }
}

