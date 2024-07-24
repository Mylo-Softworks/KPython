package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import java.util.concurrent.atomic.AtomicInteger

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

    @DontUsePython
    fun size(): Long

    @DontUsePython
    operator fun get(key: Int): PythonProxyObject?

    @DontUsePython
    operator fun set(key: Int, value: PythonProxyObject)

    @DontUsePython
    operator fun iterator(): Iterator<PythonProxyObject>

    companion object {
        fun size(self: PythonProxyObject): Long {
            return self.let {
                it.env.engine.PyList_Size(it.obj)
            }
        }

        fun get(self: PythonProxyObject, key: Int): PythonProxyObject? {
            return self.let {
                it.env.engine.PyList_GetItem(self.obj, key.toLong())?.let { it2 ->
                    it.env.createProxyObject(it2, GCBehavior.IGNORE) // Borrowed
                }
            }
        }

        fun set(self: PythonProxyObject, key: Int, value: PythonProxyObject) {
            self.env.engine.PyList_SetItem(self.obj, key.toLong(), value.obj)
        }

        fun iterator(self: PythonProxyObject): Iterator<PythonProxyObject?> {
            return PythonListIterator(self.asInterface<PyList>())
        }
    }
}

class PythonListIterator(val list: PyList) : Iterator<PythonProxyObject?> {
    val idx = AtomicInteger(0)
    val size = list.size()

    override fun hasNext(): Boolean {
        return idx.get() < size - 1
    }

    override fun next(): PythonProxyObject? {
        return list[idx.incrementAndGet()]
    }

}