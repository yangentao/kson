@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package io.github.yangentao.kson

import io.github.yangentao.anno.userName
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class KsonObject(val data: LinkedHashMap<String, KsonValue> = LinkedHashMap(32)) : KsonValue(), Map<String, KsonValue> by data {
    var caseLess = false

    constructor(capcity: Int) : this(LinkedHashMap<String, KsonValue>(capcity))

    constructor(json: String, loose: Boolean = false) : this() {
        val v = Kson.parse(json, loose)
        if (v is KsonObject) {
            data.putAll(v.data)
        }
    }

    override fun kson(buf: StringBuilder) {
        buf.append("{")
        var first = true
        for ((k, v) in data) {
            if (!first) {
                buf.append(",")
            }
            first = false
            buf.append(escapeJson(k).quoted).append(":")
            v.kson(buf)
        }
        buf.append("}")
    }

    override fun preferBufferSize(): Int {
        return 256
    }

    override fun toString(): String {
        return kson()
    }

    private val _changedProperties = ArrayList<KMutableProperty<*>>(8)
    private var gather: Boolean = false

    @Synchronized
    fun gather(block: () -> Unit): ArrayList<KMutableProperty<*>> {
        this.gather = true
        this._changedProperties.clear()
        block()
        val ls = ArrayList<KMutableProperty<*>>(_changedProperties)
        this.gather = false
        return ls
    }

    fun removeProperty(p: KProperty<*>) {
        this.data.remove(p.userName)
    }

    fun putString(key: String, value: String?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = KsonString(value)
        }
    }

    fun getString(key: String): String? {
        return when (val v = get(key)) {
            null -> null
            is KsonString -> v.data
            is KsonBool -> v.data.toString()
            is KsonNum -> v.data.toString()
            is KsonNull -> null
            is KsonObject -> v.toString()
            is KsonArray -> v.toString()
            else -> v.toString()
        }
    }

    fun putInt(key: String, value: Int?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = KsonNum(value)
        }
    }

    fun getInt(key: String): Int? {
        return when (val v = get(key)) {
            is KsonNum -> v.data.toInt()
            is KsonString -> v.data.toIntOrNull()
            else -> null
        }
    }

    fun putLong(key: String, value: Long?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = KsonNum(value)
        }
    }

    fun getLong(key: String): Long? {
        return when (val v = get(key)) {
            is KsonNum -> v.data.toLong()
            is KsonString -> v.data.toLongOrNull()
            else -> null
        }
    }

    fun putReal(key: String, value: Double?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = KsonNum(value)
        }
    }

    fun getReal(key: String): Double? {
        return when (val v = get(key)) {
            is KsonNum -> v.data.toDouble()
            is KsonString -> v.data.toDoubleOrNull()
            else -> null
        }
    }

    fun putBool(key: String, value: Boolean?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = KsonBool(value)
        }
    }

    fun getBool(key: String): Boolean? {
        val v = get(key) ?: return null
        return when (v) {
            is KsonNull -> null
            is KsonBool -> v.data
            is KsonNum -> v.data != 0
            is KsonString -> v.data == "true" || v.data == "yes" || v.data == "1"
            else -> null
        }
    }

    fun putObject(key: String, value: KsonObject?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = value
        }
    }

    fun putObject(key: String, block: KsonObject.() -> Unit) {
        val yo = KsonObject()
        yo.block()
        data[key] = yo
    }

    fun getObject(key: String): KsonObject? {
        return get(key) as? KsonObject
    }

    fun putArray(key: String, value: KsonArray?) {
        if (value == null) {
            data[key] = KsonNull
        } else {
            data[key] = value
        }
    }

    fun getArray(key: String): KsonArray? {
        return get(key) as? KsonArray
    }

    fun putAny(key: String, value: Any?) {
        data[key] = from(value)
    }

    fun getAny(key: String): Any? {
        return get(key)
    }

    fun putNull(key: String) {
        data[key] = KsonNull
    }

    infix fun <V> String.TO(value: V) {
        putAny(this, value)
    }

    infix fun String.TO(value: KsonObject) {
        putObject(this, value)
    }

    infix fun String.TO(value: KsonArray) {
        putArray(this, value)
    }

    operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.data[property.userName] = Kson.toKson(value)
        if (this.gather) {
            if (property is KMutableProperty) {
                if (property !in this._changedProperties) {
                    this._changedProperties.add(property)
                }
            }
        }
    }

    inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        val retType = property.returnType
        val v = if (caseLess) {
            this[property.userName] ?: this[property.userName.lowercase()]
        } else {
            this[property.userName]
        } ?: KsonNull

        if (v !is KsonNull) {
            val pv = KsonDecoder.decodeByType(v, retType, null)
            if (pv != null || retType.isMarkedNullable) {
                return pv as V
            }
        }
        return null as V
    }

    operator fun KProperty0<*>.unaryPlus() {
        putAny(this.userName, this.getPropValue())
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): KsonObject {
            val yo = KsonObject()
            for (e in map.entries) {
                yo.putAny(e.key, e.value)
            }
            return yo
        }
    }

}

inline fun <reified T : Any> KClass<T>.createYsonModel(argValue: KsonObject): T {
    val c = this.constructors.first { it.parameters.size == 1 && it.parameters.first().type.classifier == KsonObject::class }
    return c.call(argValue)
}

fun ksonObject(block: KsonObject.() -> Unit): KsonObject {
    val b = KsonObject()
    b.block()
    return b
}

fun ksonObject(vararg ps: Pair<String, Any?>): KsonObject {
    val b = KsonObject()
    for (p in ps) b.putAny(p.first, p.second)
    return b
}
