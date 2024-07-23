import com.mylosoftworks.kpython.PythonVersion
import com.mylosoftworks.kpython.environment.PyEnvironment
import org.junit.jupiter.api.Test

// Tests which use KPython at a user-level
class KPythonTests {
    @Test
    fun createEnv() {
        val env = PyEnvironment(PythonVersion.python312)



        env.finalize()
    }
}