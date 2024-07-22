package com.mylosoftworks.kpython.internal.engine.pythondefs

import com.sun.jna.Pointer
import com.sun.jna.Structure

typealias PyTypeObject = Pointer

//internal open class PyTypeObject : Structure() {
//    class ByReference : PyTypeObject(), Structure.ByReference
////    class ByValue : PyTypeObject(), Structure.ByValue
//}