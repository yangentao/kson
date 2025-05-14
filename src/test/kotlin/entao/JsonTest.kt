package entao

import io.github.yangentao.kson.JsonParser
import io.github.yangentao.kson.Kson
import io.github.yangentao.kson.KsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonTest {

    @Test
    fun jsonNormal() {
        val text = """
        {
        "name":"ent\u0023ao",
        }
    """.trimIndent()
        val v = JsonParser(text).parse() as KsonObject
        assertEquals("ent#ao", v.getString("name"))
        println(v)
    }

    @Test
    fun loose() {
        val text = """
        {
        "name":"entao"
        age: -9.8e5
        male:true
        so:[1,2,3]
        }
    """.trimIndent()
        val v = Kson.parse(text, true) as KsonObject
        println(v)
        assertEquals(true, v.getBool("male"))
        assertEquals("entao", v.getString("name"))
    }
}