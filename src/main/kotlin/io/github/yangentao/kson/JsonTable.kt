package io.github.yangentao.kson

class JsonTable(val tableObject: KsonObject = KsonObject()) : Iterable<KsonObject> {
    val header: KsonArray
    val body: KsonArray

    init {
        val a = tableObject.getArray(HEADER)
        if (a != null) {
            header = a
        } else {
            val ka = KsonArray()
            tableObject.putArray(HEADER, ka)
            header = ka
        }

        val b = tableObject.getArray(BODY)
        if (b != null) {
            body = b
        } else {
            val ka = KsonArray()
            tableObject.putArray(BODY, ka)
            body = ka
        }
    }

    val rowCount: Int get() = body.size
    val colCount: Int get() = header.size

    fun addRow(obj: KsonObject) {
        if (header.isEmpty()) {
            val h = header
            h.data.ensureCapacity(obj.size)
            h.addAll(obj.entries.map { KsonString(it.key) })
        }
        val ka = KsonArray(obj.size)
        ka.addAll(obj.entries.map { it.value })
        body.add(ka)
    }

    fun addRows(ls: Collection<KsonObject>) {
        body.data.ensureCapacity(ls.size)
        for (ob in ls) {
            addRow(ob)
        }
    }

    override fun toString(): String {
        return tableObject.json()
    }

    override fun iterator(): Iterator<KsonObject> {
        return JsonTableIterator(this)
    }

    class JsonTableIterator(val table: JsonTable) : Iterator<KsonObject> {
        var index: Int = 0
        override fun next(): KsonObject {
            val ko = KsonObject(table.header.size)
            val row: KsonArray = table.body[index] as KsonArray
            for (i in 0..<table.colCount) {
                val k: KsonString = table.header[i] as KsonString
                val v: KsonValue = row[i]
                ko.putAny(k.data, v)
            }
            index += 1
            return ko
        }

        override fun hasNext(): Boolean {
            return index < table.rowCount
        }

    }

    companion object {
        const val HEADER = "header"
        const val BODY = "body"

        fun fromRows(rows: Collection<KsonObject>): JsonTable {
            val tab = JsonTable()
            tab.addRows(rows)
            return tab
        }
    }
}