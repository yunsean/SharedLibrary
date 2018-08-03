package com.yunsean.dynkotlins.data

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface Codified<out T : Serializable> {
    val code: T
    object Enums {
        private val enumCodesByClass = ConcurrentHashMap<Class<*>, Map<Serializable, Enum<*>>>()

        inline fun <reified T, TCode : Serializable> decode(code: TCode): T where T : Codified<TCode>, T : Enum<*> {
            return decode(T::class.java, code)
        }
        fun <T, TCode : Serializable> decode(enumClass: Class<T>, code: TCode): T where T : Codified<TCode> {
            return tryDecode(enumClass, code) ?: throw IllegalArgumentException("No $enumClass value with code == $code")
        }
        inline fun <reified T, TCode : Serializable> tryDecode(code: TCode): T? where T : Codified<TCode> {
            return tryDecode(T::class.java, code)
        }
        @Suppress("UNCHECKED_CAST")
        fun <T, TCode : Serializable> tryDecode(enumClass: Class<T>, code: TCode?): T? where T : Codified<TCode> {
            if (code == null) return null
            val valuesForEnumClass = enumCodesByClass.getOrPut(enumClass as Class<Enum<*>>, {
                enumClass.enumConstants.associateBy { (it as T).code }
            })
            return valuesForEnumClass[code] as T?
        }
    }
}

fun <T, TCode> KClass<T>.decode(code: TCode): T
        where T : Codified<TCode>, T : Enum<T>, TCode : Serializable
        = Codified.Enums.decode(java, code)

fun <T, TCode> KClass<T>.tryDecode(code: TCode?): T?
        where T : Codified<TCode>, T : Enum<T>, TCode : Serializable
        = Codified.Enums.tryDecode(java, code)