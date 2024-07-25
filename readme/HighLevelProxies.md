# High-level proxies
High-level proxies are interfaces which extend the `KPythonProxy` interface. They are exclusively used through `PythonProxyObject.asInterface<T>()`.  
High-level proxies do not make modifications to the underlying python objects, they simply change what it's **viewed like**.

## Get the Mid-level proxy object
To get the associated `PythonProxyObject`, call `getKPythonProxyBase()`.

## Predefined High-level proxies
Some high-level proxies are predefined, you can inherit or directly use these.
* PyCallable -> An object which can be called (A function, method, class, or instance of a class with `__call__` defined)
* PyClass: PyCallable -> Specifically a callable representing a class
* PyEnterable -> An object which has `__enter__` and `__exit__`, can be called from kotlin using `.with(*args) {}`, you have access to `self` and `given`, `given` is the return type from the `__enter__`.
* PyDict -> A python dictionary
* PyList -> A python list
* PyTuple -> A python tuple

## Accessing python attributes
Python attributes can be declared by simply creating fields. If an attribute is always a string, simply declare it as a string.  
Attributes declared as `PythonProxyObject` will be returned as-is, and attributes declared as `Any` will have identical `get` behavior, but allow automatic conversion on set.  

Example:
```kotlin
interface AttributesExample : KPythonProxy {
    var example_object: PythonProxyObject // Raw Mid-level python proxy object
    var example_string: String // Automatically translated
    var example_callable: PyCallable // Automatically proxied with .asInterface<PyCallable>(), also allows setting. You should probably use a function here though.
}
```

## Accessing python functions
Python functions can be declared by simply declaring the function signature in Kotlin, the python version of the function will be invoked.

Example:
```kotlin
interface FunctionsExample : KPythonProxy {
    fun do_stuff() // Void return function with no parameters
    fun do_this(thing: String) // Void return function with automatically translated parameters
    
    fun get_thing(thing: PythonProxyObject): PythonProxyObject? // Function which takes one python object, and returns one python object (Not translated)
    // OR
    fun get_thing(thing: Any): PythonProxyObject // Function which takes one python object, and returns one python object (Automatically translated)
    
    fun get_callable(name: String): PyCallable // Function which takes one string, and returns one python object, viewed as PyCallable
}
```

## Custom behavior annotations
Some kotlin annotations exist to enforce custom behavior instead of pass-through.

### DontUsePython
Instead of passing through the function call, run a kotlin function instead.

```kotlin
interface DontUsePythonExample : KPythonProxy {
    @DontUsePython
    fun example() // Function with no params or return value
    
    @DontUsePython
    fun paramExample(text: String) // Function with string param
    
    @DontUsePython
    fun returnValueExample(): String // Function with string return value
    
    companion object { // Function implementations must be stored in companion with same name and signature (and PythonProxyObject as first argument)
        fun example(self: PythonProxyObject) {
            TODO()
        }
        
        fun paramExample(self: PythonProxyObject, text: String) {
            TODO()
        }
        
        fun returnValueExample(self: PythonProxyObject): String {
            TODO()
        }
    }
}
```

### PyFun
Instead of passing through the function call, run a python function instead.

PyFun has 2 parameters
* code: String | required
  * The code to execute, the code must be a python function definition for a function named "fun". Indents are trimmed before execution.
* firstArgIsSelf: Boolean | default=false
  * Indicates that the python function should be called with the python object representation for this object as the first argument. Like a python method.

Example:
```kotlin
interface PyFunExample : KPythonProxy {
    @PyFun("""
        def fun():
            return "This is an example!"
    """)
    fun exampleFunction(): String // No arguments, string return value
    
    @PyFun("""
        def fun(name):
            return "Hello, {name}!"
    """)
    fun greeting(name: String): String // One string argument, string return value
    
    @PyFun("""
        def fun():
            print("Hello, World!")
    """)
    fun printHelloWorld() // No arguments, no return value
    
    @PyFun("""
        def fun(callable):
            return callable()
    """)
    fun callCallable(callable: PyCallable): PythonProxyObject // One PyCallable python object argument, python object return value
}
```

### ~~GetBaseProxy~~ (Not intended to be used manually)
GetBaseProxy tags a function with a `PythonProxyObject` return type, it's used to get the `PythonProxyObject` that a proxy is based on.  
**Not intended for use:** The `KPythonProxy` base class includes a `getKPythonProxyBase()` function, so additional usage of the attribute is pointless.