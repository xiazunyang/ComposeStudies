package cn.numeron.study03

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

sealed class WxAuthorIntent {

    object GetList : WxAuthorIntent()

    data class SnackBar(
        override val message: String,
        override val actionLabel: String? = null,
        override val withDismissAction: Boolean = false,
        override val duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
    ) : WxAuthorIntent(), SnackbarVisuals

}
