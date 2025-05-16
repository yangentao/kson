package entao

import io.github.yangentao.kson.*
import kotlin.test.Test

class Per(
    var name: String,
    var age: Int,
    var addr: String? = null,
    val hello: String = "hello"
)

class TableTest {
    @Test
    fun build() {
        val ls = (0..<3).map { Kson.toKson(Per("entao $it", 40 + it, if (it % 3 == 0) null else "addr $it "), KsonEncoderConfig(listOf(Per::hello))) as KsonObject }
//        println(ls)
        val table = JsonTable.fromRows(ls)
        println(table)

        for (a in table) {
            println(a.json())
        }
    }

    @Test
    fun build2() {
        val ls = (0..<3).map { Kson.toKson(Per("entao $it", 40 + it, if (it % 3 == 0) null else "addr $it "), KsonEncoderConfig(listOf(Per::hello))) as KsonObject }
        val r = JsonResult.success().dataTable(ls)
        println(r)

    }
}