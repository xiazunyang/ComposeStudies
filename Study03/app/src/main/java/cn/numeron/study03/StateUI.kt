package cn.numeron.study03

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.numeron.uistate.Empty
import cn.numeron.uistate.Failure
import cn.numeron.uistate.Loading
import cn.numeron.uistate.UIState

@Composable
fun <T> StateUI(
    uiState: UIState<T>,
    loadAction: () -> Unit = {},
    Empty: @Composable (String) -> Unit = { message ->
        EmptyUI(message, loadAction)
    },
    Failure: @Composable (String, Throwable) -> Unit = { message, cause ->
        FailureUI(message, cause, loadAction)
    },
    Loading: @Composable (String, Float) -> Unit = { message, progress ->
        LoadingUI(message, progress)
    },
    Content: @Composable (value: T) -> Unit
) {
    if (uiState.hasValue) {
        Content(uiState.value!!)
    } else when (uiState) {
        is Empty -> Empty(uiState.message)
        is Failure -> Failure(uiState.message, uiState.cause)
        is Loading -> Loading(uiState.message, uiState.progress)
        else -> Unit
    }
}

@Composable
fun EmptyUI(message: String, loadAction: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0))
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = loadAction) {
            Text(text = "load")
        }
    }
}

@Composable
fun FailureUI(message: String, cause: Throwable, loadAction: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0))
    ) {
        Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
        Text(text = message)
        Button(onClick = loadAction) {
            Text(text = "load")
        }
    }
}

@Composable
fun LoadingUI(message: String, progress: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (progress < 0f) {
            CircularProgressIndicator()
        } else {
            LinearProgressIndicator(progress)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message)
    }
}