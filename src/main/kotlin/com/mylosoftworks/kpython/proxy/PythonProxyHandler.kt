package com.mylosoftworks.kpython.proxy

import com.mylosoftworks.kpython.internal.utils.getKotlinMember
import com.mylosoftworks.kpython.internal.utils.isPropertyAccessor
import com.mylosoftworks.kpython.internal.utils.isPropertySetter
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class PythonProxyHandler internal constructor(val obj: PythonProxyObject) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        if (isPropertyAccessor(method, proxy.javaClass.kotlin)) {
            if (isPropertySetter(method, proxy.javaClass.kotlin)) {
                // Setter, set value
                val setterName = getKotlinMember(method, proxy.javaClass.kotlin)!!.right!!.name
                obj[setterName] = args[0]
                return null
            }
            else {
                // Getter, get value
                val getterName = getKotlinMember(method, proxy.javaClass.kotlin)!!.right!!.name
                return obj[getterName]
            }
        }
        else {
            // Method, invoke
            val methodName = getKotlinMember(method, proxy.javaClass.kotlin)!!.left!!.name
            return obj.invokeMethod(methodName, *args)
        }
    }
}