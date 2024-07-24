package com.mylosoftworks.kpython.internal.utils

import com.mylosoftworks.kpython.Either
import java.lang.reflect.Method
import kotlin.reflect.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

internal fun isPropertyAccessor(method: Method): Boolean {
    return method.declaringClass.kotlin.memberProperties.any { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun isPropertySetter(method: Method): Boolean {
    return method.declaringClass.kotlin.memberProperties.any { if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
}

internal fun getKotlinMember(method: Method): Either<KFunction<*>, KProperty<*>>? {
    val prop = method.declaringClass.kotlin.memberProperties.find { it.getter.javaMethod == method || if (it is KMutableProperty<*>) it.setter.javaMethod == method else false }
    if (prop != null) return Either.Right(prop)

    val function = method.declaringClass.kotlin.memberFunctions.find { it.javaMethod == method }
    if (function != null) return Either.Left(function)

    return null
}