package com.mylosoftworks.kpython.environment.pythonobjects

import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.proxy.DontUsePython
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

interface PyDict : KPythonProxy {
    fun clear()
    fun copy(): PyDict
    fun items(): PythonProxyObject

    @DontUsePython
    fun getSize(): Long

    @DontUsePython
    operator fun get(key: Any?): PythonProxyObject?

    @DontUsePython
    operator fun set(key: Any?, value: Any?)

    @DontUsePython
    fun createMethod(name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?)

    @DontUsePython
    fun createMethodUnit(name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit)

    @DontUsePython
    fun invokeMethod(key: Any?, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject?

    @DontUsePython
    fun containsKey(key: Any?): Boolean

    @DontUsePython
    fun getKeys(): PyList

    @DontUsePython
    fun getKVPairs(): HashMap<PythonProxyObject, PythonProxyObject>

    companion object {
        fun getSize(self: PythonProxyObject): Long {
            return self.env.quickAccess.dictGetSize(self)
//            return self.let {
//                it.env.engine.PyDict_Size(it.obj)
//            }
        }

        fun get(self: PythonProxyObject, key: Any): PythonProxyObject? {
            return self.env.quickAccess.dictGetItem(self, key)
//            return self.let {
//                it.env.engine.PyDict_GetItem(it.obj, it.env.convertTo(key)!!.obj)?.let { it1 ->
//                    it.env.createProxyObject(
//                        it1, GCBehavior.IGNORE // Borrowed
//                    )
//                }
//            }
        }

        fun set(self: PythonProxyObject, key: Any?, value: Any?) {
            self.env.quickAccess.dictSetItem(self, key, value)
//            self.let {
//                it.env.engine.PyDict_SetItem(it.obj, it.env.convertTo(key)!!.obj, it.env.convertTo(value)!!.obj)
//            }
        }

        fun createMethod(self: PythonProxyObject, name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Any?) {
            val method = self.env.createFunction(self, name.toString(), docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }

        fun createMethodUnit(self: PythonProxyObject, name: Any?, docs: String = "", code: PyEnvironment. FunctionCallParams.() -> Unit) {
            val method = self.env.createFunctionUnit(self, name.toString(), docs, code)!!
            set(self, name, method.getKPythonProxyBase())
        }

        fun invokeMethod(self: PythonProxyObject, key: Any?, vararg args: Any, kwargs: HashMap<String, Any?>? = null): PythonProxyObject? {
//            return self.asInterface<PyDict>()[key]!!.asInterface<PyCallable>()(*args)
            return self.asInterface<PyDict>()[key]!!.invoke(*args, kwargs = kwargs)
        }

        fun containsKey(self: PythonProxyObject, key: Any?): Boolean {
            return self.env.quickAccess.dictContainsKey(self, key)
        }

        fun getKeys(self: PythonProxyObject): PyList {
            return self.env.quickAccess.dictGetKeys(self)
        }

        fun getKVPairs(self: PythonProxyObject): HashMap<PythonProxyObject, PythonProxyObject> {
            val keys = self.env.quickAccess.dictGetKeys(self)
            val selfProxy = self.asInterface<PyDict>()

            val outDict = HashMap<PythonProxyObject, PythonProxyObject>()
            for (key in keys) {
                outDict[key] = selfProxy[key]!!
            }
            return outDict
        }
    }
}