package io.github.yangentao.kson

class TextScanner(val text: String) {
    val codeList: CharArray = text.toCharArray()
    var position: Int = 0
    var lastBuf: ArrayList<Char> = ArrayList()

    val isEnd: Boolean get() = position >= codeList.size
    val isStart: Boolean get() = position == 0
    val nowChar: Char get() = codeList[position]
    val preChar: Char? get() = if (position >= 1) codeList[position - 1] else null
    val lastMatch: String get() = if (lastBuf.isEmpty()) "" else String(lastBuf.toCharArray())

    fun savePosition(): ScanPos = ScanPos(this, position)

    fun printLastBuf() = print(lastMatch)

    fun back(size: Int = 1) {
        if (position > 0) position -= 1
    }
}

class ScanPos(val scanner: TextScanner, val pos: Int) {
    fun restore() {

    }
}