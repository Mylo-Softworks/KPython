package com.mylosoftworks.kpython.internal.engine.pythondefs

import com.sun.jna.Pointer
import com.sun.jna.Structure

typealias PyVarObject = Pointer

//internal open class PyVarObject : PyObject() {
//    class ByReference : PyVarObject(), Structure.ByReference
////    class ByValue : PyVarObject(), Structure.ByValue
//}