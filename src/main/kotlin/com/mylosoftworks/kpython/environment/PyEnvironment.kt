package com.mylosoftworks.kpython.environment

import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.pythonobjects.*
import com.mylosoftworks.kpython.internal.engine.METH_KEYWORDS
import com.mylosoftworks.kpython.internal.engine.METH_VARARGS
import com.mylosoftworks.kpython.internal.engine.PythonEngineInterface
import com.mylosoftworks.kpython.internal.engine.StartSymbol
import com.mylosoftworks.kpython.internal.engine.initialize
import com.mylosoftworks.kpython.internal.engine.pythondefs.PyObject
import com.mylosoftworks.kpython.proxy.GCBehavior
import com.mylosoftworks.kpython.proxy.KPythonProxy
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import java.nio.file.Paths

class PythonException(message: String) : RuntimeException(message)

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
    var Type: PythonProxyObject

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

        None = evalGC("None", gcBehavior = GCBehavior.IGNORE)
        True = eval("True")
        False = eval("False")
        EmptyList = eval("[]")
        EmptyDict = eval("{}")
        EmptyTuple = eval("()")
        Ellipsis = eval("...")
        Type = eval("type")

        Str = eval("str")
        Int = eval("int")
        Float = eval("float")
        List = eval("list")
        Dict = eval("dict")
        Tuple = eval("tuple")
    }

    constructor(version: PythonVersion, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))
    constructor(version: String, pythonPath: String? = null) : this(PythonEngineInterface.initialize(version, pythonPath))

    fun finalize() {
        engine.Py_Finalize()
    }

    /**
     * For isolated expressions, with return value
     */
    inline fun <reified T: KPythonProxy> eval(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals) = eval(script, globals, locals).asInterface<T>()

    /**
     * For isolated expressions, with return value
     */
    fun eval(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals): PythonProxyObject {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject() ?: quickAccess.throwAutoError()
    }

    internal fun evalGC(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals, gcBehavior: GCBehavior = GCBehavior.FULL): PythonProxyObject {
        return engine.PyRun_String(script, StartSymbol.Eval.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)
            ?.let { createProxyObject(it, gcBehavior) } ?: quickAccess.throwAutoError()
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
    inline fun <reified T: KPythonProxy> single(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals) = single(script, globals, locals).asInterface<T>()

    /**
     * For running code line by line as if it were ran in an interactive terminal
     */
    fun single(script: String, globals: PyDict = this.globals, locals: PyDict = this.locals): PythonProxyObject {
        return engine.PyRun_String(script, StartSymbol.Single.value, locals.getKPythonProxyBase().obj, globals.getKPythonProxyBase().obj)?.asProxyObject() ?: quickAccess.throwAutoError()
    }

//    inline fun <reified T> convertFrom(input: PythonProxyObject): T {
//        return convertFrom(input, T::class.java) as T
//    }

    fun convertFrom(input: PythonProxyObject?, type: Class<*>): Any? {
        val typeIsBool = type in arrayOf(Boolean::class.java, java.lang.Boolean::class.java)
        return when {
            input == null -> null

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

    inline fun <reified T: KPythonProxy> convertToI(input: Any?): T {
        return convertTo(input).asInterface<T>()
    }

    fun convertArgs(vararg args: Any?, prefix: String = "(", postfix: String = ")"): PythonProxyObject {
//        if (args.isEmpty()) return null

        val values = args.map { getConvertCharValuePair(it) }
        val inputString = values.joinToString("", prefix = prefix, postfix = postfix) { it.first }
        val inputValues = values.map { it.second }
        val result = engine.Py_BuildValue(inputString, *inputValues.toTypedArray())
        return result?.let { createProxyObject(it, GCBehavior.ONLY_DEC) } ?: quickAccess.throwAutoError()
    }

    fun convertTo(input: Any?): PythonProxyObject {
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
            is Boolean -> if (input) True else False
            is Array<*> -> createList(*(input as Array<Any?>)).getKPythonProxyBase()
            is HashMap<*, *> -> createDict(input as HashMap<Any?, Any?>).getKPythonProxyBase()

            else -> None
        }
    }

    private fun getConvertCharValuePair(input: Any?): Pair<String, Any> {
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
            is Array<*> -> "O" to (createList(*(input as Array<Any?>)).getKPythonProxyBase().obj)
            is HashMap<*, *> -> "O" to (createDict(input as HashMap<Any?, Any?>).getKPythonProxyBase().obj)

            else -> "O" to None
        }
    }

//    private fun createArray(items: Array<*>? = null): PyList {
//        val list = engine.PyList_new()
//        items?.forEach {
//            engine.PyList_Append(list ?: EmptyList.obj, convertTo(it)?.obj ?: None.obj)
//        }
//        return createProxyObject(list ?: EmptyList.obj, GCBehavior.ONLY_DEC).asInterface<PyList>()
//    }

    private fun createDict(items: HashMap<*, *>? = null): PyDict {
        val dict = engine.PyDict_New()
        items?.forEach {
            engine.PyDict_SetItem(dict, convertTo(it.key).obj, convertTo(it.value).obj)
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
        return hashMapOf(*readArray(engine.PyDict_Keys(pyObject), keyType).zip(readArray(engine.PyDict_Values(pyObject), valueType)).toTypedArray())
    }

    private fun PyObject.asProxyObject(): PythonProxyObject = createProxyObject(this)

    fun createProxyObject(rawValue: PyObject): PythonProxyObject {
        return createProxyObject(rawValue, GCBehavior.FULL)
    }

    fun createProxyObject(rawValue: PyObject, gcBehavior: GCBehavior = GCBehavior.FULL): PythonProxyObject {
        return PythonProxyObject(this, rawValue, gcBehavior)
    }

    private fun manualConvert(input: Any, type: String): PythonProxyObject {
        return engine.Py_BuildValue(type, input)?.let { createProxyObject(it, GCBehavior.ONLY_DEC) } ?: quickAccess.throwAutoError()
    }

    // Creation functions
    fun createTuple(vararg args: Any?): PyTuple {
        return convertArgs(*args).asInterface<PyTuple>()
    }

    fun createList(vararg args: Any?): PyList {
        return convertArgs(*args, prefix = "[", postfix = "]").asInterface<PyList>()
    }

    data class FunctionCallParams(val self: PythonProxyObject, val args: PyTuple, val kwargs: PyDict, val env: PyEnvironment) {
        /**
         * Parses python function call arguments by python definition style.
         *
         * Example: `parseArgumentsByDefinition("arg1, arg2, arg3, /, **kwargs")`
         * @return A HashMap with the names as keys, values as values. If not provided, the key will not exist.
         */
        fun parseArgumentsByDefinition(definition: String): HashMap<String, PythonProxyObject> {
            val split = definition.split(Regex("\\s*,\\s*"))

            val leftHandArgs = mutableListOf<String>() // Everything to the left of either / or *args
            val kwOnlyArgs = mutableListOf<String>() // Everything to the right of either / or *args
            var leftHandIsPositionalOnly = false // If contains / or *args, will be true
            var kwargsName: String? = null // If contains **kwargs, will be kwargs
            var varargsName: String? = null // If contains *args, will be args

            for (str in split) {
                when {
                    str.startsWith("**") -> {
                        kwargsName = str.removePrefix("**")
                        // Completed foreach since kwargs must be last
                        break
                    }
                    str.startsWith("*") -> {
                        varargsName = str.removePrefix("*")
                        leftHandIsPositionalOnly = true // From this point on
                    }
                    str.equals("/") -> {
                        leftHandIsPositionalOnly = true // From this point on
                    }
                    else -> {
                        if (leftHandIsPositionalOnly) {
                            kwOnlyArgs.add(str)
                        }
                        else {
                            leftHandArgs.add(str)
                        }
                    }
                }
            }

            val outMap = HashMap<String, PythonProxyObject>()

            // Lets first use positional args
            val argCount = args.size()
            if(leftHandArgs.size > argCount && varargsName == null) throw IllegalArgumentException("Could not parse arguments")
            val max: Int? = if (varargsName == null) null else leftHandArgs.size - 1
            val collectedVArgs = mutableListOf<PythonProxyObject>()
            for ((idx, value) in args.iterator().withIndex()) {
                if (max != null && idx > max) {
                    collectedVArgs.add(value) // Add to varargs
                }
                else {
                    outMap[leftHandArgs[idx]] = value
                }
            }

            if (varargsName != null) outMap[varargsName] = env.convertTo(collectedVArgs.toTypedArray())


            // Now lets read keyword args
            val kvPairs = kwargs.getKVPairs()
            val collectedKWArgs = hashMapOf<String, PythonProxyObject>()
            val availableKWArgs = if (leftHandIsPositionalOnly) kwOnlyArgs else leftHandArgs.toMutableList().let { it.subList(argCount.toInt(), it.size) }.apply { addAll(kwOnlyArgs) }
            for ((key, value) in kvPairs) {
                val keyTS = key.toString()
                if (keyTS in availableKWArgs) {
                    outMap[keyTS] = value
                }
                else {
                    collectedKWArgs[keyTS] = value
                }
            }

            if (kwargsName != null) outMap[kwargsName] = env.convertTo(collectedKWArgs)

            return outMap
        }
    }


    class PyKotlinFunction(val env: PyEnvironment, val function: FunctionCallParams.() -> Any?) : PythonEngineInterface.PyCFunctionWithKwargs {
        override fun invoke(self: PyObject?, args: PyObject?, kwargs: PyObject?): PyObject {
            return env.convertTo(
                function(
                    FunctionCallParams(
                        self?.let {
                            env.createProxyObject(it)
                                  } ?: env.None,
                        args?.let {
                            env.createProxyObject(it).asInterface<PyTuple>()
                        } ?: env.EmptyTuple.asInterface<PyTuple>(),
                        kwargs?.let {
                            env.createProxyObject(it).asInterface<PyDict>()
                        } ?: env.EmptyDict.asInterface<PyDict>(),
                        env
                    )
                )
            ).obj
        }
    }

    fun createFunction(self: PythonProxyObject? = null, name: String = "", doc: String = "", function: FunctionCallParams.() -> Any?): PyCallable {
        val callback = PyKotlinFunction(this, function)
        val method_flags = METH_VARARGS or METH_KEYWORDS
//            if (pyClass == null) METH_VARARGS or METH_KEYWORDS
//            else METH_VARARGS or METH_KEYWORDS or METH_CLASS

        val functionStruct = PythonEngineInterface.PyMethodDef.ByReference(name, callback, method_flags, doc)
        val func = engine.PyCFunction_New(functionStruct, self?.obj ?: Str.asInterface<PyClass>().invoke()!!.obj)
            ?.let { createProxyObject(it, GCBehavior.ONLY_DEC).asInterface<PyCallable>() } ?: quickAccess.throwAutoError()

        return func

//        return engine.PyCMethod_New(functionStruct, self?.obj ?: Str.asInterface<PyClass>().invoke()!!.obj, null, pyClass?.getKPythonProxyBase()?.obj)
//            ?.let { createProxyObject(it, GCBehavior.ONLY_DEC).asInterface<PyCallable>() } ?: quickAccess.throwAutoError()
    }

    fun createFunctionUnit(self: PythonProxyObject? = null, name: String = "", doc: String = "", function: FunctionCallParams.() -> Unit): PyCallable {
        return createFunction(self, name, doc) { function(this); None }
    }

    fun createModule(name: String, register: Boolean = true): PyModule {
        val mod = createProxyObject(engine.PyModule_New(name), GCBehavior.ONLY_DEC)
        if (register) {
            getSys<PyDict>("modules")[name] = mod
        }
        return mod.asInterface<PyModule>()
    }

    fun createClass(name: String, init: FunctionCallParams.() -> Unit): PyClass {
//        val type = engine.PyType_FromSpec(PythonEngineInterface.PyType_Spec(name, 0, 0, 0,
//                arrayOf(
//                    PythonEngineInterface.PyType_Slot(Py_tp_init, PyKotlinFunction(this, init)), // TODO: Get init working
//                PythonEngineInterface.PyType_Slot(0, null))
//            )
//        )
//        return type?.let { createProxyObject(it, GCBehavior.ONLY_DEC) }?.asInterface() ?: quickAccess.throwAutoError()

        // WARNING: This is very hacky, and should be improved upon.

        val tempGlobals = createDict()
        single("""
            class $name:
              pass
        """.trimIndent(), globals = tempGlobals)
        val base = tempGlobals[name]!!
        val clazz = base.asInterface<PyClass>()
        clazz.__name__ = name // Set the name of the class
//        clazz.getKPythonProxyBase()["__new__"] = createFunction(function = init).getKPythonProxyBase()
//        val oldNew = clazz.getKPythonProxyBase()["__new__"]

        val newNew: FunctionCallParams.() -> Any? = newNew@{
//            val inst = import("builtins").invokeMethod("super", self!!["__class__"], self)?.invokeMethod("__new__", self)
//            val inst = import("builtins").invokeMethod("object", clazz)
            val inst = import("builtins")["object"].invokeMethod("__new__", clazz)

            val newArgs = mutableListOf<PythonProxyObject>()
            val iter = args.iterator()
            iter.next() // Skip first item (Would be class)
            for (item in iter) {
                newArgs.add(item)
            }

            val initFunc = createFunction(inst, "__init__", function = init).getKPythonProxyBase() // Set __init__ to the init supplied in function call
            inst["__init__"] = initFunc // Rewrite __init__ on the object
            engine.PyObject_Call(initFunc.obj, convertTo(newArgs.toTypedArray()).obj, kwargs.getKPythonProxyBase().obj) // Call __init__

            return@newNew inst
        }

        clazz.getKPythonProxyBase()["__new__"] = createFunction(name = "__new__", function = newNew).getKPythonProxyBase()
        return clazz
    }

    // Functions during python code (Like in a callback)

    enum class DefType(internal val func: (PythonEngineInterface) -> PyObject) {
        BUILTINS({ it.PyEval_GetBuiltins() }),
        GLOBALS({ it.PyEval_GetGlobals() }),
        LOCALS({ it.PyEval_GetLocals() }),
    }

    fun getDefDict(type: DefType): PyDict {
        return createProxyObject(type.func(engine), GCBehavior.FULL).asInterface<PyDict>() // Borrowed, but given to kotlin
    }

    // Common functions

    fun import(name: String): PythonProxyObject {
        return engine.PyImport_ImportModule(name)?.let { createProxyObject(it, GCBehavior.ONLY_DEC) } ?: quickAccess.throwAutoError()
    }

    inline fun <reified T: KPythonProxy> import(name: String): T {
        return import(name).asInterface<T>()
    }

    fun setFakeFileDir(dir: String, fileName: String = "fake_file.py", globals: PyDict? = null) {
        // Set path
        val path = engine.PySys_GetObject("path")!! // Borrowed
        val d = engine.PyUnicode_DecodeFSDefault(dir)
        engine.PyList_Insert(path, 0, d)
        engine.Py_DecRef(d)

        // Set __file__
        (globals ?: this.globals)["__file__"] = convertTo(Paths.get(dir, fileName))
    }

    fun setArgv(fileName: String = "fake_file.py", vararg args: String) {
        val allArgs = args.toMutableList().apply { add(0, fileName) }.toTypedArray()
        engine.PySys_SetObject("argv", convertTo(allArgs).obj)
    }

    fun getArgv(): List<String> {
        val argv = engine.PySys_GetObject("argv") ?: quickAccess.throwAutoError()
        val proxy = createProxyObject(argv, GCBehavior.FULL).asInterface<PyList>() // Borrowed, but given to kotlin
        return List(proxy.size().toInt()) {
            proxy[it.toLong()].toString()
        }
    }

    /**
     * Get a sys variable, borrowed, so python refcount is not affected
     */
    inline fun <reified T> getSys(name: String): T {
        return convertFrom(getSys(name), T::class.java) as T
    }

    /**
     * Get a sys variable, borrowed, so python refcount is not affected
     */
    fun getSys(name: String): PythonProxyObject {
        return engine.PySys_GetObject(name)?.let { createProxyObject(it, GCBehavior.FULL) } ?: quickAccess.throwAutoError() // Borrowed, but given to kotlin
    }

    /**
     * Set a sys variable, value is converted unless already PythonProxyObject/KPythonProxy
     */
    fun setSys(name: String, value: Any) {
        val converted = convertTo(value)
        engine.PySys_SetObject(name, converted.obj)
    }

    val quickAccess: QuickAccess = QuickAccess()
    // Mid-level bindings for fast access to functions, dictionaries, lists, and tuples
    inner class QuickAccess {
        // Functions
        fun invoke(o: PythonProxyObject, vararg args: Any?, kwargs: HashMap<String, Any?>? = null): PythonProxyObject {
            val usedArgs = convertArgs(*args) // Not allowed to be empty
            val usedKwargs = kwargs?.let { createDict(it) } // Since this can be null
            return engine.PyObject_Call(o.obj, usedArgs.obj, usedKwargs?.getKPythonProxyBase()?.obj)?.let { createProxyObject(it, GCBehavior.ONLY_DEC) } ?: quickAccess.throwAutoError() // New reference
//            return engine.PyObject_CallObject(o.obj, args2?.obj)?.let { createProxyObject(it) }
        }

        // Dicts
        fun dictGetSize(o: PythonProxyObject): Long {
            return engine.PyDict_Size(o.obj)
        }

        fun dictGetItem(o: PythonProxyObject, key: Any?): PythonProxyObject {
            return engine.PyDict_GetItem(o.obj, convertTo(key).obj)?.let {
                createProxyObject(
                    it, GCBehavior.FULL // Borrowed, but given to kotlin
                )
            } ?: None
        }

        fun dictSetItem(o: PythonProxyObject, key: Any?, value: Any?) {
            engine.PyDict_SetItem(o.obj, convertTo(key).obj, convertTo(value).obj)
        }

        fun dictContainsKey(o: PythonProxyObject, key: Any?): Boolean {
            return engine.PyDict_Contains(o.obj, convertTo(key).obj) == 1
        }

        fun dictGetKeys(o: PythonProxyObject): PyList {
            return engine.PyDict_Keys(o.obj).let { createProxyObject(it, GCBehavior.ONLY_DEC) }.asInterface<PyList>()
        }

        // lists
        fun listGetSize(o: PythonProxyObject): Long {
            return engine.PyList_Size(o.obj)
        }

        fun listGetItem(o: PythonProxyObject, idx: Long): PythonProxyObject {
            return engine.PyList_GetItem(o.obj, idx)?.let { it2 ->
                createProxyObject(it2, GCBehavior.FULL) // Borrowed, but given to kotlin
            } ?: None
        }

        fun listSetItem(o: PythonProxyObject, idx: Long, value: Any?) {
            engine.PyList_SetItem(o.obj, idx, convertTo(value).obj)
        }

        // tuples
        fun tupleGetSize(o: PythonProxyObject): Long {
            return engine.PyTuple_Size(o.obj)
        }

        fun tupleGetItem(o: PythonProxyObject, idx: Long): PythonProxyObject {
            return engine.PyTuple_GetItem(o.obj, idx)?.let {
                createProxyObject(it, GCBehavior.FULL) // Borrowed, but given to kotlin
            } ?: None
        }

        // module
        fun moduleGetDict(o: PythonProxyObject): PythonProxyObject {
            return engine.PyModule_GetDict(o.obj)?.let {
                createProxyObject(it, GCBehavior.FULL) // Borrowed, but given to kotlin
            } ?: None
        }

        fun typeGetDict(o: PythonProxyObject): PythonProxyObject {
            return o["__dict__"]
//            return engine.PyType_GetDict(o.obj)?.let {
//                createProxyObject(it, GCBehavior.ONLY_DEC) // Borrowed, but given to kotlin
//            }
        }

        fun errorOccurred(): PythonProxyObject? {
            return engine.PyErr_GetRaisedException()?.let { createProxyObject(it, GCBehavior.FULL) }
        }

        fun autoError() {
            errorOccurred()?.let { throw PythonException(it.toString()) }
        }

        fun throwAutoError(): Nothing {
            throw PythonException(errorOccurred().toString())
        }
    }
}

fun String.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)
}

fun Boolean.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)
}

fun Number.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)
}

fun Array<*>.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)
}

fun HashMap<*, *>.toPython(env: PyEnvironment): PythonProxyObject {
    return env.convertTo(this)
}