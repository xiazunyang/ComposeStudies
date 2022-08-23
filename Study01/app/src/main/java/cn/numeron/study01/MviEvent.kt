package cn.numeron.study01

enum class Duration {
    Short,
    Long,
    Indefinite
}

sealed class Event(
    /** 消息文本 */
    open val message: String,
    /** 持续时间 */
    open val duration: Duration?
)

data class ToastEvent(
    /** 消息文本 */
    override val message: String,
    /** 持续时间 */
    override val duration: Duration
) : Event(message, duration)

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

data class MessageEvent(
    /** 消息文本 */
    override val message: String
) : Event(message, null)