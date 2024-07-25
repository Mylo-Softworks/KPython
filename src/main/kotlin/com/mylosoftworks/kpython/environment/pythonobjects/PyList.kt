package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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
    operator fun get(key: Long): PythonProxyObject?

    @DontUsePython
    operator fun set(key: Long, value: Any?)

    @DontUsePython
    operator fun iterator(): Iterator<PythonProxyObject>

    companion object {
        fun size(self: PythonProxyObject): Long {
            return self.env.quickAccess.listGetSize(self)
//            return self.let {
//                it.env.engine.PyList_Size(it.obj)
//            }
        }

        fun get(self: PythonProxyObject, key: Long): PythonProxyObject? {
            return self.env.quickAccess.listGetItem(self, key)
//            return self.let {
//                it.env.engine.PyList_GetItem(self.obj, key.toLong())?.let { it2 ->
//                    it.env.createProxyObject(it2, GCBehavior.IGNORE) // Borrowed
//                }
//            }
        }

        fun set(self: PythonProxyObject, key: Long, value: Any?) {
            self.env.quickAccess.listSetItem(self, key, value)
//            self.env.engine.PyList_SetItem(self.obj, key.toLong(), value.obj)
        }

        fun iterator(self: PythonProxyObject): Iterator<PythonProxyObject?> {
            return PythonListIterator(self.asInterface<PyList>())
        }
    }
}

class PythonListIterator(val list: PyList) : Iterator<PythonProxyObject?> {
    val idx = AtomicLong(0)
    val size = list.size()

    override fun hasNext(): Boolean {
        return idx.get() < size - 1
    }

    override fun next(): PythonProxyObject? {
        return list[idx.incrementAndGet()]
    }

}