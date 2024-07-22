package com.mylosoftworks.kpython.internal.engine

import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyTypeObject
import com.mylosoftworks.kpython.internal.engine.pythondefs.Py_ssize_t
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary

internal interface PythonEngineInterface : Library {
    // init and finalize
    fun Py_IsInitialized(): Boolean
    fun Py_Initialize()
    fun Py_InitializeEx(initSigs: Boolean)
    fun Py_Finalize()

    // Version and other info
    fun Py_GetVersion(): String
    fun Py_GetPlatform(): String
    fun Py_GetCopyright(): String
    fun Py_GetCompiler(): String
    fun Py_GetBuildInfo(): String

    // comparisons
    fun Py_Is(x: PyObject, y: PyObject): Boolean
    fun Py_IsNone(x: PyObject): Boolean
    fun Py_IsTrue(x: PyObject): Boolean
    fun Py_IsFalse(x: PyObject): Boolean
    fun Py_TYPE(o: PyObject): PyTypeObject
    fun Py_IS_TYPE(o: PyObject, type: PyTypeObject): Boolean
    fun Py_SET_TYPE(o: PyObject, type: PyTypeObject)
    fun Py_SIZE(o: PyObject): Py_ssize_t
    fun Py_SET_SIZE(o: PyObject, size: Py_ssize_t)

    // PyRun_
    fun PyRun_SimpleString(code: String): Int
    fun PyRun_String(code: String, start: Int, globals: PyObject, locals: PyObject): PyObject

    // Values
    // https://docs.python.org/3/c-api/arg.html#c.Py_BuildValue
    fun Py_BuildValue(format: String, vararg value: Any): PyObject
    fun PyLong_AsLong(obj: PyObject): Long
    fun PyFloat_AsDouble(obj: PyObject): Double
    fun PyUnicode_AsUTF8(obj: PyObject): String
    fun PyObject_IsTrue(obj: PyObject): Boolean
    fun PyObject_IsFalse(obj: PyObject): Boolean

    // builtins, globals, locals
    fun PyEval_GetBuiltins(): PyObject
    fun PyEval_GetLocals(): PyObject
    fun PyEval_GetGlobals(): PyObject

    // dicts
    fun PyDict_Check(p: PyObject): Boolean
    fun PyDict_CheckExact(p: PyObject): Boolean
    fun PyDict_New(): PyObject
    fun PyDict_Clear(p: PyObject)
    fun PyDict_Contains(p: PyObject, key: PyObject): Boolean
    fun PyDict_Copy(p: PyObject): PyObject
    fun PyDict_SetItem(p: PyObject, key: PyObject, value: PyObject): Boolean
    fun PyDict_SetItemString(p: PyObject, key: String, value: PyObject): Boolean
    fun PyDict_DelItem(p: PyObject, key: PyObject): Boolean
    fun PyDict_DelItemString(p: PyObject, key: String): Boolean
    fun PyDict_GetItem(p: PyObject, key: PyObject): PyObject
    fun PyDict_GetItemWithError(p: PyObject, key: PyObject): PyObject
    fun PyDict_GetItemString(p: PyObject, key: String): PyObject
    fun PyDict_SetDefault(p: PyObject, key: PyObject, defaultObject: PyObject): PyObject
    fun PyDict_Items(p: PyObject): PyObject
    fun PyDict_Keys(p: PyObject): PyObject
    fun PyDict_Values(p: PyObject): PyObject
    fun PyDict_Size(p: PyObject): Py_ssize_t

    // lists

    fun PyList_Check(): Boolean
    fun PyList_CheckExact(): Boolean
    fun PyList_new(len: Py_ssize_t): PyObject
    fun PyList_Size(list: PyObject): Py_ssize_t
    fun PyList_GET_SIZE(list: PyObject): Py_ssize_t
    fun PyList_GetItem(list: PyObject, index: Py_ssize_t): PyObject
    fun PyList_GET_ITEM(list: PyObject, index: Py_ssize_t): PyObject
    fun PyList_SetItem(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_SET_ITEM(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_Insert(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_Append(list: PyObject, item: PyObject): Boolean


    companion object // Uses extension methods just in case
}

internal fun PythonEngineInterface.Companion.initialize(pythonVersion: PythonVersion, pythonPath: String? = null): PythonEngineInterface {
    return initialize(pythonVersion.verName, pythonPath)
}

internal fun PythonEngineInterface.Companion.initialize(pythonVersion: String, pythonPath: String? = null): PythonEngineInterface {
    if (pythonPath != null) { NativeLibrary.addSearchPath(pythonVersion, pythonPath) }
    return Native.load(pythonVersion, PythonEngineInterface::class.java) as PythonEngineInterface
}