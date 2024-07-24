import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.PyEnvironment
import com.mylosoftworks.kpython.environment.pythonobjects.PyCallable
import com.mylosoftworks.kpython.environment.pythonobjects.PyClass
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
}