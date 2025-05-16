package io.github.yangentao.kson

/**
 * 第一行是列的名称
 * 从第二行开始, 是数据
 * 类似 csv
 */
class JsonTable(val array: KsonArray = KsonArray()) : Iterable<KsonObject> {
    val header: KsonArray? get() = array.getOrNull(0) as? KsonArray

    val rowCount: Int get() = if (array.isEmpty()) 0 else array.size - 1
    val colCount: Int get() = header?.size ?: 0

    private fun ensureHeader(obj: KsonObject) {
        if (array.isEmpty()) {
            val h = KsonArray(obj.size)
            h.addAll(obj.entries.map { KsonString(it.key) })
            array.add(h)
        }
    }

    private fun appendBody(obj: KsonObject) {
        val ka = KsonArray(obj.size)
        ka.addAll(obj.entries.map { it.value })
        array.add(ka)
    }

    fun addRow(obj: KsonObject): JsonTable {
        ensureHeader(obj)
        appendBody(obj)
        return this
    }

    fun addRows(ls: Collection<KsonObject>): JsonTable {
        if (ls.isEmpty()) return this
        array.data.ensureCapacity(ls.size + 1)
        ensureHeader(ls.first())
        for (ob in ls) {
            appendBody(ob)
        }
        return this
    }

    override fun toString(): String {
        return array.json()
    }

    override fun iterator(): Iterator<KsonObject> {
        return JsonTableIterator(this)
    }

    class JsonTableIterator(val table: JsonTable) : Iterator<KsonObject> {
        private var index: Int = 1
        private val header: KsonArray by lazy { table.array[0] as KsonArray }
        override fun next(): KsonObject {
            val ko = KsonObject(header.size)
            val row: KsonArray = table.array[index] as KsonArray
            for (i in 0..<header.size) {
                val k: KsonString = header[i] as KsonString
                val v: KsonValue = row[i]
                ko.putAny(k.data, v)
            }
            index += 1
            return ko
        }

        override fun hasNext(): Boolean {
            return index < table.array.size
        }

    }

    companion object {

        fun fromRows(rows: Collection<KsonObject>): JsonTable {
            return JsonTable().addRows(rows)
        }
    }
}