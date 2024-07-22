package com.mylosoftworks.kpython.internal.utils

import com.mylosoftworks.kpython.Either
import java.lang.reflect.Method
import kotlin.reflect.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

internal fun <T: Any> isPropertyAccessor(method: Method, clazz: KClass<T>): Boolean {
    return clazz.memberProperties.any { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun <T: Any> isPropertySetter(method: Method, clazz: KClass<T>): Boolean {
    return clazz.memberProperties.any { if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun <T: Any> getKotlinMember(method: Method, clazz: KClass<T>): Either<KFunction<*>, KProperty<*>>? {
    val prop = clazz.memberProperties.find { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
    if (prop != null) return Either.Right(prop)

    val function = clazz.memberFunctions.find { it.javaMethod == method }
    if (function != null) return Either.Left(function)

    return null
}