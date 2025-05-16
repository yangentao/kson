@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

class KsonArray(val data: ArrayList<KsonValue> = ArrayList(16)) : KsonValue(), MutableList<KsonValue> by data {

    constructor(capcity: Int) : this(ArrayList<KsonValue>(capcity))

    constructor(json: String, loose: Boolean = false) : this() {
        val v = Kson.parse(json, loose)
        if (v is KsonArray) {
            data.addAll(v.data)
        }
    }

    override fun kson(buf: StringBuilder) {
        buf.append("[")
        for (i in data.indices) {
            if (i != 0) {
                buf.append(",")
            }
            data[i].kson(buf)
        }
        buf.append("]")
    }

    override fun preferBufferSize(): Int {
        return data.sumOf { it.preferBufferSize() }
    }

    override fun toString(): String {
        return kson()
    }

    fun toBoolArray(): BooleanArray {
        return this.map { (it as KsonBool).data }.toBooleanArray()
    }

    fun toByteArray(): ByteArray {
        return this.map { (it as KsonNum).data.toByte() }.toByteArray()
    }

    fun toShortArray(): ShortArray {
        return this.map { (it as KsonNum).data.toShort() }.toShortArray()
    }

    fun toIntArray(): IntArray {
        return this.map { (it as KsonNum).data.toInt() }.toIntArray()
    }

    fun toLongArray(): LongArray {
        return this.map { (it as KsonNum).data.toLong() }.toLongArray()
    }

    fun toFloatArray(): FloatArray {
        return this.map { (it as KsonNum).data.toFloat() }.toFloatArray()
    }

    fun toDoubleArray(): DoubleArray {
        return this.map { (it as KsonNum).data.toDouble() }.toDoubleArray()
    }

    fun toCharArray(): CharArray {
        return this.map { (it as KsonString).data.first() }.toCharArray()
    }

    fun toStringArray(): Array<String> {
        return this.map { (it as KsonString).data }.toTypedArray()
    }

    fun toByteList(): List<Byte> {
        return this.map { (it as KsonNum).data.toByte() }
    }

    fun toShortList(): List<Short> {
        return this.map { (it as KsonNum).data.toShort() }
    }

    fun toIntList(): List<Int> {
        return this.map { (it as KsonNum).data.toInt() }
    }

    fun toLongList(): List<Long> {
        return this.map { (it as KsonNum).data.toLong() }
    }

    fun toFloatList(): List<Float> {
        return this.map { (it as KsonNum).data.toFloat() }
    }

    fun toDoubleList(): List<Double> {
        return this.map { (it as KsonNum).data.toDouble() }
    }

    fun toCharList(): List<Char> {
        return this.map { (it as KsonString).data.first() }
    }

    fun toStringList(): List<String> {
        return this.map { (it as KsonString).data }
    }

    fun toObjectList(): List<KsonObject> {
        return this.map { it as KsonObject }
    }

    inline fun <reified T : Any> toList(): List<T> {
        return this.mapNotNull {
            KsonDecoder.decodeValue(it)
        }
    }

    fun add(value: String?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonString(value))
        }
    }

    fun add(value: Boolean?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonBool(value))
        }
    }

    fun add(value: Int?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonNum(value))
        }
    }

    fun add(value: Long?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonNum(value))
        }
    }

    fun add(value: Float?) {
        add(value?.toDouble())
    }

    fun add(value: Double?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonNum(value))
        }
    }

    fun addBlob(value: ByteArray?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(KsonBlob(value))
        }
    }

    fun add(value: KsonValue?) {
        if (value == null) {
            data.add(KsonNull)
        } else {
            data.add(value)
        }
    }

    fun addAny(value: Any?) {
        data.add(from(value))
    }

    inline fun eachObject(block: (KsonObject) -> Unit) {
        for (item in this) {
            block(item as KsonObject)
        }
    }

    inline fun <R> mapObject(transform: (KsonObject) -> R): ArrayList<R> {
        val ls = ArrayList<R>(this.size)
        for (yo in this) {
            ls += transform(yo as KsonObject)
        }
        return ls
    }

}

fun ksonArray(values: Collection<Any?>): KsonArray {
    val arr = KsonArray()
    for (v in values) {
        arr.addAny(v)
    }
    return arr
}

fun ksonArray(vararg values: Any): KsonArray {
    val arr = KsonArray()
    for (v in values) {
        arr.addAny(v)
    }
    return arr
}

fun <T> ksonArray(values: Collection<T>, block: (T) -> Any?): KsonArray {
    val arr = KsonArray()
    for (v in values) {
        arr.addAny(block(v))
    }
    return arr
}