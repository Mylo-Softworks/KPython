# Kotlin functions
Kotlin functions are functions available to Python which will run Kotlin code when invoked.

## Creating a function and getting the `PyCallable`
```kotlin
fun main() {
    val env = PyEnvironment(PythonVersion.python312) // Create a python 3.12 environment
    
    val func = env.createFunctionUnit { 
        println("func was called!")
    }!!
    
    val func2 = env.createFunction {
        return@createFunction "Hello from kotlin!"
    }!!
    
    val obj = env.Str.asInterface<PyClass>().invoke() // Create a string by calling the constructor
    
    val func3 = env.createFunctionUnit(obj, "name") { // Set the "self" for the function to obj
        println(self)
    }
    
    val func4 = env.createFunctionUnit {
        val firstArg = args[0] // Get first argument, of type PythonProxyObject
        println(firstArg)
    }
}
```

## Adding a global function
```kotlin
fun main() {
    val env = PyEnvironment(PythonVersion.python312)
    
    env.globals.createFunctionUnit("example_func") {
        println("example_func was called")
    }
}
```

## Adding a function to an object
```kotlin
fun main() {
    val env = PyEnvironment(PythonVersion.python312)

    val obj = env.Str.asInterface<PyClass>().invoke() // Create a string by calling the constructor
    
    obj.createMethodUnit("example_method") {
        println("example_method was called")
    }
}
```