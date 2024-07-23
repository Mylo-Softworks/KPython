package com.mylosoftworks.kpython.environment

import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.pythonobjects.PyDict
import com.mylosoftworks.kpython.environment.pythonobjects.PyList
import com.mylosoftworks.kpython.internal.engine.PythonEngineInterface
import com.mylosoftworks.kpython.internal.engine.StartSymbol
import com.mylosoftworks.kpython.internal.engine.initialize
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject

/**
 * Represents a python environment, only one can exist at a time per process currently.
 */
class PyEnvironment internal constructor(internal val engine: PythonEngineInterface) {

    constructor(version: PythonVersion, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))
    constructor(version: String, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))

    val None by lazy { PythonProxyObject(this, engine.PyRun_String("None", StartSymbol.Eval.value, engine.PyDict_New(), engine.PyDict_New())!!) }
    val True by lazy { PythonProxyObject(this, engine.PyRun_String("True", StartSymbol.Eval.value, engine.PyDict_New(), engine.PyDict_New())!!) }
    val False by lazy { PythonProxyObject(this, engine.PyRun_String("False", StartSymbol.Eval.value, engine.PyDict_New(), engine.PyDict_New())!!) }
    val EmptyList by lazy { PythonProxyObject(this, engine.PyRun_String("[]", StartSymbol.Eval.value, engine.PyDict_New(), engine.PyDict_New())!!) }
    val EmptyDict by lazy { PythonProxyObject(this, engine.PyRun_String("{}", StartSymbol.Eval.value, engine.PyDict_New(), engine.PyDict_New())!!) }

    init {
        engine.Py_Initialize()
    }

    fun finalize() {
        engine.Py_Finalize()
    }

    inline fun <reified T: KPythonProxy> eval(script: String, locals: PyDict, globals: PyDict) = eval(script, globals, locals)?.asInterface<T>()

    fun eval(script: String, locals: PyDict = engine.PyDict_New().asProxyObject().asInterface(), globals: PyDict = engine.PyDict_New().asProxyObject().asInterface()): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject()
    }

    inline fun <reified T> convertFrom(input: PythonProxyObject): T {
        return convertFrom(input, T::class.java) as T
    }

    fun convertFrom(input: PythonProxyObject, type: Class<*>): Any? {
        return when {
            engine.Py_IsNone(input.obj) -> null
            engine.Py_IsTrue(input.obj) -> true
            engine.Py_IsFalse(input.obj) -> false
            engine.PyLong_Check(input.obj) -> engine.PyLong_AsLongLong(input.obj)
            engine.PyFloat_Check(input.obj) -> engine.PyFloat_AsDouble(input.obj)
            engine.PyList_Check(input.obj) -> readArray(input.obj)
            engine.PyDict_Check(input.obj) -> readDict(input.obj)
            else -> input
        }
    }

    inline fun <reified T: KPythonProxy> convertTo(input: Any?): T? {
        return convertTo(input)?.asInterface()
    }

    fun convertTo(input: Any?): PythonProxyObject? {
        return when (input) {
            null -> None
            is PythonProxyObject -> input
            is KPythonProxy -> input.getKPythonProxyBase()
            is String -> manualConvert(input, "s")
            is Float -> manualConvert(input, "f")
            is Double -> manualConvert(input, "d")
            is Char -> manualConvert(input, "C")
            is Long -> manualConvert(input, "L")
            is Int -> manualConvert(input, "i")
            is Boolean -> manualConvert(if (input) True else False, "b")
            else -> None
        }
    }

    private fun createArray(items: Array<*>): PyList {
        val list = engine.PyList_new()
        items.forEach {
            engine.PyList_Append(list ?: EmptyList.obj, convertTo(it)?.obj ?: None.obj)
        }
        return createProxyObject(list ?: EmptyList.obj).asInterface<PyList>()
    }

    private fun createDict(items: HashMap<*, *>): PyDict {
        val dict = engine.PyDict_New()
        items.forEach {
            engine.PyDict_SetItem(dict, convertTo(it.key)?.obj ?: None.obj, convertTo(it.value)?.obj ?: None.obj)
        }
        return createProxyObject(dict).asInterface<PyDict>()
    }

    private fun readArray(pyObject: PyObject): Array<PythonProxyObject> {
        return Array(engine.PyList_Size(pyObject).toInt()) {
            engine.PyList_GetItem(pyObject, 0)?.asProxyObject() ?: None
        }
    }

    private fun readDict(pyObject: PyObject): HashMap<PythonProxyObject, PythonProxyObject> {
        return hashMapOf(*readArray(engine.PyDict_Keys(pyObject) ?: EmptyList.obj).zip(readArray(engine.PyDict_Values(pyObject) ?: EmptyList.obj)).toTypedArray())
    }

    private fun PyObject.asProxyObject(): PythonProxyObject = createProxyObject(this)

    private fun createProxyObject(rawValue: PyObject): PythonProxyObject {
        return PythonProxyObject(this, rawValue)
    }

    private fun manualConvert(input: Any, type: String): PythonProxyObject? {
        return engine.Py_BuildValue(type, input)?.let { createProxyObject(it) }
    }
}