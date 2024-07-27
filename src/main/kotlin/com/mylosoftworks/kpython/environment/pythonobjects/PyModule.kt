package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyModule : KPythonProxy {
    @DontUsePython
    fun getDict(): PyDict?

    companion object {
        fun getDict(self: PythonProxyObject): PythonProxyObject {
            return self.env.quickAccess.moduleGetDict(self)
        }
    }
}