package com.mylosoftworks.kpython.internal.engine

import com.sun.jna.IntegerType



// https://docs.python.org/3/c-api/veryhigh.html#c.Py_eval_input
// https://github.com/python/cpython/blob/5716cc352940a5f8557a8191e873837aa619498a/Include/compile.h#L10
internal const val Py_single_input = 256
internal const val Py_file_input = 257
internal const val Py_eval_input = 258
internal const val Py_func_type_input = 345

internal enum class StartSymbol(val value: Int) {
    Single(Py_single_input),
    File(Py_file_input),
    Eval(Py_eval_input);
}

internal const val METH_VARARGS = 0x0001
internal const val METH_KEYWORDS = 0x0002

//internal const val Py_CONSTANT_NONE             = 0
//internal const val Py_CONSTANT_FALSE            = 1
//internal const val Py_CONSTANT_TRUE             = 2
//internal const val Py_CONSTANT_ELLIPSIS         = 3
//internal const val Py_CONSTANT_NOT_IMPLEMENTED  = 4
//internal const val Py_CONSTANT_ZERO             = 5
//internal const val Py_CONSTANT_ONE              = 6
//internal const val Py_CONSTANT_EMPTY_STR        = 7
//internal const val Py_CONSTANT_EMPTY_BYTES      = 8
//internal const val Py_CONSTANT_EMPTY_TUPLE      = 9
//
//enum class Constants(val value: Int) {
//    None(Py_CONSTANT_NONE),
//    False(Py_CONSTANT_FALSE),
//    True(Py_CONSTANT_TRUE),
//    Ellipsis(Py_CONSTANT_ELLIPSIS),
//    NotImplemented(Py_CONSTANT_NOT_IMPLEMENTED),
//    Zero(Py_CONSTANT_ZERO),
//    One(Py_CONSTANT_ONE),
//    EmptyStr(Py_CONSTANT_EMPTY_STR),
//    EmptyBytes(Py_CONSTANT_EMPTY_BYTES),
//    EmptyTuple(Py_CONSTANT_EMPTY_TUPLE);
//}