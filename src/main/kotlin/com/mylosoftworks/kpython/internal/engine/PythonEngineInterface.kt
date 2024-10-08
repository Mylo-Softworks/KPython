package com.mylosoftworks.kpython.internal.engine

import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyTypeObject
import com.mylosoftworks.kpython.internal.engine.pythondefs.Py_ssize_t
import com.sun.jna.*
import com.sun.jna.ptr.PointerByReference
import java.awt.Point


internal interface PythonEngineInterface : Library {
    // https://docs.python.org/3/c-api/structures.html#c.PyMethodDef
    @Structure.FieldOrder("ml_name", "ml_meth", "ml_flags", "ml_doc")
    open class PyMethodDef(@JvmField var ml_name: String?, @JvmField var ml_meth: PyCFunctionWithKwargs?, @JvmField var ml_flags: Int, @JvmField var ml_doc: String?): Structure() {
        class ByReference(ml_name: String?, ml_meth: PyCFunctionWithKwargs?, ml_flags: Int, ml_doc: String?) : PyMethodDef(ml_name, ml_meth, ml_flags, ml_doc), Structure.ByReference
    }

    interface PyCFunction : Callback { // https://docs.python.org/3/c-api/structures.html#c.PyCFunction
        fun invoke(self: PyObject?, args: PyObject?): PyObject?
    }

    interface PyCFunctionWithKwargs : Callback { // https://docs.python.org/3/c-api/structures.html#c.PyCFunctionWithKeywords
        fun invoke(self: PyObject?, args: PyObject?, kwargs: PyObject?): PyObject?
    }

    @Structure.FieldOrder("pointer")
    class DoublePointer(@JvmField var pointer: Pointer?): Structure(), Structure.ByReference

    @Structure.FieldOrder("slot", "pfunc")
    class PyType_Slot(
        @JvmField var slot: Int,
        @JvmField var pfunc: PyCFunctionWithKwargs?
    ): Structure(), Structure.ByValue {
        constructor() : this(0, null)
    }

    @Structure.FieldOrder("value")
    class PyType_SlotArray(
        @JvmField var value: Array<PyType_Slot>
    ): Structure(), Structure.ByReference {}

    @Structure.FieldOrder("name", "basicSize", "itemSize", "flags", "slots")
    open class PyType_Spec(
        @JvmField var name: String,
        @JvmField var basicSize: Int,
        @JvmField var itemSize: Int,
        @JvmField var flags: Int, // Uint
        @JvmField var slots: PyType_SlotArray, // Array of PyType_Slot structures. Terminated by the special slot value {0, NULL}.
//        @JvmField var slots: PyType_Slot, // Array of PyType_Slot structures. Terminated by the special slot value {0, NULL}.
    ): Structure(), Structure.ByReference

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

    // Environments
    fun Py_SetPythonHome(home: WString) // https://docs.python.org/3/c-api/init.html#c.Py_SetPythonHome
    fun Py_GetPythonHome(): WString

    // comparisons
    fun Py_Is(x: PyObject, y: PyObject): Boolean
    fun Py_IsNone(x: PyObject): Boolean
    fun Py_IsTrue(x: PyObject): Boolean
    fun Py_IsFalse(x: PyObject): Boolean
    fun PyObject_IsInstance(inst: PyObject, cls: PyObject): Boolean
    fun PyObject_RichCompare(x: PyObject, y: PyObject, opid: Int): PyObject
    fun PyObject_RichCompareBool(x: PyObject, y: PyObject, opid: Int): Int // -1 on error, 0 false, 1 true

    // PyRun_
    fun PyRun_SimpleString(code: String): Int
    fun PyRun_String(code: String, start: Int, globals: PyObject, locals: PyObject): PyObject?

    // Values
    // https://docs.python.org/3/c-api/arg.html#c.Py_BuildValue
    fun Py_BuildValue(format: String, vararg value: Any): PyObject?
    fun PyLong_AsLongLong(obj: PyObject): Long
    fun PyFloat_AsDouble(obj: PyObject): Double
    fun PyUnicode_AsUTF8(obj: PyObject): String
    fun PyUnicode_DecodeFSDefault(str: String): PyObject
    fun PyObject_IsTrue(obj: PyObject): Boolean
    fun PyObject_IsFalse(obj: PyObject): Boolean

//    fun Py_GetConstant(id: Int): PyObject // 3.13 and higher

    // builtins, globals, locals
    fun PyEval_GetBuiltins(): PyObject
    fun PyEval_GetLocals(): PyObject
    fun PyEval_GetGlobals(): PyObject

    // dicts
    fun PyDict_New(): PyObject
    fun PyDict_Clear(p: PyObject)
    fun PyDict_Contains(p: PyObject, key: PyObject): Int
    fun PyDict_Copy(p: PyObject): PyObject
    fun PyDict_SetItem(p: PyObject, key: PyObject, value: PyObject): Boolean
    fun PyDict_SetItemString(p: PyObject, key: String, value: PyObject): Boolean
    fun PyDict_DelItem(p: PyObject, key: PyObject): Boolean
    fun PyDict_DelItemString(p: PyObject, key: String): Boolean
    fun PyDict_GetItem(p: PyObject, key: PyObject): PyObject?
    fun PyDict_GetItemWithError(p: PyObject, key: PyObject): PyObject?
    fun PyDict_GetItemString(p: PyObject, key: String): PyObject?
    fun PyDict_SetDefault(p: PyObject, key: PyObject, defaultObject: PyObject): PyObject?
    fun PyDict_Items(p: PyObject): PyObject?
    fun PyDict_Keys(p: PyObject): PyObject
    fun PyDict_Values(p: PyObject): PyObject
    fun PyDict_Size(p: PyObject): Py_ssize_t

