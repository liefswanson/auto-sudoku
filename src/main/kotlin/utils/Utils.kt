package utils

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

fun <R> Throwable.multicatch(vararg classes: KClass<*>, block: () -> R): R {
    if (classes.any { this::class.isSubclassOf(it) }) {
        return block()
    } else {
        throw this
    }
}

fun align(vararg lines: String): List<String> {
    val len = lines
            .map { it.length }
            .max() ?: 0

    return lines.map { it.padEnd(len, ' ') }
}