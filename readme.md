# KPython
Embed [cpython](https://github.com/python/cpython) inside of [Kotlin/JVM](https://github.com/JetBrains/kotlin) applications, using [JNA](https://github.com/java-native-access/jna).
> KPython is an early project. Expect bugs.  
> Bugs should be reported in the [issues](https://github.com/Mylo-Softworks/KPython/issues) section of this project.

## How?
By using CPython's api through JNA, this allows us to embed a full CPython interpreter in our Kotlin/JVM projects.

## How can I use this?
KPython uses CPython through its library, for python 3.12, this is python312.dll on Windows, and libpython3.12.so on Linux.

## Summary of features
* Run Python code from Kotlin
* Give Kotlin code to Python as a function
* Convert Python types to Kotlin
* Convert Kotlin types to Python
* Add a Kotlin "Frontend" to a python backend, for use with python libraries such as pytorch, tensorflow, numpy, etc.
* Proxied access to python objects using `PythonProxyObject` instances.
* Proxied type-safe access to python objects using interfaces extending `KPythonProxy`.

## Features

### Mid-level proxy
Python objects are represented as `PythonProxyObject`, attributes can be get and set with Kotlin's indexing. Functions/methods can be called using `PythonProxyObject.invokeMethod(name, args)`  
Mid-level proxies can be converted to specific High-level proxies.

### High-level proxies
High-level proxies are Java Proxy interfaces which extend `KPythonProxy`, which will be automatically implemented at runtime.  
High-level proxies can access the Mid-level proxy they're based on.  
Return and input types are automatically converted if possible.  
[see more](readme/HighLevelProxies.md)

### Automatic conversions
Some types can be converted automatically between Kotlin and Python, these types include:
* Byte, Short, Int, Long (Represented as long in python)
* Float, Double (Represented as double in python)
* String
* Array -> list (Exclusively Array, not List or ArrayList, as those are type-erased)
* (To python only) HashMap -> dict
* PythonProxyObject -> object (No conversion occurs, as PythonProxyObject holds the real representation of the proxy)
* KPythonProxy -> object (Converted to PythonProxyObject, then use the value)

### Kotlin functions
Kotlin functions are used to execute kotlin code from python, no automatic conversion exists, but there are many ways to create them.  
[see more](readme/KotlinFunctions.md)

### Compatibility with file scripts
A few methods exist to support relative imports from python. Getting and setting argv is also supported.  
`PyEnvironment.setFakeFileDir()` is used to set the directory where imports come from. Optionally, a `__file__` can also be set for the PyEnvironment.  
`PyEnvironment.setArgv()` is used to set argv, as if the script was ran from a file with arguments. `setArgv` takes a string for the filename, and then `varargs String` for the arguments.

### Environment
Creating an environment is done like `val env = PyEnvironment(PythonVersion.python312)`. More documentation pending.