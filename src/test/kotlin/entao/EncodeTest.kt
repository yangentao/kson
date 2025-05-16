package entao

import io.github.yangentao.kson.KsonArray
import io.github.yangentao.kson.KsonNull
import kotlin.test.Test

class EncodeTest {
    @Test
    fun encode() {
        val ja = KsonArray()
        ja.addAny(1)
        ja.addAny("s")
        ja.addAny(arrayOf("a", "b"))
        ja.addAny(KsonNull)
        println(ja.json())
    }
}