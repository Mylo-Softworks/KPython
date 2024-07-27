package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.environment.pythonobjects.PyCallable
import com.mylosoftworks.kpython.internal.utils.getKotlinMember
import com.mylosoftworks.kpython.internal.utils.isPropertyAccessor
import com.mylosoftworks.kpython.internal.utils.isPropertySetter
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.jvm.javaMethod

class PythonProxyHandler internal constructor(val obj: PythonProxyObject) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        val argsSafe = args ?: arrayOf()

        if (proxy !is KPythonProxy) {
            return null
        }

        if (method.isAnnotationPresent(GetBaseProxy::class.java)) {
            return obj
        }

        if (method.isAnnotationPresent(PyFun::class.java)) {
            val annotation = (method.getAnnotation(PyFun::class.java) as PyFun)

            val globals = obj.env.globals.copy()

            obj.env.file(annotation.code.trimIndent(), globals = globals)
            val actualArgs = if (annotation.firstArgIsSelf) argsSafe.toMutableList().apply { add(0, obj) }.toTypedArray() else argsSafe
//            return globals.getKPythonProxyBase().invokeMethod("fun", *argsSafe)!!.let { obj.env.convertFrom(it, method.returnType) }
            return globals["fun"]!!.asInterface<PyCallable>().invoke(*actualArgs)?.let { obj.env.convertFrom(it, method.returnType) } ?: obj.env.None
        }

        if (method.isAnnotationPresent(DontUsePython::class.java)) {
            val name = method.name
            val params = method.parameters.map { it.type }.toMutableList().apply { add(0, PythonProxyObject::class.java) }
            val argsForFuncCall = argsSafe.toMutableList().apply { add(0, obj) }

//            val companion = proxy::class.java.getDeclaredField("Companion").get(null)
//            val companion = proxy::class.companionObjectInstance

            val companion = method.declaringClass.kotlin.companionObjectInstance
            assert(companion != null) { "No companion was found for function with DontUsePython for ${proxy::class.simpleName}.$method" }
            val companionMethod = companion!!.javaClass.getDeclaredMethod(name, *params.toTypedArray())
            assert(companionMethod.returnType == method.returnType || companionMethod.returnType == PythonProxyObject::class.java) { "Companion method for ${proxy::class.simpleName}.$method has wrong return type" }
            if (companionMethod.returnType == PythonProxyObject::class.java) {
                val result = (companionMethod.invoke(companion, *argsForFuncCall.toTypedArray()) as PythonProxyObject?) ?: obj.env.None
                return obj.env.convertFrom(result, method.returnType)
            }
            return companionMethod.invoke(companion, *argsForFuncCall.toTypedArray())
        }

        if (isPropertyAccessor(method)) {
            if (isPropertySetter(method)) {
                // Setter, set value
                val setterName = getKotlinMember(method)!!.right!!.name
                obj[setterName] = obj.env.convertTo(argsSafe[0]) ?: obj.env.None
                return null
            }
            else {
                // Getter, get value
                val getter = getKotlinMember(method)!!.right!!
                val getterName = getter.name
                return obj.env.convertFrom(obj[getterName] ?: obj.env.None, getter.getter.javaMethod!!.returnType)
            }
        }
        else {
            // Method, invoke
            val methodName = getKotlinMember(method)!!.left!!.name
            if (methodName == "toString") return obj.toString() // Failsafe
            return obj.invokeMethod(methodName, *argsSafe)?.let { obj.env.convertFrom(it, method.returnType) } // TODO: Kwarg support through annotation @Kwarg(kwargIndex)
        }
    }
}