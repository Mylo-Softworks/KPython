package com.mylosoftworks.kpython

internal enum class Side {
    Left, Right
}

abstract class Either<L, R> private constructor(private var backing: Any, internal val side: Side) {
    class Left<L: Any, R: Any>(value: L) : Either<L, R>(value, Side.Left)
    class Right<L: Any, R: Any>(value: R) : Either<L, R>(value, Side.Right)

    val left: L? get() = if (side == Side.Left) backing as L else null
    val right: R? get() = if (side == Side.Right) backing as R else null
}