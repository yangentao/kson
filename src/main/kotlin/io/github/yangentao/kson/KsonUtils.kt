package io.github.yangentao.kson

import io.github.yangentao.anno.Exclude
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
internal fun formatDateTime(date: java.util.Date): String {
    return simpleDateTimePattern.format(date)
}

//yyyy-MM-dd
internal fun formatDate(date: java.util.Date): String {
    return simpleDatePattern.format(date)
}

//HH:mm:ss
internal fun formatTime(date: java.util.Date): String {
    return simpleTimePattern.format(date)
}

private val simpleDateTimePattern = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
private val simpleDatePattern = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val simpleTimePattern = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

internal fun formatDate(date: LocalDate): String {
    return date.format(localDatePattern)
}

internal fun formatTime(time: LocalTime): String {
    return time.format(localTimePattern)
}

internal fun formatDateTime(datetime: LocalDateTime): String {
    return datetime.format(localDateTimePattern)
}

private val localDateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val localDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val localTimePattern = DateTimeFormatter.ofPattern("HH:mm:ss")

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
    return parse(listOf("HH:mm:ss", "H:m:s", "HH:mm:ss.SSS", "H:m:s.S", "HH:mm:ss.SSSSSS"), s)
}

internal fun parseDateTime(s: String?): Long? {
    if (s == null || s.length < 6) {
        return null
    }
    return parse(
        listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-M-d H:m:s.S",
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

internal fun toLocalDate(mill: Long): LocalDate {
    val ins = java.time.Instant.ofEpochMilli(mill)
    return LocalDate.ofInstant(ins, ZoneId.systemDefault())
}

internal fun toLocalTime(mill: Long): LocalTime {
    val ins = java.time.Instant.ofEpochMilli(mill)
    return LocalTime.ofInstant(ins, ZoneId.systemDefault())
}

internal fun toLocalDateTime(mill: Long): LocalDateTime {
    val ins = java.time.Instant.ofEpochMilli(mill)
    return LocalDateTime.ofInstant(ins, ZoneId.systemDefault())
}

