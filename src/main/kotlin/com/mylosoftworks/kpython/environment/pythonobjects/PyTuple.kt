package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import java.util.concurrent.atomic.AtomicLong

interface PyTuple : KPythonProxy {

    @DontUsePython
    fun size(): Long

    @DontUsePython
    operator fun get(idx: Long): PythonProxyObject?

    @DontUsePython
    operator fun component1(): PythonProxyObject?

    @DontUsePython
    operator fun component2(): PythonProxyObject?

    @DontUsePython
    operator fun component3(): PythonProxyObject?

    @DontUsePython
    operator fun component4(): PythonProxyObject?

    @DontUsePython
    operator fun component5(): PythonProxyObject?

    @DontUsePython
    operator fun component6(): PythonProxyObject?

    @DontUsePython
    operator fun component7(): PythonProxyObject?

    @DontUsePython
    operator fun component8(): PythonProxyObject?

    @DontUsePython
    operator fun component9(): PythonProxyObject?

    @DontUsePython
    operator fun component10(): PythonProxyObject?

    @DontUsePython
    operator fun iterator(): Iterator<PythonProxyObject>

    companion object {
        fun size(self: PythonProxyObject): Long {
            return self.env.quickAccess.tupleGetSize(self)
//            return self.let {
//                it.env.engine.PyTuple_Size(it.obj)
//            }
        }

        fun get(self: PythonProxyObject, idx: Long): PythonProxyObject? {
            return self.env.quickAccess.tupleGetItem(self, idx)
//            return self.let {
//                it.env.engine.PyTuple_GetItem(it.obj, idx)?.let { it2 ->
//                    it.env.createProxyObject(it2)
//                }
//            }
        }

        fun component1(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 0)
        }

        fun component2(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 1)
        }

        fun component3(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 2)
        }

        fun component4(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 3)
        }

        fun component5(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 4)
        }

        fun component6(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 5)
        }

        fun component7(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 6)
        }

        fun component8(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 7)
        }

        fun component9(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 8)
        }

        fun component10(self: PythonProxyObject): PythonProxyObject? {
            return get(self, 10)
        }

        fun iterator(self: PythonProxyObject): Iterator<PythonProxyObject?> {
            return PythonTupleIterator(self.asInterface<PyTuple>())
        }
    }
}

class PythonTupleIterator(val list: PyTuple) : Iterator<PythonProxyObject?> {
    val idx = AtomicLong(0)
    val size = list.size()

    override fun hasNext(): Boolean {
        return idx.get() < size
    }

    override fun next(): PythonProxyObject? {
        return list[idx.getAndIncrement()]
    }
}