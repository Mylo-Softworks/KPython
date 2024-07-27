package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyEnterable : KPythonProxy {
    @DontUsePython
    fun with(vararg args: Any?, kwargs: HashMap<String, Any?>? = null, block: InsideEnterable.() -> Unit)

    @DontUsePython
    fun canEnter(): Boolean

    companion object {
        fun with(self: PythonProxyObject, vararg args: Any?, kwargs: HashMap<String, Any?>? = null, block: InsideEnterable.() -> Unit) {
            val given = self.invokeMethod("__enter__", *args, kwargs = kwargs)
            block(InsideEnterable(self, given))
            if (!self.hasAttribute("__exit__")) return
        }

        fun canEnter(self: PythonProxyObject): Boolean {
            return self.hasAttribute("__enter__")
        }
    }
}

data class InsideEnterable(val self: PythonProxyObject, val given: PythonProxyObject?)