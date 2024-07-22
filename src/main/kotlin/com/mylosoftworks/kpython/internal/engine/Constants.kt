package com.mylosoftworks.kpython.internal.engine

// https://docs.python.org/3/c-api/veryhigh.html#c.Py_eval_input
// https://github.com/python/cpython/blob/5716cc352940a5f8557a8191e873837aa619498a/Include/compile.h#L10
const val Py_single_input = 256
const val Py_file_input = 257
const val Py_eval_input = 258
const val Py_func_type_input = 345

enum class StartSymbol(val value: Int) {
    Single(Py_single_input),
    File(Py_file_input),
    Eval(Py_eval_input);
}
