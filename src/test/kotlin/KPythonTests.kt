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

        val pyClass = env.globals["Test"].asInterface<PyClass>()
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

        val result = reverseFunction.invoke(test)

        assert(test.reversed() == result.toString())
    }

    @Test
    fun addKotlinFunction() {
        val env = PyEnvironment(PythonVersion.python312)

        val compString = "Abracadabra"

        env.globals.createMethod("get_kotlin_string") {
            compString
        }

        val returnValue = env.globals.invokeMethod("get_kotlin_string").toJvmRepresentation<String>()
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

        val A = env.globals["A"].asInterface<PyClass>()
        val inst = A()!!.asInterface<PyEnterable>()

        inst.with {
            assert(given.toString() == "Example!")
        }
    }

    @Test
    fun testModuleCreation() {
        val env = PyEnvironment(PythonVersion.python312)

        val testModule = env.createModule("testmodule", true)

        testModule.createFunction("testFunc") {
            return@createFunction "Compare me!"
        }

        testModule.createClass("TestClass") {
            self["name"] = env.convertTo("Test")
        }

        env.file("""
            import testmodule
            
            result = testmodule.testFunc()
            result_name = testmodule.TestClass().name
        """.trimIndent())

        assert(env.globals["result"].toString() == "Compare me!")
        assert(env.globals["result_name"].toString() == "Test")
    }

    @Test
    fun testCreateClass() {
        val env = PyEnvironment(PythonVersion.python312)

        val pyClass = env.createClass("A_Class") {

            self.createMethod("whoami") {
                return@createMethod self.asInterface<KPythonProxy>().__class__.__name__
            }
        }

        val subClass = env.createClass("Another_Class", pyClass) {

        }

        val inst = pyClass()
        val subInst = subClass()
        val result = inst.invokeMethod("whoami")
        assert(result.toString() == "A_Class")
    }

    @Test
    fun testKwargs() {
        val env = PyEnvironment(PythonVersion.python312)

        env.file("""
            class A:
                def test(self, **kwargs):
                    return kwargs
        """.trimIndent())
        val pyClass = env.globals["A"].asInterface<PyClass>()
        val inst = pyClass()

        val kwargs = hashMapOf<String, Any?>("test1" to "Value!")

        val result = inst.invokeMethod("test", kwargs = kwargs)
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

        val result = pyFun.invoke(kwargs = kwargs)?.asInterface<PyDict>()
        assert(result?.get("test")?.toString() == testString)
    }

    @Test
    fun testParseArguments() {
        val env = PyEnvironment(PythonVersion.python312)

        val functionCallParams = PyEnvironment.FunctionCallParams(env.None, env.createTuple("first", "second"), env.convertToI<PyDict>(
            hashMapOf<String, Any?>("name" to "one", "another" to 2)
        ), env)

        val parsed = functionCallParams.parseArgumentsByDefinition("arg1, *args, name, **kwargs")

        assert(parsed["arg1"].toString() == "first")
        assert(parsed["args"]!!.asInterface<PyList>()[0].toString() == "second") // First item in args should be "second"
        assert(parsed["name"].toString() == "one")
        assert(parsed["kwargs"]!!.asInterface<PyDict>()["another"].toString() == "2")
    }
}