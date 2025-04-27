@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.yangentao.kson

import io.github.yangentao.anno.Exclude
import io.github.yangentao.anno.userName
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.*

object KsonDecoder {

    inline fun <reified E : Any, reified C : MutableList<E>> decodeToList(toList: C, value: KsonValue): C {
        for (y in value as KsonArray) {
            toList.add(decodeByClass(y, E::class, null) as E)
        }
        return toList
    }

    inline fun <reified E : Any, reified C : List<E>> decodeList(value: KsonValue): C {
        val toList: MutableList<E> = if (!C::class.isAbstract && C::class.isSubclassOf(MutableList::class)) {
            @Suppress("UNCHECKED_CAST")
            C::class.createInstance() as MutableList<E>
        } else {
            ArrayList<E>()
        }
        decodeToList(toList, value)
        return toList as C
    }

    inline fun <reified E : Any, reified C : MutableSet<E>> decodeToSet(toSet: C, value: KsonValue): C {
        for (y in value as KsonArray) {
            toSet.add(decodeByClass(y, E::class, null) as E)
        }
        return toSet
    }

    inline fun <reified E : Any, reified C : Set<E>> decodeSet(value: KsonValue): C {
        val toSet: MutableSet<E> = if (!C::class.isAbstract && C::class.isSubclassOf(MutableSet::class)) {
            @Suppress("UNCHECKED_CAST")
            C::class.createInstance() as MutableSet<E>
        } else {
            HashSet<E>()
        }
        return decodeToSet(toSet, value) as C
    }

    inline fun <reified E : Any, reified M : MutableMap<String, E>> decodeToMap(toMap: M, value: KsonValue): M {
        for (y in value as KsonObject) {
            toMap[y.key] = decodeByClass(y.value, E::class, null) as E
        }
        return toMap
    }

    inline fun <reified E : Any, reified M : Map<String, E>> decodeMap(value: KsonValue): M {
        val toMap: MutableMap<String, E> = if (!M::class.isAbstract && M::class.isSubclassOf(MutableMap::class)) {
            @Suppress("UNCHECKED_CAST")
            M::class.createInstance() as MutableMap<String, E>
        } else {
            LinkedHashMap<String, E>()
        }
        return decodeToMap(toMap, value) as M
    }

    inline fun <reified T : Any> decodeValue(value: KsonValue): T? {
        return decodeByClass(value, T::class, null) as? T
    }

