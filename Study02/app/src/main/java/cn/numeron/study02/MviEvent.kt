package cn.numeron.study02

import android.widget.Toast

enum class Duration {

    Short,

    Long,

    Indefinite;

    val toast: Int
        get() = when (this) {
            Short -> Toast.LENGTH_SHORT
            Long -> Toast.LENGTH_LONG
            Indefinite -> TODO()
        }

}

/** 事件的基类，实现类有[ToastEvent]、[SnackbarEvent]以及[MessageEvent] */
sealed class Event(

    /** 消息文本 */
    open val message: String,

    /** 持续时间 */
    open val duration: Duration?

)

/** Toast事件，指需要用Toast消耗此事件 */
data class ToastEvent(

    /** 消息文本 */
    override val message: String,

    /** 持续时间 */
    override val duration: Duration

) : Event(message, duration)

/** Snackbar事件，指需要用Snackbar消耗此事件 */
data class SnackbarEvent(

    /** Snackbar消息文本 */
    override val message: String,

    /** 持续时间 */
    override val duration: Duration = Duration.Indefinite,

    /** 按钮文本 */
    val action: String? = null,

    /** 点击按钮时的意图 */
    val intent: MviIntent? = null
) : Event(message, duration)

/** 一个消息事件，是指需要在UI中展示一段文本的事件 */
data class MessageEvent(

    /** 消息文本 */
    override val message: String

) : Event(message, null)


interface EventOwner {

    val event: Event?

    fun dismantle()

}