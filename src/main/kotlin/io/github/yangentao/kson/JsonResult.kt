@file:Suppress("unused", "FunctionName")

package io.github.yangentao.kson

class JsonResult private constructor(val jo: KsonObject = KsonObject()) {

    constructor(ok: Boolean, data: Any? = null, msgFailed: String? = null, msgOK: String? = null) : this() {
        if (ok) {
            result(code = CODE_OK, msg = msgOK, data = data)
        } else {
            result(code = -1, msg = msgFailed, data = data)
        }
    }

    constructor(code: Int, msg: String?, data: Any?) : this() {
        this.code = code
        this.message = msg
        this.data = data
    }

    @Suppress("PropertyName")
    val OK: Boolean get() = code == CODE_OK

    var code: Int
        get() = jo.getInt(CODE) ?: 0;
        set(value) = jo.putInt(CODE, value)

    var message: String?
        get() = jo.getString(MSG)
        set(value) = jo.putString(MSG, value)

    var data: Any?
        get() = jo.getAny(DATA)
        set(value) = jo.putAny(DATA, value)

    var table: JsonTable
        get() {
            val ob = jo.getObject(TABLE)
            if (ob != null) return JsonTable(ob)
            val tab = JsonTable()
            jo.putAny(TABLE, tab.tableObject)
            return tab
        }
        set(value) = jo.putAny(TABLE, value.tableObject)

    fun result(code: Int, msg: String?, data: Any?): JsonResult {
        this.code = code
        this.message = msg
        this.data = data
        return this
    }

    fun success(msg: String? = MSG_OK, data: Any? = null, code: Int = CODE_OK): JsonResult {
        return result(code = code, msg = msg ?: MSG_OK, data = data)
    }

    fun failed(msg: String? = MSG_FAILED, data: Any? = null, code: Int = -1): JsonResult {
        return result(code = code, msg = msg ?: MSG_FAILED, data = data)
    }

    fun withAttrs(vararg attrs: Pair<String, Any?>): JsonResult {

        for (p in attrs) {
            jo.putAny(p.first, p.second)
        }
        return this
    }

    fun putAll(vararg ps: Pair<String, Any?>): JsonResult {
        for (p in ps) {
            jo.putAny(p.first, p.second)
        }
        return this
    }

    fun put(key: String, value: Any?): JsonResult {
        jo.putAny(key, value)
        return this
    }

    fun data(value: Any): JsonResult {
        this.data = value
        return this
    }

    fun table(tab: JsonTable): JsonResult {
        this.table = tab
        return this
    }

    fun tableRows(rows: Collection<KsonObject>): JsonResult {
        this.table.addRows(rows)
        return this
    }

    fun dataObject(vararg ps: Pair<String, Any?>): JsonResult {
        val obj = KsonObject()
        for (p in ps) {
            obj.putAny(p.first, p.second)
        }
        this.data = obj
        return this
    }

    fun dataObject(yo: KsonObject): JsonResult {
        return data(yo)
    }

    fun dataArray(ya: KsonArray): JsonResult {
        return data(ya)
    }

    fun <T : Any> dataList(list: Collection<T>): JsonResult {
        return this.data(ksonArray(list))
    }

    fun <T : Any> dataList(list: Collection<T>, block: (T) -> Any?): JsonResult {
        return this.data(ksonArray(list, block))
    }

    override fun toString(): String {
        return jo.toString()
    }

    companion object {
        const val CODE_OK = 0
        const val MSG_OK = "操作成功"
        const val MSG_FAILED = "操作失败"

        const val TABLE = "table"
        var MSG = "msg"
        var CODE = "code"
        var DATA = "data"

        fun success(code: Int = CODE_OK, msg: String = MSG_OK, data: Any? = null): JsonResult {
            return JsonResult(code, msg, data)
        }

        fun failed(code: Int = -1, msg: String = MSG_FAILED, data: Any? = null): JsonResult {
            return JsonResult(code, msg, data)
        }
    }
}

fun JsonFailed(msg: String = JsonResult.MSG_FAILED, data: Any? = null, code: Int = -1): JsonResult {
    return JsonResult(code = code, msg = msg, data = data)
}

fun JsonSuccess(msg: String = JsonResult.MSG_OK, data: Any? = null, code: Int = JsonResult.CODE_OK, attrs: List<Pair<String, Any?>> = emptyList()): JsonResult {
    val r = JsonResult(code = code, msg = msg, data = data)
    if (attrs.isNotEmpty()) {
        for (p in attrs) r.put(p.first, p.second)
    }
    return r
}