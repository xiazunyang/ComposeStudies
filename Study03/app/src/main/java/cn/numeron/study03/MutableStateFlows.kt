@file:Suppress("NOTHING_TO_INLINE")

package cn.numeron.study03

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KProperty

inline operator fun <T> MutableStateFlow<T>.setValue(ref: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

inline operator fun <T> MutableStateFlow<T>.getValue(ref: Any?, property: KProperty<*>): T {
    return value
}