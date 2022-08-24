package cn.numeron.study02

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

interface MviIntent

interface EventIntent : MviIntent

data class SnackbarIntent(
    override val message: String,
    override val actionLabel: String = "确定",
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false
) : EventIntent, SnackbarVisuals

data class ToastIntent(

    val message: String,

    val duration: Duration

) : EventIntent