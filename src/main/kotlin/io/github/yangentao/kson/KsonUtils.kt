package io.github.yangentao.kson

import io.github.yangentao.anno.Exclude
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

private val KProperty1<*, *>.acceptJson: Boolean get() = this.isPublic && this is KMutableProperty1 && !this.isAbstract && !this.isConst && !this.hasAnnotation<Exclude>()

internal val KClass<*>.propertiesJSON: List<KMutableProperty1<*, *>>
    get() {
        val map = this.java.declaredFields.withIndex().associate { it.value.name to it.index }
        val ls = this.declaredMemberProperties.sortedBy { map[it.name] ?: map[it.name + "$" + "delegate"] }
        return ls.filter { it.acceptJson }.map { (it as KMutableProperty1) }
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

internal fun KProperty<*>.getPropValue(inst: Any? = null): Any? {
    if (this.getter.parameters.isEmpty()) {
        return this.getter.call()
    }
    return this.getter.call(inst)
}

internal val KProperty<*>.isPublic: Boolean get() = this.visibility == KVisibility.PUBLIC
internal val KType.genericArgs: List<KTypeProjection> get() = this.arguments.filter { it.variance == KVariance.INVARIANT }
internal val KType.isGeneric: Boolean get() = this.arguments.isNotEmpty()

//yyyy-MM-dd HH:mm:ss
internal fun formatDateTime(date: Long): String {
    return format(date, "yyyy-MM-dd HH:mm:ss")
}

//yyyy-MM-dd
internal fun formatDate(date: Long): String {
    return format(date, "yyyy-MM-dd")
}

//HH:mm:ss
internal fun formatTime(date: Long): String {
    return format(date, "HH:mm:ss")
}

internal fun format(date: Long, pattern: String): String {
    val ff = SimpleDateFormat(pattern, Locale.getDefault())
    return ff.format(Date(date))
}

internal fun parseDate(s: String?): Long? {
    if (s == null || s.length < 6) {
        return null
    }
    return parse(listOf("yyyy-MM-dd", "yyyy-M-d"), s)
}

internal fun parseTime(s: String?): Long? {
    if (s == null || s.length < 6) {
        return null
    }
    return parse(listOf("HH:mm:ss", "H:m:s", "HH:mm:ss.SSS", "H:m:s.S"), s)
}

internal fun parseDateTime(s: String?): Long? {
    if (s == null || s.length < 6) {
        return null
    }
    return parse(
        listOf(
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-M-d H:m:s.S",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-M-d H:m:s",
            "yyyy-MM-ddTHH:mm:ss.SSS",
            "yyyy-M-dTH:m:s.S",
            "yyyy-MM-ddTHH:mm:ss",
            "yyyy-M-dTH:m:s"
        ), s
    )
}

internal fun parse(formats: List<String>, dateStr: String, locale: Locale = Locale.getDefault()): Long? {
    for (f in formats) {
        val d = parse(f, dateStr, locale)
        if (d != null) return d
    }
    return null
}

internal fun parse(format: String, dateStr: String, locale: Locale = Locale.getDefault()): Long? {
    val ff = SimpleDateFormat(format, locale)
    try {
        val d = ff.parse(dateStr)
        if (d != null) {
            return d.time
        }
    } catch (_: Exception) {
    }
    return null
}