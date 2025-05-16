@file:JvmName("KsonParserKt")

package io.github.yangentao.kson

import io.github.yangentao.charcode.CharCode
import io.github.yangentao.charcode.TextScanner

class LooeseJsonParser(json: String) : JsonParser(json) {

    override fun parseObject(): KsonObject {
        ts.skipWhites()
        val map = KsonObject()
        ts.expectChar(CharCode.LCUB)
        while (ts.nowChar != CharCode.RCUB) {
            ts.skipWhites()
            val key = if (ts.nowChar == CharCode.QUOTE) parseString() else parseIdent()
            ts.skipWhites()
            ts.expectAnyChar(ASSIGN)
            val v = parseValue()
            map.putAny(key, v)
            val trails = ts.skipChars(TRAIL)
            if (ts.nowChar != CharCode.RCUB) {
                if (trails.intersect(SEP).isEmpty()) raise()
            }
        }
        ts.expectChar(CharCode.RCUB)
        return map
    }

    override fun parseArray(): KsonArray {
        ts.skipWhites()
        val list = KsonArray()
        ts.expectChar(CharCode.LSQB)
        ts.skipWhites()
        while (ts.nowChar != CharCode.RSQB) {
            ts.skipWhites()
            val v = parseValue()
            list.add(v)
            val trails = ts.skipChars(TRAIL)
            if (ts.nowChar != CharCode.RSQB) {
                if (trails.intersect(SEP).isEmpty()) raise()
            }
        }
        ts.expectChar(CharCode.RSQB)
        return list
    }

    companion object {
        private val ASSIGN: Set<Char> = setOf(CharCode.COLON, CharCode.EQUAL)
        private val SEP: Set<Char> = setOf(CharCode.COMMA, CharCode.SEMI, CharCode.CR, CharCode.LF)
        private val TRAIL: Set<Char> = setOf(CharCode.SP, CharCode.HTAB, CharCode.CR, CharCode.LF, CharCode.COMMA, CharCode.SEMI)
    }
}

open class JsonParser(json: String) {
    protected val ts = TextScanner(json)

    fun parse(): KsonValue {
        val v = parseValue()
        ts.skipWhites()
        if (!ts.isEnd) raise()
        return v
    }

    protected fun parseValue(): KsonValue {
        if (ts.isEnd) return KsonNull
        ts.skipWhites()
        val ch = ts.nowChar
        return when (ch) {
            CharCode.LCUB -> parseObject()
            CharCode.LSQB -> parseArray()
            CharCode.QUOTE -> KsonString(parseString())
            CharCode.MINUS -> KsonNum(parseNum())
            in CharCode.NUM0..CharCode.NUM9 -> KsonNum(parseNum())
            CharCode.n -> {
                ts.expectString("null")
                KsonNull
            }

            CharCode.t -> {
                ts.expectString("true")
                KsonBool(true)
            }

            CharCode.f -> {
                ts.expectString("false")
                KsonBool(false)
            }

            else -> raise()

        }
    }

    protected open fun parseObject(): KsonObject {
        ts.skipWhites()
        val map = KsonObject()
        ts.expectChar(CharCode.LCUB)
        ts.skipWhites()
        while (ts.nowChar != CharCode.RCUB) {
            ts.skipWhites()
            val key = parseString()
            ts.skipWhites()
            ts.expectChar(CharCode.COLON)
            val v = parseValue()
            map.putAny(key, v)
            ts.skipWhites()
            if (ts.nowChar != CharCode.RCUB) {
                ts.expectChar(CharCode.COMMA)
                ts.skipWhites()
            }
        }
        ts.expectChar(CharCode.RCUB)
        return map
    }

    protected open fun parseArray(): KsonArray {
        ts.skipWhites()
        val list = KsonArray()
        ts.expectChar(CharCode.LSQB)
        ts.skipWhites()
        while (ts.nowChar != CharCode.RSQB) {
            ts.skipWhites()
            val v = parseValue()
            list.add(v)
            ts.skipWhites()
            if (ts.nowChar != CharCode.RSQB) {
                ts.expectChar(CharCode.COMMA)
                ts.skipWhites()
            }
        }
        ts.expectChar(CharCode.RSQB)
        return list
    }

    protected fun parseNum(): Number {
        val buf = ts.moveNext(acceptor = { isNum(it) })
        if (buf.isEmpty()) raise("Except number")
        val s = String(buf.toCharArray())
        if (CharCode.DOT in buf) {
            return s.toDouble()
        }
        return s.toLong()
    }

    protected fun parseIdent(): String {
        val charList = ts.expectIdent()
        return String(charList.toCharArray())
    }

