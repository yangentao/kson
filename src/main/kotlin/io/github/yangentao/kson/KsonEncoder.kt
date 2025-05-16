@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

import io.github.yangentao.anno.userName
import java.net.URI
import java.net.URL
import java.sql.Blob
import java.util.*
import kotlin.reflect.KProperty1

class KsonEncoderConfig(val includeProperties: List<KProperty1<*, *>> = emptyList()) {

}

object KsonEncoder {

    fun encode(m: Any?, config: KsonEncoderConfig?): KsonValue {
        if (m == null) return KsonNull
        if (m is KsonValue) return m

        when (m) {
            is Boolean -> return KsonBool(m)
            is Number -> return KsonNum(m)
            is String -> return KsonString(m)
            is Iterable<*> -> return KsonArray().apply { m.mapTo(this.data) { encode(it, config) } }
            is Map<*, *> -> return KsonObject(m.size).apply { m.remapTo(this.data, { it.toString() }, { encode(it, config) }) }
            is Char -> return KsonString(m.toString())
            is StringBuffer -> return KsonString(m.toString())
            is StringBuilder -> return KsonString(m.toString())
            is java.sql.Date -> return KsonString(formatDate(m.time))
            is java.sql.Time -> return KsonString(formatTime(m.time))
            is java.sql.Timestamp -> return KsonString(formatDateTime(m.time))
            is java.util.Date -> return KsonNum(m.time)
            is Calendar -> return KsonNum(m.timeInMillis)
            is Blob -> return KsonBlob(m)
            is ByteArray -> return KsonBlob(m)
            is CharArray -> return KsonString(String(m))
            is URL -> return KsonString(m.toString())
            is URI -> return KsonString(m.toString())
            is UUID -> return KsonString(m.toString())

            else -> {
                if (m::class.java.isArray) {
                    val length: Int = java.lang.reflect.Array.getLength(m)
                    val ka = KsonArray(length)
                    for (i in 0..<length) {
                        val v = java.lang.reflect.Array.get(m, i)
                        ka.addAny(v)
                    }
                    return ka
                } else {
                    val ls: List<KProperty1<*, *>> = if (config == null) {
                        m::class.propertiesJSON
                    } else {
                        if (config.includeProperties.isEmpty()) {
                            m::class.propertiesJSON
                        } else {
                            m::class.propertiesJSON + config.includeProperties
                        }
                    }
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
}

