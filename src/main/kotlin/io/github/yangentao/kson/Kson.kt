@file:Suppress("unused")

package io.github.yangentao.kson

import io.github.yangentao.xlog.TagLog
import kotlin.reflect.KClass
import kotlin.reflect.KType

internal val jsonLog = TagLog("kson")

/**
 * Kson , encode and decode
 */
object Kson {
    fun parse(text: String, loose: Boolean = false): KsonValue? {
        try {
            return if (loose) {
                LooeseJsonParser(text).parse()
            } else {
                JsonParser(text).parse()
            }
        } catch (ex: Exception) {
            jsonLog.e("Parse Json error: ", ex.localizedMessage)
            jsonLog.e(ex)
            return null
        }
    }

    fun toKson(v: Any?): KsonValue {
        return KsonEncoder.encode(v, null)
    }

    fun toKson(v: Any?, config: KsonEncoderConfig?): KsonValue {
        return KsonEncoder.encode(v, config)
    }

    inline fun <reified T : Any> toModel(kson: KsonValue, config: KsonDecoderConfig? = null): T? {
        return KsonDecoder.decodeByClass(kson, T::class, config) as T?
    }

    fun toModelClass(value: KsonValue, cls: KClass<*>, config: KsonDecoderConfig? = null): Any? {
        return KsonDecoder.decodeByClass(value, cls, config)
    }

    inline fun <reified T : Any> toModelGeneric(value: KsonValue, ktype: KType, config: KsonDecoderConfig? = null): T? {
        return KsonDecoder.decodeByType(value, ktype, config) as T?
    }

    object Types {
        val ArrayListString: KType by lazy { object : KsonTypeTake<ArrayList<String>>() {}.type }
        val ArrayListInt: KType by lazy { object : KsonTypeTake<ArrayList<Int>>() {}.type }
        val ArrayListLong: KType by lazy { object : KsonTypeTake<ArrayList<Long>>() {}.type }
        val HashMapStringString: KType by lazy { object : KsonTypeTake<HashMap<String, String>>() {}.type }
        val HashMapStringInt: KType by lazy { object : KsonTypeTake<HashMap<String, Int>>() {}.type }
        val HashMapStringLong: KType by lazy { object : KsonTypeTake<HashMap<String, Long>>() {}.type }
    }
}

abstract class KsonTypeTake<T> {

    val type: KType by lazy { this::class.supertypes.first().arguments.first().type!! }
}

class KsonError(msg: String = "KsonError") : Exception("Json解析错误, $msg") {

    constructor(msg: String, text: String, pos: Int) : this(
        msg + ", " + if (pos < text.length) text.substring(pos, Math.min(pos + 20, text.length)) else text
    )
}

fun escapeJson(s: String): String {
    return encodeJsonString(s)
}

internal val String.quoted get() = "\"$this\""