    protected fun parseString(): String {
        ts.expectChar(CharCode.QUOTE)
        val charList = ts.moveNext(terminator = { it == CharCode.QUOTE && ts.lastBuf.lastOrNull() != CharCode.BSLASH })
        val s = codesToString(charList)
        ts.expectChar(CharCode.QUOTE)
        return s
    }

    protected fun raise(msg: String = "Json parse error"): Nothing {
        error("$msg. ${ts.position}, ${ts.leftText}")
    }
}

private fun isNum(ch: Char): Boolean {
    if (ch >= CharCode.NUM0 && ch <= CharCode.NUM9) return true
    return ch == CharCode.DOT || ch == CharCode.MINUS || ch == CharCode.PLUS || ch == CharCode.e || ch == CharCode.E
}

private fun codesToString(charList: List<Char>): String {
    val buf = ArrayList<Char>()
    var escaping = false
    var i = 0
    while (i < charList.size) {
        val ch = charList[i]
        if (!escaping) {
            if (ch == CharCode.BSLASH) {
                escaping = true;
            } else {
                buf.add(ch);
            }
        } else {
            escaping = false;
            when (ch) {
                CharCode.SQUOTE, CharCode.BSLASH, CharCode.SLASH -> {
                    buf.add(ch);
                }

                CharCode.b -> buf.add(CharCode.BS)
                CharCode.f -> buf.add(CharCode.FF)
                CharCode.n -> buf.add(CharCode.LF)
                CharCode.r -> buf.add(CharCode.CR)
                CharCode.t -> buf.add(CharCode.HTAB)
                CharCode.u, CharCode.U -> {
                    val uls = ArrayList<Char>()
                    i += 1;
                    if (i < charList.size && charList[i] == CharCode.PLUS) {
                        i += 1;
                    }
                    while (i < charList.size && uls.size < 4 && CharCode.isHex(charList[i])) {
                        uls.add(charList[i]);
                        i += 1;
                    }
                    if (uls.size != 4) error("Convert to string failed: ${String(charList.toCharArray())}.");
                    val s = String(uls.toCharArray())
                    val n = s.toInt(16)
                    val charArr = Character.toChars(n)
                    for (c in charArr) buf.add(c)
                    i -= 1;
                }

                else -> buf.add(ch)
            }
        }
        i += 1;
    }
    return String(buf.toCharArray());
}

internal fun encodeJsonString(s: String): String {
    val chars: CharArray = s.toCharArray()
    val buf: ArrayList<Char> = ArrayList()
    var i: Int = 0
    while (i < chars.size) {
        val ch = chars[i]
        if (ch < CharCode.SP) {
            when (ch) {
                CharCode.BS -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.b)
                }

                CharCode.FF -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.f)
                }

                CharCode.LF -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.n)
                }

                CharCode.CR -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.r)
                }

                CharCode.HTAB -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.t)
                }

                else -> {
                    val x: Int = ch.code
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.u)
                    buf.add(CharCode.NUM0)
                    buf.add(CharCode.NUM0)
                    buf.add(lastHex(x shr 4))
                    buf.add(lastHex(x))
                }
            }
        } else if (CharCode.isUnicodeLead(ch) && (i + 1 < chars.size) && CharCode.isUnicodeTrail(chars[i + 1])) {
            val x: Int = ch.code
            buf.add(CharCode.BSLASH)
            buf.add(CharCode.u)
            buf.add(CharCode.d)
            buf.add(lastHex(x shr 8))
            buf.add(lastHex(x shr 4))
            buf.add(lastHex(x))

            val y = chars[i + 1].code
            buf.add(CharCode.BSLASH)
            buf.add(CharCode.u)
            buf.add(CharCode.d)
            buf.add(lastHex(y shr 8))
            buf.add(lastHex(y shr 4))
            buf.add(lastHex(y))
            i += 1
        } else {
            when (ch) {
                CharCode.SQUOTE -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.SQUOTE);
                }

                CharCode.BSLASH -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.BSLASH);
                }

                CharCode.SLASH -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.SLASH);
                }

                else -> {
                    buf.add(ch)
                }
            }

        }
        i += 1
    }
    return String(buf.toCharArray())
}

// '0' + x  or  'a' + x - 10
private fun hex4(n: Int): Char = Char(if (n < 10) 48 + n else 87 + n)
private fun lastHex(n: Int): Char = hex4(n and 0x0F)

private fun isUTF16(a: Char, b: Char): Boolean {
    return CharCode.isUnicodeLead(a) && CharCode.isUnicodeTrail(b)
}