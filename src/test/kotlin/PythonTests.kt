import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.internal.engine.PythonEngineInterface
import com.mylosoftworks.kpython.internal.engine.StartSymbol
import com.mylosoftworks.kpython.internal.engine.initialize
import org.junit.jupiter.api.Test

// Tests which run python code

class PythonTests {
    @Test
    fun confirmValues() {
        val pythonLib = PythonEngineInterface.initialize(PythonVersion.python312)
        pythonLib.Py_Initialize()

        val kotlinString = "This is a string which will be checked"
        val pythonString = pythonLib.Py_BuildValue("s", kotlinString)
        val reobtainedString = pythonLib.PyUnicode_AsUTF8(pythonString!!)

        pythonLib.Py_Finalize()

        assert(reobtainedString == kotlinString)
    }

    @Test
    fun functionCallTest() {
        val pythonLib = PythonEngineInterface.initialize(PythonVersion.python312)
        pythonLib.Py_Initialize()

        val globals = pythonLib.PyDict_New()
        val locals = pythonLib.PyDict_New()

        val functionDef = """
            class Test:
                def __call__(self):
                    return "Hello, world!"
               
                def another_function(self):
                    return "Called another_function!"
                    
            def function_test():
                return "Function test was called!"
        """.trimIndent()

        pythonLib.PyRun_String(functionDef, StartSymbol.File.value, globals, locals)

        val testClass = pythonLib.PyDict_GetItemString(locals, "Test") // Get the test class
        val testFunction = pythonLib.PyDict_GetItemString(locals, "function_test") // Get the test class


        val instance = pythonLib.PyObject_CallObject(testClass!!, null) // Create Test object

        val result2 = pythonLib.PyObject_CallObject(instance!!, null) // instance()
        assert(pythonLib.PyUnicode_AsUTF8(result2!!) == "Hello, world!")

        val anotherFunction = pythonLib.PyObject_GetAttrString(instance, "another_function") // instance.another_function
        val result3 = pythonLib.PyObject_CallObject(anotherFunction!!, null) // instance.another_function()
        assert(pythonLib.PyUnicode_AsUTF8(result3!!) == "Called another_function!")

        val result4 = pythonLib.PyObject_CallObject(testFunction!!, null)
        assert(pythonLib.PyUnicode_AsUTF8(result4!!) == "Function test was called!")

        pythonLib.Py_Finalize()
    }
}