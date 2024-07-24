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

    init {
        engine.Py_Initialize()
    }

    constructor(version: PythonVersion, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))
    constructor(version: String, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))

    // Constants TODO: Find a better way to get these
    val None by lazy { eval("None")!! }
    val True by lazy { eval("True")!! }
    val False by lazy { eval("False")!! }
    val EmptyList by lazy { eval("[]")!! }
    val EmptyDict by lazy { eval("{}")!! }
    val Ellipsis by lazy { eval("...")!! }

    // Types TODO: Find a better way to get these
    val Str by lazy { eval("str")!! }
    val Int by lazy { eval("int")!! }
    val Float by lazy { eval("float")!! }
    val List by lazy { eval("list")!! }
    val Dict by lazy { eval("dict")!! }

    fun finalize() {
        engine.Py_Finalize()
    }

    val globals by lazy { engine.PyDict_New().asProxyObject().asInterface<PyDict<String>>() }
    val locals by lazy { engine.PyDict_New().asProxyObject().asInterface<PyDict<String>>() }

    /**
     * For isolated expressions, with return value
     */
    inline fun <reified T: KPythonProxy> eval(script: String, globals: PyDict<String> = this.globals, locals: PyDict<String> = this.locals) = eval(script, globals, locals)?.asInterface<T>()

    /**
     * For isolated expressions, with return value
     */
    fun eval(script: String, globals: PyDict<String> = this.globals, locals: PyDict<String> = this.locals): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject()
    }

    /**
     * For running code as if it were being ran from a file
     */
    fun file(script: String, globals: PyDict<String> = this.globals, locals: PyDict<String> = this.locals) {
        engine.PyRun_String(script, StartSymbol.File.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)
    }

    /**
     * For isolated expressions, with return value
     */
    inline fun <reified T: KPythonProxy> single(script: String, globals: PyDict<String> = this.globals, locals: PyDict<String> = this.locals) = single(script, globals, locals)?.asInterface<T>()

    /**
     * For running code line by line as if it were ran in an interactive terminal
     */
    fun single(script: String, globals: PyDict<String> = this.globals, locals: PyDict<String> = this.locals): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Single.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject()
    }

//    inline fun <reified T> convertFrom(input: PythonProxyObject): T {
//        return convertFrom(input, T::class.java) as T
//    }

    fun convertFrom(input: PythonProxyObject, type: Class<*>): Any? {
        return when {
            type == PythonProxyObject::class.java -> input
            KPythonProxy::class.java.isAssignableFrom(type) -> input.asInterface(type as Class<KPythonProxy>)
            engine.Py_IsNone(input.obj) -> null
            type == Boolean::class.java && engine.Py_IsTrue(input.obj) -> true
            type == Boolean::class.java && engine.Py_IsFalse(input.obj) -> false
            type in arrayOf(Long::class.java, Int::class.java, Short::class.java, Byte::class.java, UByte::class.java, UShort::class.java, UInt::class.java, ULong::class.java) && engine.PyObject_IsInstance(input.obj, Int.obj) -> engine.PyLong_AsLongLong(input.obj)
            type in arrayOf(Float::class.java, Double::class.java) && engine.PyObject_IsInstance(input.obj, Float.obj) -> engine.PyFloat_AsDouble(input.obj)
            type == String::class.java && engine.PyObject_IsInstance(input.obj, Str.obj) -> engine.PyUnicode_AsUTF8(input.obj)
            type == Array::class.java && engine.PyObject_IsInstance(input.obj, List.obj) -> readArray(input.obj, type.componentType)
            type == HashMap::class.java && engine.PyObject_IsInstance(input.obj, Dict.obj) -> readDict(input.obj)
            else -> input
        }
    }

    inline fun <reified T: KPythonProxy> convertTo(input: Any?): T? {
        return convertTo(input)?.asInterface()
    }

    fun convertArgs(vararg args: Any?): PythonProxyObject? {
        if (args.isEmpty()) return null

        val values = args.map { getConvertCharValuePair(it) }
        val inputString = values.joinToString("", prefix = "(", postfix = ")") { it.first }
        val inputValues = values.map { it.second }
        val result = engine.Py_BuildValue(inputString, *inputValues.toTypedArray())
        return result?.asProxyObject()
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

    fun getConvertCharValuePair(input: Any?): Pair<String, Any> {
        return when (input) {
            null -> "O" to None
            is PythonProxyObject -> "O" to input.obj
            is KPythonProxy -> "O" to input.getKPythonProxyBase().obj
            is String -> "s" to input
            is Float -> "f" to input
            is Double -> "d" to input
            is Char -> "C" to input
            is Long -> "L" to input
            is Int -> "i" to input
            is Boolean -> "b" to if (input) True else False

            else -> "O" to None
        }
    }

    private fun createArray(items: Array<*>): PyList {
        val list = engine.PyList_new()
        items.forEach {
            engine.PyList_Append(list ?: EmptyList.obj, convertTo(it)?.obj ?: None.obj)
        }
        return createProxyObject(list ?: EmptyList.obj).asInterface<PyList>()
    }

    private fun createDict(items: HashMap<*, *>): PyDict<String> {
        val dict = engine.PyDict_New()
        items.forEach {
            engine.PyDict_SetItem(dict, convertTo(it.key)?.obj ?: None.obj, convertTo(it.value)?.obj ?: None.obj)
        }
        return createProxyObject(dict).asInterface<PyDict<String>>()
    }

    private fun readArray(pyObject: PyObject, itemType: Class<*> = PythonProxyObject::class.java): Array<Any?> {
//        return Array(engine.PyList_Size(pyObject).toInt()) {
//            engine.PyList_GetItem(pyObject, it)?.asProxyObject() ?: None
//        }
        return Array(engine.PyList_Size(pyObject).toInt()) {
            convertFrom(engine.PyList_GetItem(pyObject, it.toLong())?.asProxyObject() ?: None, itemType)
        }
    }

    private fun readDict(pyObject: PyObject, keyType: Class<*> = PythonProxyObject::class.java, valueType: Class<*> = PythonProxyObject::class.java): HashMap<Any?, Any?> {
        return hashMapOf(*readArray(engine.PyDict_Keys(pyObject) ?: EmptyList.obj, keyType).zip(readArray(engine.PyDict_Values(pyObject) ?: EmptyList.obj, valueType)).toTypedArray())
    }

    private fun PyObject.asProxyObject(): PythonProxyObject = createProxyObject(this)

    internal fun createProxyObject(rawValue: PyObject): PythonProxyObject {
        return PythonProxyObject(this, rawValue)
    }

    private fun manualConvert(input: Any, type: String): PythonProxyObject? {
        return engine.Py_BuildValue(type, input)?.let { createProxyObject(it) }
    }
}