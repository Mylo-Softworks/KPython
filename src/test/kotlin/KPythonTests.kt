import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.environment.pythonobjects.*
import com.mylosoftworks.kpython.proxy.KPythonProxy
import org.junit.jupiter.api.Test

// Tests which use KPython at a user-level
class KPythonTests {
    @Test
    fun createEnv() {
        val env = PyEnvironment(PythonVersion.python312)

        // Init some env vars, which will run some code, ensuring the env is working properly
        env.None
        env.True
        env.False

        env.finalize()
    }

    @Test
    fun runEval() {
        val env = PyEnvironment(PythonVersion.python312)

        val result = env.eval("'Hello, World!' * 2")
        assert(result.toString() == "Hello, World!".repeat(2))
    }

    @Test
    fun callObject() {
        val env = PyEnvironment(PythonVersion.python312)

        env.file("""
            class Test:
                def __call__(self):
                    return "This is an example object with return!"
        """.trimIndent())

        val pyClass = env.globals["Test"]!!.asInterface<PyClass>()
//        val inst = pyClass()!!.asInterface<PyCallable>()
        val inst = pyClass.createTyped<PyCallable>()!!
        val result = inst()
        assert(result.toString() == "This is an example object with return!")
        assert(pyClass.__name__ == "Test")
    }

    @Test
    fun testCallbacks() {
        val env = PyEnvironment(PythonVersion.python312)

        val test = "This is a test!"

        val reverseFunction = env.createFunction {
            val (arg1) = args
            return@createFunction arg1.toString().reversed()
        }

        val result = reverseFunction?.invoke(test)

        assert(test.reversed() == result.toString())
    }

    @Test
    fun addKotlinFunction() {
        val env = PyEnvironment(PythonVersion.python312)

        val compString = "Abracadabra"

        env.globals.createMethod("get_kotlin_string") {
            compString
        }

        val returnValue = env.globals.invokeMethod("get_kotlin_string")!!.toJvmRepresentation<String>()
        assert(compString == returnValue)
    }

    @Test
    fun testEnterExit() {
        val env = PyEnvironment(PythonVersion.python312)

        env.file("""
            class A:
                def __enter__(self):
                    return "Example!"

                def __exit__(self, exc_type, exc_val, exc_tb):
                    pass
        """.trimIndent())

        val A = env.globals["A"]!!.asInterface<PyClass>()
        val inst = A()!!.asInterface<PyEnterable>()

        inst.with {
            assert(given.toString() == "Example!")
        }
    }

    @Test
    fun testModuleCreation() {
        val env = PyEnvironment(PythonVersion.python312)

        val testModule = env.createModule("testmodule", true)

        testModule.getDict()?.createMethod("testFunc") {
            return@createMethod "Compare me!"
        }

        env.file("""
            import testmodule
            
            result = testmodule.testFunc()
        """.trimIndent())

        assert(env.globals["result"].toString() == "Compare me!")
    }

    @Test
    fun testCreateClass() {
        val env = PyEnvironment(PythonVersion.python312)

        val pyClass = env.createClass("A")

//        pyClass?.getKPythonProxyBase()?.createMethod("testFunc") {
//            return@createMethod args
////            return@createMethod "Confirm this string!"
//        }

        pyClass?.getDict()?.createMethod("testFunc") {
            return@createMethod args
        }

        val inst = pyClass?.invoke()

        inst?.set("test", env.convertTo("Confirm me!"))

        println(inst?.invokeMethod("testFunc").toString())
        assert(inst?.invokeMethod("testFunc").toString() == "The ${pyClass?.__name__} object has a function!")
    }

    @Test
    fun testKwargs() {
        val env = PyEnvironment(PythonVersion.python312)

        env.file("""
            class A:
                def test(self, **kwargs):
                    return kwargs
        """.trimIndent())
        val pyClass = env.globals["A"]!!.asInterface<PyClass>()
        val inst = pyClass()

        val kwargs = hashMapOf<String, Any?>("test1" to "Value!")

        val result = inst?.invokeMethod("test", kwargs = kwargs)
        val convertedResult = env.convertFrom(result, HashMap::class.java)

        assert(convertedResult.toString() == kwargs.toString()) // Using toString since hashmaps don't compare well here, since the content is just strings this won't be a problem
    }

    @Test
    fun testKwargsFromKotlinFun() {
        val env = PyEnvironment(PythonVersion.python312)

        val pyFun = env.createFunction {
            return@createFunction kwargs // pass-through for kwargs
        }

        val testString = "Confirm me!"

        val kwargs = hashMapOf<String, Any?>("test" to testString)

        val result = pyFun?.invoke(kwargs = kwargs)?.asInterface<PyDict>()
        assert(result?.get("test")?.toString() == testString)
    }
}