package com.mylosoftworks.kpython.internal.engine.pythondefs

import com.sun.jna.Pointer
import com.sun.jna.Structure

typealias PyObject = Pointer

//internal open class PyObject : Structure() {
//    class ByReference : PyObject(), Structure.ByReference
////    class ByValue : PyObject(), Structure.ByValue
//}