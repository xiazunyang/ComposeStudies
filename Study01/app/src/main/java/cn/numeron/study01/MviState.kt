package cn.numeron.study01

sealed class MviState<T>(

    /** 数据 */
    open val value: T?,

    /** 事件 */
    open var event: Event? = null

) {

    fun withSuccess(value: T, event: Event? = null): MviState<T> {
        return Success(value = value, event = event)
    }

    fun withLoading(progress: Float, event: Event? = null): MviState<T> {
        return Loading(progress = progress, value = value, event = event)
    }

    fun withFailure(error: Throwable, event: Event? = null): MviState<T> {
        return Failure(error = error, value = value, event = event)
    }

    fun withEmpty(event: Event? = null): MviState<T> {
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

    fun dismantle() {
        event = null
    }

    companion object {

        inline operator fun <reified T> invoke(): MviState<T> {
            return Empty()
        }

        inline operator fun <reified T> invoke(value: T): MviState<T> {
            return Success(value)
        }

    }

}

data class Success<T>(

    /** 成功时的数据，一定是非空的 */
    override val value: T,

    override var event: Event? = null

) : MviState<T>(value, event)

data class Failure<T>(

    /** 失败时的异常信息 */
    val error: Throwable,

    /** 失败之前成功的数据 */
    override val value: T? = null,

    override var event: Event? = null

) : MviState<T>(value, event)

data class Loading<T>(

    /** 加载中的进度 */
    val progress: Float,

    /** 加载之前成功的数据 */
    override val value: T? = null,

    override var event: Event? = null

) : MviState<T>(value, event)

data class Empty<T>(

    /** 空数据之前成功的数据 */
    override val value: T? = null,

    override var event: Event? = null

) : MviState<T>(value, event)


