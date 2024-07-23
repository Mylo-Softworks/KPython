package com.mylosoftworks.kpython.internal.utils

import com.mylosoftworks.kpython.Either
import java.lang.reflect.Method
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaMethod

internal fun <T: Any> isMember(method: Method, clazz: KClass<T>): Boolean {
    return getKotlinMember(method, clazz) != null
}

internal fun <T: Any> isPropertyAccessor(method: Method, clazz: KClass<T>): Boolean {
    return clazz.declaredMemberProperties.any { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun <T: Any> isPropertySetter(method: Method, clazz: KClass<T>): Boolean {
    return clazz.declaredMemberProperties.any { if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun <T: Any> getKotlinMember(method: Method, clazz: KClass<T>): Either<KFunction<*>, KProperty<*>>? {
    val prop = clazz.declaredMemberProperties.find { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
    if (prop != null) return Either.Right(prop)

    val function = clazz.declaredMemberFunctions.find { it.javaMethod == method }
    if (function != null) return Either.Left(function)

    return null
}