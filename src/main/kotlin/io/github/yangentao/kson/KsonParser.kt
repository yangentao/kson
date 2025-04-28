@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

private const val CR: Char = '\r'
private const val LF: Char = '\n'
private const val SP: Char = ' '
private const val TAB: Char = '\t'
private const val DOT: Char = '.'
private const val QT: Char = '\"'
private const val SQT: Char = '\''
private val WHITE: Set<Char> = hashSetOf(CR, LF, SP, TAB)
private val NUM_START: Set<Char> = hashSetOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
private val NUMS: Set<Char> = hashSetOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', DOT, 'e', 'E', '+', '-')
private val ESCAP: Set<Char> = hashSetOf(SQT, '\\', '/', 'b', 'f', 'n', 'r', 't', 'u')

private val Char.isWhite: Boolean get() = this in WHITE

class KsonError(msg: String = "YsonError") : Exception("Json解析错误, $msg") {

    constructor(msg: String, text: String, pos: Int) : this(
        msg + ", " + if (pos < text.length) text.substring(pos, Math.min(pos + 20, text.length)) else text
    )
}

class KsonParser(val text: String) {
    private val data: CharArray = text.toCharArray()
    private var current: Int = 0

    private val end: Boolean get() = current >= data.size
    private val currentChar: Char get() = data[current]

    val leftString: String
        get() {
            if (current >= data.size) {
                return ""
            }
            val sb = StringBuilder()
            var n = 0
            while (n < 20) {
                if (current + n >= data.size) {
                    break
                }
                sb.append(data[current + n])
                ++n
            }
            return sb.toString()
        }

    fun parse(endParse: Boolean): KsonValue {
        skipWhite()
        if (end) {
            throw KsonError("空的字符串")
        }
        val ch = currentChar
        val v = when (ch) {
            '{' -> parseObject()
            '[' -> parseArray()
            QT -> parseString()
            't' -> parseTrue()
            'f' -> parseFalse()
            'n' -> parseNull()
            in NUM_START -> parseNumber()
            else -> throw KsonError("")
        }
        skipWhite()
        if (endParse) {
            if (!this.end) {
                throw IllegalArgumentException("应该结束解析:$leftString")
            }
        }
        return v
    }

    fun parseArray(): KsonArray {
        skipWhite()
        tokenc('[')
        val ya = KsonArray()
        while (!end) {
            skipWhite()
            if (currentChar == ']') {
                break
            }
            if (currentChar == ',') {
                next()
                continue
            }
            val yv = parse(false)
            ya.data.add(yv)
        }
        tokenc(']')
        return ya
    }

    fun parseObject(): KsonObject {
        skipWhite()
        tokenc('{')
        val yo = KsonObject()
        while (!end) {
            skipWhite()
            if (currentChar == '}') {
                break
            }
            if (currentChar == ',') {
                next()
                continue
            }
            val key = parseString()
            tokenc(':')
            val yv = parse(false)
            yo.data[key.data] = yv
        }
        tokenc('}')
        return yo
    }

    fun parseString(): KsonString {
        skipWhite()
        tokenc(QT)//字符串以双引号开始
        val buf = StringBuilder(64)
        var escing = false
        while (!end) {
            val ch = currentChar
            if (!escing) {
                if (ch == QT) {//字符串结束,双引号
                    break
                }
                next()
                if (ch == '\\') {//开始转义
                    escing = true
                    continue
                }
                buf.append(ch)//正常字符
            } else {
                escing = false
                next()
                when (ch) {
                    QT, SQT, '\\', '/' -> buf.append(ch)
                    'b' -> buf.append('\b')
                    'f' -> buf.append(12.toChar())
                    'n' -> buf.append(LF)
                    'r' -> buf.append(CR)
                    't' -> buf.append(TAB)
                    'u', 'U' -> {
                        if (current + 4 < text.length) {
                            val sb = StringBuilder(4)
                            sb.append(text[current + 0])
                            sb.append(text[current + 1])
                            sb.append(text[current + 2])
                            sb.append(text[current + 3])
                            current += 4
                            val n = sb.toString().toInt(16)
                            buf.appendCodePoint(n)
                            //TODO UTF16 解码 https://cloud.tencent.com/developer/article/1625557
                        } else {
                            throw KsonError("期望是unicode字符")
                        }
                    }

                    else -> {
                        buf.append(ch)
                    }

                }
            }
        }
        if (escing) {
            throw KsonError("解析错误,转义,")
        }
        tokenc(QT)
        return KsonString(buf.toString())
    }

    fun parseNumber(): KsonValue {
        skipWhite()
        val buf = StringBuilder(32)
        while (!end) {
            val c = currentChar
            if (c !in NUMS) {
                break
            }
            buf.append(c)
            next()
        }
        val s = buf.toString()
        if (s.isEmpty()) {
            throw KsonError("非数字")
        }
        if ('.' in s) {
            val d = s.toDouble()
            return KsonNum(d)
        }
        val n = s.toLong()
        return KsonNum(n)
    }

    fun parseTrue(): KsonBool {
        skipWhite()
        tokens("true")
        return KsonBool.True
    }

    fun parseFalse(): KsonBool {
        skipWhite()
        tokens("false")
        return KsonBool.False
    }

    fun parseNull(): KsonNull {
        skipWhite()
        tokens("null")
        return KsonNull.inst
    }

    private fun next() {
        current += 1
    }

    private fun skipWhite() {
        while (!end) {
            if (currentChar.isWhite) {
                next()
            } else {
                return
            }
        }
    }

    private fun isChar(c: Char): Boolean {
        return currentChar == c
    }

    private fun tokenc(c: Char) {
        skipWhite()
        if (currentChar != c) {
            throw KsonError("期望是字符$c", text, current)
        }
        next()
        skipWhite()
    }

    private fun tokens(s: String) {
        skipWhite()
        for (c in s) {
            if (currentChar != c) {
                throw KsonError("期望是字符串$s", text, current)
            }
            next()
        }
        skipWhite()
    }

}

private val esCharSet: Set<Char> = setOf('\\', '\"', '/', '\b', '\n', '\r', '\t', 12.toChar())
private val unicodeReg: Regex = Regex("u[0-9a-fA-F]{4}]")
private val _fch: Char = 12.toChar()
fun escapeJson(s: String): String {
    var n = 0
    for (c in s) {
        if (c in esCharSet) {
            n += 1
        }
    }

    if (n == 0 && !s.matches(unicodeReg)) {
        return s
    }
    val sb = StringBuilder(s.length + n)
    for (i in s.indices) {
        val c = s[i]
        when (c) {
            '\\' -> if (i + 5 < s.length
                && (s[i + 1] == 'u' || s[i + 1] == 'U')
                && s[i + 2].isHex
                && s[i + 3].isHex
                && s[i + 4].isHex
                && s[i + 5].isHex
            ) {
                sb.append("""\""")
            } else {
                sb.append("""\\""")
            }

            '\"' -> sb.append("""\"""")
            '/' -> sb.append("""\/""")
            '\b' -> sb.append("""\b""")
            '\n' -> sb.append("""\n""")
            '\r' -> sb.append("""\r""")
            '\t' -> sb.append("""\t""")
            _fch -> sb.append("""\f""")
            else -> {
                sb.append(c)
            }
        }
    }
    return sb.toString()
}

private val Char.isHex: Boolean get() = (this in '0'..'9') || (this in 'a'..'f') || (this in 'A'..'F')

fun main() {
    val a = """
        1"
        2'
        \u12A0\uEF09\uEF0Z
    """.trimIndent()
    println(escapeJson(a))
}

