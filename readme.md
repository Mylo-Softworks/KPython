# KPython
Embed python inside of Kotlin/JVM applications.

## How?
By using CPython's api through JNA, this allows us to embed a full CPython interpreter in our project.

## How can I use this?
KPython uses CPython through its library, for python 3.12, this is python312.dll on Windows, and libpython3.12.so on Linux.

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