    fun decodeByClass(value: KsonValue, cls: KClass<*>, config: KsonDecoderConfig?): Any? {
        if (value is KsonNull) {
            return if (cls == KsonNull::class) KsonNull.inst else null
        }
        if (value::class == cls) {
            return value
        }

        when (cls) {
            Boolean::class -> return when (value) {
                is YsonBool -> value.data
                is KsonNum -> value.data != 0
                is KsonString -> value.data == "true" || value.data == "yes"
                else -> error("type error: $cls  value: $value")
            }

            Byte::class -> return when (value) {
                is KsonNum -> value.data.toByte()
                is YsonBool -> if (value.data) 1.toByte() else 0.toByte()
                is KsonString -> value.data.toByteOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Short::class -> return when (value) {
                is KsonNum -> value.data.toShort()
                is YsonBool -> if (value.data) 1.toShort() else 0.toShort()
                is KsonString -> value.data.toShortOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Int::class -> return when (value) {
                is KsonNum -> value.data.toInt()
                is YsonBool -> if (value.data) 1.toInt() else 0.toInt()
                is KsonString -> value.data.toIntOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Long::class -> return when (value) {
                is KsonNum -> value.data.toLong()
                is YsonBool -> if (value.data) 1.toLong() else 0.toLong()
                is KsonString -> value.data.toLongOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Float::class -> return when (value) {
                is KsonNum -> value.data.toFloat()
                is YsonBool -> if (value.data) 1.toFloat() else 0.toFloat()
                is KsonString -> value.data.toFloatOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Double::class -> return when (value) {
                is KsonNum -> value.data.toDouble()
                is YsonBool -> if (value.data) 1.toDouble() else 0.toDouble()
                is KsonString -> value.data.toDoubleOrNull()
                else -> error("type error: $cls  value: $value")
            }

            BigInteger::class -> return when (value) {
                is KsonNum -> BigInteger(value.data.toString())
                is YsonBool -> if (value.data) BigInteger("1") else BigInteger("0")
                is KsonString -> value.data.toBigIntegerOrNull()
                else -> error("type error: $cls  value: $value")
            }

            BigDecimal::class -> return when (value) {
                is KsonNum -> BigDecimal(value.data.toString())
                is YsonBool -> if (value.data) BigDecimal("1") else BigDecimal("0")
                is KsonString -> value.data.toBigDecimalOrNull()
                else -> error("type error: $cls  value: $value")
            }

            Char::class -> {
                if (value is KsonString) return value.data.firstOrNull()
                error("type error: $cls  value: $value")
            }

            String::class -> return strValue(value)
            StringBuffer::class -> return StringBuffer(strValue(value))
            StringBuilder::class -> return StringBuilder(strValue(value))
            java.sql.Date::class -> return when (value) {
                is KsonNum -> java.sql.Date(value.data.toLong())
                is KsonString -> parseDate(value.data)?.let { java.sql.Date(it) }
                else -> error("type error: $cls  value: $value")
            }

            java.sql.Time::class -> return when (value) {
                is KsonNum -> java.sql.Time(value.data.toLong())
                is KsonString -> parseTime(value.data)?.let { java.sql.Time(it) }
                else -> error("type error: $cls  value: $value")
            }

            java.sql.Timestamp::class -> return when (value) {
                is KsonNum -> java.sql.Timestamp(value.data.toLong())
                is KsonString -> parseDateTime(value.data)?.let { java.sql.Timestamp(it) }
                else -> error("type error: $cls  value: $value")
            }

            java.util.Date::class -> return when (value) {
                is KsonNum -> java.util.Date(value.data.toLong())
                is KsonString -> parseDate(value.data)?.let { java.util.Date(it) }
                else -> error("type error: $cls  value: $value")
            }

            ByteArray::class -> return when (value) {
                is KsonString -> YsonBlob.decode(value.data)
                is YsonBlob -> value.data
                is KsonArray -> value.toByteArray()
                else -> error("type error: $cls  value: $value")
            }

            BooleanArray::class -> return when (value) {
                is KsonArray -> value.toBoolArray()
                else -> error("type error: $cls  value: $value")
            }

            ShortArray::class -> return when (value) {
                is KsonArray -> value.toShortArray()
                else -> error("type error: $cls  value: $value")
            }

            IntArray::class -> return when (value) {
                is KsonArray -> value.toIntArray()
                else -> error("type error: $cls  value: $value")
            }

            LongArray::class -> return when (value) {
                is KsonArray -> value.toLongArray()
                else -> error("type error: $cls  value: $value")
            }

            FloatArray::class -> return when (value) {
                is KsonArray -> value.toFloatArray()
                else -> error("type error: $cls  value: $value")
            }

            DoubleArray::class -> return when (value) {
                is KsonArray -> value.toDoubleArray()
                else -> error("type error: $cls  value: $value")
            }

            CharArray::class -> return when (value) {
                is KsonArray -> value.toCharArray()
                is KsonString -> value.data.toCharArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<String>::class -> return when (value) {
                is KsonArray -> value.toStringArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Boolean>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as YsonBool).data }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Short>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as KsonNum).data.toShort() }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Int>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as KsonNum).data.toInt() }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Long>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as KsonNum).data.toLong() }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Float>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as KsonNum).data.toFloat() }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }

            Array<Double>::class -> return when (value) {
                is KsonArray -> value.data.map { (it as KsonNum).data.toDouble() }.toTypedArray()
                else -> error("type error: $cls  value: $value")
            }
            //TODO List, Map
        }
        if (cls.java.isArray) {
            value as KsonArray
            val arr = java.lang.reflect.Array.newInstance(cls.java.componentType, value.size)
            val c = cls.java.componentType.kotlin
            var i = 0;
            for (y in value) {
                val v = decodeByClass(y, c, config)
                java.lang.reflect.Array.set(arr, i, v)
                i += 1
            }
            return arr
        }
        val yo: KsonObject = value as? KsonObject ?: throw KsonError("期望是YsonObject")
        val model: Any = cls.createInstance()
        val mems: List<KMutableProperty1<*, *>> = cls.propertiesJSON
        for ((k, v) in yo) {
            val p = mems.find { it.userName == k } ?: continue
            val pt = p.returnType
            val pv = if (pt.isGeneric) {
                decodeByType(v, pt, config)
            } else {
                decodeByClass(v, pt.classifier as KClass<*>, config)
            }
            if (pv != null || pt.isMarkedNullable) {
                p.setter.call(model, pv)
            } else {
                // use default value
            }
        }
        return model
    }

    fun decodeByType(value: KsonValue, ktype: KType, config: KsonDecoderConfig?): Any? {
        val cls = ktype.classifier as KClass<*>
        if (!ktype.isGeneric) {
            return decodeByClass(value, cls, config)
        }
        if (value is KsonNull) {
            return null
        }

        if (cls.java.isArray) {
            throw KsonError("不支持泛型数组Array<T>,请用ArrayList<T>代替")
        }
        val argList = ktype.genericArgs
        val inst = cls.createInstance()
        if (inst is MutableCollection<*>) {
            if (value !is KsonArray) {
                throw KsonError("类型不匹配")
            }
            val addFun = cls.memberFunctions.find { it.name == "add" && it.parameters.size == 2 } ?: throw KsonError("没有add 方法")
            val arg = argList.first()
            val argType = arg.type ?: throw KsonError("type是null")
            val argCls = argType.classifier as KClass<*>
            for (yv in value) {
                val v = if (argType.isGeneric) {
                    decodeByType(yv, argType, config)
                } else {
                    decodeByClass(yv, argCls, config)
                }
                if (v != null || arg.type!!.isMarkedNullable) {
                    addFun.call(inst, v)
                }
            }
        } else if (inst is MutableMap<*, *>) {
            if (value !is KsonObject) {
                throw KsonError("类型不匹配")
            }
            val argKey = argList.first()
            if (argKey.type?.classifier != String::class) {
                throw KsonError("MutableMap的键必须是String")
            }
            val typeVal = argList[1].type ?: throw KsonError("类型不匹配")
            val putFun = cls.memberFunctions.find { it.name == "put" && it.parameters.size == 3 } ?: throw KsonError("没有put 方法")
            for ((key, yv) in value) {
                val v = decodeByType(yv, typeVal, config)
                if (v != null || typeVal.isMarkedNullable) {
                    putFun.call(inst, key, v)
                }
            }
        }
        return inst
    }

    private fun strValue(value: KsonValue): String {
        if (value is KsonString) return value.data
        if (value is YsonBool) return value.data.toString()
        if (value is KsonNum) return value.data.toString()
        if (value is KsonObject) return value.data.toString()
        if (value is KsonArray) return value.data.toString()
        if (value is YsonBlob) return value.encoded
        error("type error:   value: $value")
    }
}

class KsonDecoderConfig {

}

internal val KClass<*>.propertiesJSON: List<KMutableProperty1<*, *>>
    get() {
        return this.memberProperties.filter { it.isPublic && it is KMutableProperty1 && !it.isAbstract && !it.isConst && !it.hasAnnotation<Exclude>() }.map { (it as KMutableProperty1) }
    }
