@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

import java.util.*

abstract class KsonValue {

    abstract fun kson(buf: StringBuilder)

    open fun kson(): String {
        val sb = StringBuilder(preferBufferSize())
        kson(sb)
        return sb.toString()
    }

    open fun preferBufferSize(): Int {
        return 64
    }

    override fun toString(): String {
        return kson()
    }

    val isCollection: Boolean get() = this is KsonObject || this is KsonArray

    companion object {
        fun from(value: Any?): KsonValue {
            if (value == null) {
                return KsonNull.inst
            }
            if (value is KsonValue) {
                return value
            }
            return KsonEncoder.encode(value, null)
        }
    }
}

class KsonNull private constructor() : KsonValue() {

    override fun kson(buf: StringBuilder) {
        buf.append("null")
    }

    override fun equals(other: Any?): Boolean {
        return other is KsonNull
    }

    override fun preferBufferSize(): Int {
        return 8
    }

    override fun hashCode(): Int {
        return 1000
    }

    companion object {
        val inst: KsonNull = KsonNull()
    }
}

class KsonNum(val data: Number) : KsonValue() {

    override fun kson(buf: StringBuilder) {
        buf.append(data.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (other is KsonNum) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return 12
    }

}

class KsonString(val data: String) : KsonValue() {

    constructor(v: Char) : this(String(charArrayOf(v)))
    constructor(v: StringBuffer) : this(v.toString())
    constructor(v: StringBuilder) : this(v.toString())

    override fun kson(buf: StringBuilder) {
        buf.append("\"")
        buf.append(escapeJson(data))
        buf.append("\"")
    }

    override fun equals(other: Any?): Boolean {
        if (other is KsonString) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return data.length + 8
    }
}

class KsonBool(val data: Boolean) : KsonValue() {
    override fun kson(buf: StringBuilder) {
        if (data) {
            buf.append("true")
        } else {
            buf.append("false")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is KsonBool) {
            return other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return 8
    }

    companion object {
        val True: KsonBool = KsonBool(true)
        val False: KsonBool = KsonBool(false)
    }
}

class KsonBlob(val data: ByteArray) : KsonValue() {

    constructor(v: java.sql.Blob) : this(v.getBytes(1, v.length().toInt()))

    override fun kson(buf: StringBuilder) {
        buf.append("\"")
        buf.append(encoded)
        buf.append("\"")
    }

    val encoded: String get() = encode(data)

    override fun equals(other: Any?): Boolean {
        if (other is KsonBlob) {
            return other.data.contentEquals(data)
        }
        return false
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun preferBufferSize(): Int {
        return data.size * 4 / 3 + 4
    }

    companion object {
        fun encode(data: ByteArray): String {
            return Base64.getUrlEncoder().encodeToString(data)
        }

        fun decode(s: String): ByteArray {
            return Base64.getUrlDecoder().decode(s)
        }

    }

}
