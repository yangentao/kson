@file:Suppress("unused")

package io.github.yangentao.kson

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Kson , encode and decode
 */
object Kson {
    fun parse(text: String): KsonValue? {
        try {
            val p = KsonParser(text)
            return p.parse(true)
        } catch (ex: Exception) {
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