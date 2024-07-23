package com.mylosoftworks.kpython.proxy

interface KPythonProxy {
    /**
     * Get the base object this proxy belongs to
     */
    @GetBaseProxy
    fun getKPythonProxyBase(): PythonProxyObject
}