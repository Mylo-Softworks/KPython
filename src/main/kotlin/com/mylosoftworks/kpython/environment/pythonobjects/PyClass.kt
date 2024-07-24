package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Represents a python class of the provided type
 */
interface PyClass : PyCallable {
    val __name__: String
}