package io.github.yangentao.kson

import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.*


internal  fun KProperty<*>.getPropValue(inst: Any? = null): Any? {
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