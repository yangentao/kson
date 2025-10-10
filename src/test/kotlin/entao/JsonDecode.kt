package entao

import io.github.yangentao.kson.KsonDecoder
import io.github.yangentao.kson.TypeTaker
import io.github.yangentao.kson.ksonArray
import java.util.*
import kotlin.test.Test

class JsonDecodeTest {
    @Test
    fun a() {
        val p = ::testList.parameters.first()
        val ka = ksonArray("a", "b", "c")
        val v = KsonDecoder.decode(p, ka) ?: return
        println(v::class)
        println(v)
        val vv = KsonDecoder.decode(object : TypeTaker<ArrayList<String>>() {}, ka)
        println(vv)
    }
}

private fun testList(ls: List<String>) {

}


