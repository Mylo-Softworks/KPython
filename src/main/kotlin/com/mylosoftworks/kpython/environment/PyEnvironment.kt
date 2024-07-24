package com.mylosoftworks.kpython.environment

import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.pythonobjects.*
import com.mylosoftworks.kpython.internal.engine.METH_VARARGS
import com.mylosoftworks.kpython.internal.engine.PythonEngineInterface
import com.mylosoftworks.kpython.internal.engine.StartSymbol
import com.mylosoftworks.kpython.internal.engine.initialize
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import java.nio.file.Paths

/**
 * Represents a python environment, only one can exist at a time per process currently.
 */
class PyEnvironment internal constructor(internal val engine: PythonEngineInterface) {

    // Constants TODO: Find a better way to get these
    var None: PythonProxyObject
    var True: PythonProxyObject
    var False: PythonProxyObject
    var EmptyList: PythonProxyObject
    var EmptyDict: PythonProxyObject
    var EmptyTuple: PythonProxyObject
    var Ellipsis: PythonProxyObject

    // Types TODO: Find a better way to get these
    var Str: PythonProxyObject
    var Int: PythonProxyObject
    var Float: PythonProxyObject
    var List: PythonProxyObject
    var Dict: PythonProxyObject
    var Tuple: PythonProxyObject

    var globals: PyDict
    var locals: PyDict

    init {
        engine.Py_Initialize()

        globals = createProxyObject(engine.PyDict_New(), GCBehavior.ONLY_DEC).asInterface<PyDict>()
        locals = createProxyObject(engine.PyDict_New(), GCBehavior.ONLY_DEC).asInterface<PyDict>()

        None = evalGC("None", gcBehavior = GCBehavior.IGNORE)!!
        True = eval("True")!!
        False = eval("False")!!
        EmptyList = eval("[]")!!
        EmptyDict = eval("{}")!!
        EmptyTuple = eval("()")!!
        Ellipsis = eval("...")!!

        Str = eval("str")!!
        Int = eval("int")!!
        Float = eval("float")!!
        List = eval("list")!!
        Dict = eval("dict")!!
        Tuple = eval("tuple")!!
    }

