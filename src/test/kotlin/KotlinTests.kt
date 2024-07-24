import com.mylosoftworks.kpython.internal.utils.isPropertyAccessor
import org.junit.jupiter.api.Test

// Tests on the kotlin size (no python is called)

class KotlinTests {
    @Test
    fun testPropertyDetection() {
        for (f in PropertyAndFunctionsTestObject::class.java.declaredMethods) {
            if (f.name.contains("Function") || f.name.contains("Property")){
                assert(f.name.contains("Property") == isPropertyAccessor(f)) { f.name }
            }
        }
    }
}

interface PropertyAndFunctionsTestObject {
    fun basicFunction()
    fun basicFunction2(): Boolean
    fun basicFunction3(i: Int)
    fun basicFunction4(b: Boolean, s: String): Int

    val basicProperty: Int
    var basicProperty2: Boolean
}