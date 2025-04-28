@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

import io.github.yangentao.anno.userName
import java.net.URI
import java.net.URL
import java.sql.Blob
import java.util.*

class KsonEncoderConfig {

}

object KsonEncoder {

    fun encode(m: Any?, config: KsonEncoderConfig?): KsonValue {
        if (m == null) return KsonNull.inst
        if (m is KsonValue) return m
        when (m) {
            is Boolean -> return KsonBool(m)
            is Number -> return KsonNum(m)
            is Char -> return KsonString(m.toString())
            is String -> return KsonString(m)
            is StringBuffer -> return KsonString(m.toString())
            is StringBuilder -> return KsonString(m.toString())
            is java.sql.Date -> return KsonString(formatDate(m.time))
            is java.sql.Time -> return KsonString(formatTime(m.time))
            is java.sql.Timestamp -> return KsonString(formatDateTime(m.time))
            is java.util.Date -> return KsonNum(m.time)
            is Calendar -> return KsonNum(m.timeInMillis)
            is Blob -> return KsonBlob(m)
            is ByteArray -> return KsonBlob(m)
            is BooleanArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonBool(it) } }
            is ShortArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonNum(it) } }
            is IntArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonNum(it) } }
            is LongArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonNum(it) } }
            is FloatArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonNum(it) } }
            is DoubleArray -> return KsonArray(m.size).apply { m.mapTo(this.data) { KsonNum(it) } }
            is CharArray -> return KsonString(String(m))
            is URL -> return KsonString(m.toString())
            is URI -> return KsonString(m.toString())
            is UUID -> return KsonString(m.toString())
            is Array<*> -> return KsonArray().apply { m.mapTo(this.data) { encode(it, config) } }
            is Map<*, *> -> return KsonObject(m.size).apply { m.remapTo(this.data, { it.toString() }, { encode(it, config) }) }
            is Iterable<*> -> return KsonArray().apply { m.mapTo(this.data) { encode(it, config) } }

            else -> {
                val ls = m::class.propertiesJSON
                val yo = KsonObject(ls.size)
                ls.forEach { p ->
                    val k = p.userName
                    val v = p.getter.call(m)
                    yo.data[k] = encode(v, config)
                }
                return yo
            }
        }
    }
}

internal fun <K, V, K2, V2> Map<K, V>.remap(keyBlock: (K) -> K2, valueBlock: (V) -> V2): LinkedHashMap<K2, V2> {
    val m = LinkedHashMap<K2, V2>(this.size + this.size / 2)
    for (e in this) {
        m[keyBlock(e.key)] = valueBlock(e.value)
    }
    return m
}

internal fun <K, V, K2, V2> Map<K, V>.remapTo(newMap: MutableMap<K2, V2>, keyBlock: (K) -> K2, valueBlock: (V) -> V2) {
    for (e in this) {
        newMap[keyBlock(e.key)] = valueBlock(e.value)
    }
}