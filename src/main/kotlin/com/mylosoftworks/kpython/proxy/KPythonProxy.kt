package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.pythonobjects.PyList

interface KPythonProxy {
    /**
     * Get the base object this proxy belongs to
     */
    @GetBaseProxy
    fun getKPythonProxyBase(): PythonProxyObject

    val __class__: PythonProxyObject

    fun __dir__(): PyList
}