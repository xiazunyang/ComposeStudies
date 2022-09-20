package cn.numeron.study02

import java.lang.reflect.Field
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class MviState<T>(

    /** 数据 */
    open val value: T?,

    /** 事件 */
    override val event: Event? = null

) : EventOwner {

    val hasValue: Boolean
        get() = value != null && hasValue(value)

    fun toSuccess(value: T, event: Event? = null): MviState<T> {
        return Success(value = value, event = event)
    }

    fun toLoading(progress: Float = 0f, event: Event? = null): MviState<T> {
        return Loading(progress = progress, value = value, event = event)
    }

    fun toFailure(
        error: Throwable,
        event: Event = MessageEvent(MviArchitecture.errorTextExtractor.extract(error))
    ): MviState<T> {
        return Failure(error = error, value = value, event = event)
    }

    fun toEmpty(event: Event? = null): MviState<T> {
        return Empty(value = value, event = event)
    }

    fun postEvent(event: Event): MviState<T> {
        return when (this) {
            is Empty -> Empty(value = value, event = event)
            is Success -> Success(value = value, event = event)
            is Failure -> Failure(error = error, value = value, event = event)
            is Loading -> Loading(progress = progress, value = value, event = event)
        }
    }

    final override fun dismantle() {
        eventField.set(this, null)
    }

    companion object {

        private val eventField: Field

        init {
            val mviStateClass = MviState::class.java
            eventField = mviStateClass.getDeclaredField("event")
            eventField.isAccessible = true
        }

        private fun hasValue(value: Any?): Boolean {
            if (value is Iterable<*>) return value.any()
            if (value is Iterator<*>) return value.hasNext()
            return false
        }

        inline operator fun <reified T> invoke(): MviState<T> = Empty()

        inline operator fun <reified T> invoke(value: T): MviState<T> = Success(value)

    }

}

data class Success<T>(

    /** 成功时的数据，一定是非空的 */
    override val value: T,

    override val event: Event? = null

) : MviState<T>(value, event)

data class Failure<T>(

    /** 失败时的异常信息 */
    val error: Throwable,

    override val event: Event,

    /** 失败之前成功的数据 */
    override val value: T? = null

) : MviState<T>(value, null)

data class Loading<T>(

    /** 加载中的进度 */
    val progress: Float,

    /** 加载之前成功的数据 */
    override val value: T? = null,

    override val event: Event? = null

) : MviState<T>(value, event)

data class Empty<T>(

    /** 空数据之前成功的数据 */
    override val value: T? = null,

    override val event: Event? = null

) : MviState<T>(value, event)


