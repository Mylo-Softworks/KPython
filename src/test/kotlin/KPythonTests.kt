import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.environment.pythonobjects.PyCallable
import com.mylosoftworks.kpython.environment.pythonobjects.PyClass
import com.mylosoftworks.kpython.environment.pythonobjects.PyEnterable
import com.mylosoftworks.kpython.environment.pythonobjects.createTyped
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

        pyClass?.getKPythonProxyBase()?.createMethod("testFunc") {
            return@createMethod "The ${self?.get("__class__")?.get("__name__")} object has a function!"
        }

        val inst = pyClass?.invoke()
        println(inst?.invokeMethod("testFunc").toString())
        assert(inst?.invokeMethod("testFunc").toString() == "The ${pyClass?.__name__} object has a function!")
    }
}