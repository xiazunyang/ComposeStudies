@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package cn.numeron.study02

import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class MviViewModel<T>(state: MviState<T>) : ViewModel() {

    val snackbarHostState = SnackbarHostState()

    var state: MviState<T> by mutableStateOf(state)
        protected set

    @CallSuper
    open fun dispatch(intent: MviIntent) {
        when (intent) {
            is EventIntent -> {
                dispatchEventIntent(intent)
            }
        }
    }

    open fun dispatchEventIntent(intent: EventIntent) {
        when (intent) {
            is SnackbarIntent -> handle(intent)
            is ToastIntent -> handle(intent)
        }
    }

    open fun handle(intent: SnackbarIntent) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(intent)
            state.dismantle()
        }
    }

    open fun handle(intent: ToastIntent) {
        Toast.makeText(ComposeApplication(), intent.message, intent.duration.toast).show()
    }

    protected inline fun withState(reconstruction: (MviState<T>) -> MviState<T>) {
        state = reconstruction(state)
    }

}