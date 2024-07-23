package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyDict : KPythonProxy {
    fun clear()
    fun copy(): PyDict
    fun get(key: Any?): PythonProxyObject
}