    // lists
    fun PyList_new(len: Py_ssize_t = 0L): PyObject?
    fun PyList_Size(list: PyObject): Py_ssize_t
    fun PyList_GET_SIZE(list: PyObject): Py_ssize_t
    fun PyList_GetItem(list: PyObject, index: Py_ssize_t): PyObject?
    fun PyList_GET_ITEM(list: PyObject, index: Py_ssize_t): PyObject?
    fun PyList_SetItem(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_SET_ITEM(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_Insert(list: PyObject, index: Py_ssize_t, item: PyObject): Boolean
    fun PyList_Append(list: PyObject, item: PyObject): Boolean

    // tuples
    fun PyTuple_Size(tuple: PyObject): Py_ssize_t
    fun PyTuple_GetItem(tuple: PyObject, pos: Py_ssize_t): PyObject?

    // python functions
    fun PyFunction_Check(p: PyObject): Boolean
    fun PyFunction_New(code: PyObject, globals: PyObject): PyObject?
    fun PyFunction_NewWithQualName(code: PyObject, globals: PyObject, qualname: PyObject): PyObject?
    fun PyFunction_GetCode(op: PyObject): PyObject

    // c functions
    fun PyCFunction_New(ml: PyMethodDef.ByReference, self: PyObject?): PyObject?
//    fun PyCMethod_New(ml: PyMethodDef.ByReference, self: PyObject, module: PyObject?, cls: PyTypeObject?): PyObject?

    // objects
    fun PyObject_HasAttr(o: PyObject, attr_name: PyObject): Boolean
    fun PyObject_HasAttrString(o: PyObject, attr_name: String): Boolean
    fun PyObject_Dir(o: PyObject): PyObject?
    fun PyObject_GetAttr(o: PyObject, attr_name: PyObject): PyObject?
    fun PyObject_GetAttrString(o: PyObject, attr_name: String): PyObject?
    fun PyObject_SetAttr(o: PyObject, attr_name: PyObject, attr_value: PyObject): Int
    fun PyObject_SetAttrString(o: PyObject, attr_name: String, attr_value: PyObject): Int
    fun PyObject_DelAttr(o: PyObject, attr_name: PyObject): Int
    fun PyObject_DelAttrString(o: PyObject, attr_name: String): Int
    fun PyObject_Str(o: PyObject): PyObject?

    // function calling (https://docs.python.org/3/c-api/call.html)
    fun PyCallable_Check(o: PyObject): Boolean
    fun PyObject_Call(callable: PyObject, args: PyObject, kwargs: PyObject?): PyObject?
    fun PyObject_CallNoArgs(callable: PyObject): PyObject?
    fun PyObject_CallOneArg(callable: PyObject, arg: PyObject): PyObject?
    fun PyObject_CallObject(callable: PyObject, args: PyObject?): PyObject?
    fun PyObject_CallFunction(callable: PyObject, format: String, vararg args: Any): PyObject? // https://docs.python.org/3/c-api/call.html#c.PyObject_CallFunction
    fun PyObject_CallMethod(obj: PyObject, name: String, format: String, vararg args: Any): PyObject?
    fun PyObject_CallFunctionObjArgs(callable: PyObject, vararg args: PyObject): PyObject?
    fun PyObject_CallMethodObjArgs(callable: PyObject, name: PyObject, vararg args: PyObject): PyObject?
    fun PyObject_CallMethodNoArgs(callable: PyObject, name: PyObject): PyObject?
    fun PyObject_CallMethodOneArg(callable: PyObject, name: PyObject, arg: PyObject): PyObject?

    // Modules
    fun PyImport_ImportModule(name: String): PyObject?
    fun PyModule_New(name: String): PyObject // New ref, see https://docs.python.org/3/c-api/module.html#c.PyModule_NewObject for information
    fun PyModule_GetDict(module: PyObject): PyObject?

    // Refcounting
    fun Py_IncRef(o: PyObject)
    fun Py_DecRef(o: PyObject)

    // Sys
    fun PySys_GetObject(name: String): PyObject? // Borrowed
    fun PySys_SetObject(name: String, value: PyObject): Int

    // Class/Type
    fun PyType_FromSpec(spec: PyType_Spec): PyTypeObject?
    fun PyType_GetDict(type: PyTypeObject): PyObject?
    fun PyType_GetSlot(type: PyTypeObject, slot: Int): Pointer

    // Errors
    fun PyErr_GetRaisedException(): PyObject? // 3.12 only
    fun PyErr_Fetch(pType: DoublePointer, pValue: DoublePointer, pTraceback: DoublePointer) // 3+, deprecated in 3.12
    fun PyErr_SetObject(type: PyObject, value: PyObject)

    // Byte[]
    fun PyByteArray_Size(pyObject: PyObject): Long
    fun PyByteArray_AsString(pyObject: PyObject): Pointer?

    companion object // Uses extension methods just in case
}

internal fun PythonEngineInterface.Companion.initialize(pythonVersion: PythonVersion, pythonPath: String? = null): PythonEngineInterface {
    return initialize(pythonVersion.toString(), pythonPath)
}

internal fun PythonEngineInterface.Companion.initialize(pythonVersion: String, pythonPath: String? = null): PythonEngineInterface {
    if (pythonPath != null) { NativeLibrary.addSearchPath(pythonVersion, pythonPath) }
    return Native.load(pythonVersion, PythonEngineInterface::class.java) as PythonEngineInterface
}