package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontForward
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyList : KPythonProxy {
    fun append(item: Any?)
    fun clear()
    fun copy(): PyList
    fun count(value: Any?): Int
    fun extend(other: PyList)
    fun insert(index: Int, value: Any?)
    fun pop(index: Int): Any?
    fun remove(value: Any?)
    fun reverse()
    fun sort(reverse: Boolean = false, key: ((Any?) -> Int)? = null)
    fun index(key: Int): PythonProxyObject
    @DontForward
    operator fun get(key: Int): PythonProxyObject = index(key)
}