package kkk.com.mhwjewelry

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

fun MainActivity.loadData() : List<JewelriesRecord>{
    val result : ArrayList<JewelriesRecord> = arrayListOf();
    result.add(JewelriesRecord(0,"a","b","c"))
    result.add(JewelriesRecord(1,"a","b","c"))
    result.add(JewelriesRecord(2,"a","b","c"))
    result.add(JewelriesRecord(3,"a","b","c"))
    return result;
}
/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("kkk.com.mhwjewelry", appContext.packageName)
        var intent = Intent(appContext,MainActivity::class.java);
        appContext.startActivity(intent);
//        assertEquals(123,100+23+1)
    }
}