    constructor(version: PythonVersion, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))
    constructor(version: String, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))

    fun finalize() {
        engine.Py_Finalize()
    }

    /**
     * For isolated expressions, with return value
     */
    inline fun <reified T: KPythonProxy> eval(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals) = eval(script, globals, locals)?.asInterface<T>()

    /**
     * For isolated expressions, with return value
     */
    fun eval(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject()
    }

    internal fun evalGC(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals, gcBehavior: GCBehavior = GCBehavior.FULL): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)
            ?.let { createProxyObject(it, gcBehavior) }
    }

    /**
     * For running code as if it were being ran from a file
     */
    fun file(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals) {
        engine.PyRun_String(script, StartSymbol.File.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)
    }

    /**
     * For isolated expressions, with return value
     */
    inline fun <reified T: KPythonProxy> single(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals) = single(script, globals, locals)?.asInterface<T>()

    /**
     * For running code line by line as if it were ran in an interactive terminal
     */
    fun single(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals): PythonProxyObject? {
        return engine.PyRun_String(script, StartSymbol.Single.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject()
    }

//    inline fun <reified T> convertFrom(input: PythonProxyObject): T {
//        return convertFrom(input, T::class.java) as T
//    }

    fun convertFrom(input: PythonProxyObject, type: Class<*>): Any? {
        val typeIsBool = type in arrayOf(Boolean::class.java, java.lang.Boolean::class.java)
        return when {
            type == PythonProxyObject::class.java -> input
            KPythonProxy::class.java.isAssignableFrom(type) -> input.asInterface(type as Class<KPythonProxy>)
            engine.Py_IsNone(input.obj) -> null
            typeIsBool && engine.Py_IsTrue(input.obj) -> true
            typeIsBool && engine.Py_IsFalse(input.obj) -> false
            type in arrayOf(Long::class.java, java.lang.Long::class.java, Int::class.java, java.lang.Integer::class.java, Short::class.java, java.lang.Short::class.java, Byte::class.java, java.lang.Byte::class.java, UByte::class.java, UShort::class.java, UInt::class.java, ULong::class.java) && engine.PyObject_IsInstance(input.obj, Int.obj) -> engine.PyLong_AsLongLong(input.obj)
            type in arrayOf(Float::class.java, java.lang.Float::class.java, Double::class.java, java.lang.Double::class.java) && engine.PyObject_IsInstance(input.obj, Float.obj) -> engine.PyFloat_AsDouble(input.obj)
            type in arrayOf(java.lang.String::class.java, String::class.java) && engine.PyObject_IsInstance(input.obj, Str.obj) -> engine.PyUnicode_AsUTF8(input.obj)
            (type == Array::class.java || type.isArray) && engine.PyObject_IsInstance(input.obj, List.obj) -> readArray(input.obj, type.componentType)
            type == HashMap::class.java && engine.PyObject_IsInstance(input.obj, Dict.obj) -> readDict(input.obj)
            else -> input
        }
    }

    inline fun <reified T: KPythonProxy> convertTo(input: Any?): T? {
        return convertTo(input)?.asInterface()
    }

    fun convertArgs(vararg args: Any?, prefix: String = "(", postfix: String = ")"): PythonProxyObject? {
        if (args.isEmpty()) return null

        val values = args.map { getConvertCharValuePair(it) }
        val inputString = values.joinToString("", prefix = prefix, postfix = postfix) { it.first }
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
            is Array<*> -> createList(*(input as Array<Any>))?.getKPythonProxyBase()
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
        return createProxyObject(list ?: EmptyList.obj, GCBehavior.ONLY_DEC).asInterface<PyList>()
    }

    private fun createDict(items: HashMap<*, *>): PyDict {
        val dict = engine.PyDict_New()
        items.forEach {
            engine.PyDict_SetItem(dict, convertTo(it.key)?.obj ?: None.obj, convertTo(it.value)?.obj ?: None.obj)
        }
        return createProxyObject(dict, GCBehavior.ONLY_DEC).asInterface<PyDict>()
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

    fun createProxyObject(rawValue: PyObject): PythonProxyObject {
        return createProxyObject(rawValue, GCBehavior.FULL)
    }

    internal fun createProxyObject(rawValue: PyObject, gcBehavior: GCBehavior = GCBehavior.FULL): PythonProxyObject {
        return PythonProxyObject(this, rawValue, gcBehavior)
    }

    private fun manualConvert(input: Any, type: String): PythonProxyObject? {
        return engine.Py_BuildValue(type, input)?.let { createProxyObject(it, GCBehavior.ONLY_DEC) }
    }

    // Creation functions
    fun createTuple(vararg args: Any): PyTuple? {
        return convertArgs(*args)?.asInterface<PyTuple>()
    }

    fun createList(vararg args: Any): PyList? {
        return convertArgs(*args, prefix = "[", postfix = "]")?.asInterface<PyList>()
    }

    data class FunctionCallParams(val self: PythonProxyObject?, val args: PyTuple, val env: PyEnvironment)
    class PyKotlinFunction(val env: PyEnvironment, val function: FunctionCallParams.() -> Any?) : PythonEngineInterface.PyCFunction {
        override fun invoke(self: PyObject?, args: PyObject?): PyObject? {
            return env.convertTo(function(FunctionCallParams(self?.let { env.createProxyObject(it) }, args?.let { env.createProxyObject(it).asInterface<PyTuple>() } ?: env.EmptyTuple.asInterface<PyTuple>(), env)))?.obj
        }
    }

    fun createFunction(self: PythonProxyObject? = null, name: String = "", doc: String = "", function: FunctionCallParams.() -> Any?): PyCallable? {
        val callback = PyKotlinFunction(this, function)
        val functionStruct = PythonEngineInterface.PyMethodDef.ByReference(name, callback, METH_VARARGS, doc)
        return engine.PyCFunction_New(functionStruct, self?.obj ?: Str.asInterface<PyClass>().invoke()!!.obj)
            ?.let { createProxyObject(it, GCBehavior.ONLY_DEC).asInterface<PyCallable>() }
    }

    fun createFunctionUnit(self: PythonProxyObject? = null, name: String = "", doc: String = "", function: FunctionCallParams.() -> Unit): PyCallable? {
        return createFunction(self, name, doc) { function(this); None }
    }

    // Functions during python code (Like in a callback)

    enum class DefType(internal val func: (PythonEngineInterface) -> PyObject) {
        BUILTINS({ it.PyEval_GetBuiltins() }),
        GLOBALS({ it.PyEval_GetGlobals() }),
        LOCALS({ it.PyEval_GetLocals() }),
    }

    fun getDefDict(type: DefType): PyDict {
        return createProxyObject(type.func(engine), GCBehavior.IGNORE).asInterface<PyDict>() // Borrowed
    }

    // Common functions
    fun import(name: String): PythonProxyObject? {
        return engine.PyImport_ImportModule(name)?.asProxyObject()
    }

    inline fun <reified T: KPythonProxy> import(name: String): T? {
        return import(name)?.asInterface<T>()
    }

    fun setFakeFileDir(dir: String, fileName: String = "fake_file.py", globals: PyDict? = null) {
        // Set path
        val path = engine.PySys_GetObject("path")!! // Borrowed
        val d = engine.PyUnicode_DecodeFSDefault(dir)
        engine.PyList_Insert(path, 0, d)
        engine.Py_DecRef(d)

        // Set __file__
        (globals ?: this.globals)["__file__"] = convertTo(Paths.get(dir, fileName))!!
    }

    fun setArgv(fileName: String = "fake_file.py", vararg args: String) {
        val allArgs = args.toMutableList().apply { add(0, fileName) }.toTypedArray()
        engine.PySys_SetObject("argv", convertTo(allArgs)!!.obj)
    }

    fun getArgv(): List<String>? {
        val argv = engine.PySys_GetObject("argv") ?: return null
        val proxy = createProxyObject(argv, GCBehavior.IGNORE).asInterface<PyList>() // Borrowed
        return List(proxy.size().toInt()) {
            proxy[it].toString()
        }
    }

    /**
     * Get a sys variable, borrowed, so python refcount is not affected
     */
    inline fun <reified T> getSys(name: String): T? {
        return getSys(name)?.let { convertFrom(it, T::class.java) } as T?
    }

    /**
     * Get a sys variable, borrowed, so python refcount is not affected
     */
    fun getSys(name: String): PythonProxyObject? {
        return engine.PySys_GetObject(name)?.let { createProxyObject(it, GCBehavior.IGNORE) } // Borrowed
    }

    /**
     * Set a sys variable, value is converted unless already PythonProxyObject/KPythonProxy
     */
    fun setSys(name: String, value: Any) {
        val converted = convertTo(value) ?: return
        engine.PySys_SetObject(name, converted.obj)
    }
}

fun String.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)!!
}

fun Number.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)!!
}

fun Array<*>.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)!!
}

fun HashMap<*, *>.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)!!
}