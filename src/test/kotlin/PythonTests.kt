import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.internal.engine.PythonEngineInterface
import com.mylosoftworks.kpython.internal.engine.StartSymbol
import com.mylosoftworks.kpython.internal.engine.initialize
import com.mylosoftworks.kpython.proxy.PythonProxyObject
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import org.junit.jupiter.api.Test

// Tests which run python code

class PythonTests {
    @Test
    fun pythonLibBasicTest() {
        val pythonLib = PythonEngineInterface.initialize(PythonVersion.python310)

        pythonLib.Py_Initialize()

        val globals = pythonLib.PyDict_New()
        val locals = pythonLib.PyDict_New()

//        val code = """
//
//            "This is a test"
//
//        """.trimIndent()
//
//        val result = pythonLib.PyRun_String(code, StartSymbol.Eval.value, globals, locals)

        val result = pythonLib.Py_BuildValue("s", "This is a test")
        println(pythonLib.PyUnicode_AsUTF8(result))

//        pythonLib.PyDict_SetItemString(globals, "message", result) // message = result
//        pythonLib.PyRun_String("print(message)", StartSymbol.File.value, globals, locals)

//        val result = pythonLib.PyRun_SimpleString("""
//
//            import sys
//            print(sys.version)
//
//            """.trimIndent())


        pythonLib.Py_Finalize()
    }

    @Test
    fun confirmValues() {
        val pythonLib = PythonEngineInterface.initialize(PythonVersion.python312)

        pythonLib.Py_Initialize()

        val kotlinString = "This is a string which will be checked"
        val pythonString = pythonLib.Py_BuildValue("s", kotlinString)
        val reobtainedString = pythonLib.PyUnicode_AsUTF8(pythonString)

        pythonLib.Py_Finalize()

        assert(reobtainedString == kotlinString)
    }
}