@file:OptIn(ExperimentalMaterial3Api::class)

package cn.numeron.study02

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.numeron.study02.ui.theme.Study02Theme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {

    init {
        MviArchitecture.errorTextExtractor = MviErrorTextExtractor {
            it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Study02Theme {
                WxAuthorListScreen()
            }
        }
    }

    @Composable
    fun WxAuthorListScreen(
        wxAuthorListViewModel: WxAuthorListViewModel = viewModel()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "公众号列表",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = wxAuthorListViewModel.snackbarHostState,
                )
            }
        ) {
            val state = wxAuthorListViewModel.state
            SwipeRefresh(
                state = SwipeRefreshState(state is Loading && state.hasValue),
                onRefresh = { wxAuthorListViewModel.dispatch(WxAuthorIntent.GetWxAuthorList) },
                indicator = { s, trigger ->
                    SwipeRefreshIndicator(
                        state = s,
                        refreshTriggerDistance = trigger,
                        contentColor = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding(),
                    )
            ) {
                if (state.hasValue) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        state.value?.forEach { wxAuthor ->
                            item(key = wxAuthor.id) {
                                ListItem(
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondaryContainer,
                                        )
                                    },
                                    headlineText = {
                                        Text(text = wxAuthor.name)
                                    },
                                    modifier = Modifier.clickable {
                                        wxAuthorListViewModel.dispatch(SnackbarIntent(message = wxAuthor.name))
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
                when {
                    state is Empty -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(ScrollState(0))
                        ) {
                            Text(text = state.event?.message ?: "没有数据")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                wxAuthorListViewModel.dispatch(WxAuthorIntent.GetWxAuthorList)
                            }) {
                                Text(text = "获取数据")
                            }
                        }
                    }
                    state is Failure -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(ScrollState(0))
                        ) {
                            Icon(imageVector = Icons.Filled.Warning, contentDescription = null)
                            Text(text = state.event.message)
                        }
                    }
                    state is Loading && !state.hasValue -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                            Text(text = "正在加载，请稍候...")
                        }
                    }
                }
            }
        }
    }

}

sealed class WxAuthorIntent : MviIntent {

    object GetWxAuthorList : WxAuthorIntent()

}

data class WxAuthor(val id: Long, val name: String) {
    override fun toString(): String = name
}

class WxAuthorListViewModel : MviViewModel<List<WxAuthor>>(MviState()) {

    private val repository = WxAuthorListRepository()

    override fun dispatch(intent: MviIntent) {
        super.dispatch(intent)
        when (intent) {
            is WxAuthorIntent.GetWxAuthorList -> getWxAuthorList()
        }
    }

    private fun getWxAuthorList() {
        viewModelScope.launch {
            state = try {
                withState(MviState<List<WxAuthor>>::toLoading)
                val wxAuthorList = repository.getWxAuthorList()
                if (wxAuthorList.isEmpty()) {
                    state.toEmpty()
                } else {
                    state.toSuccess(wxAuthorList)
                }
            } catch (throwable: Throwable) {
                state.toFailure(throwable)
            }
        }
    }

}

class WxAuthorListRepository {

    suspend fun getWxAuthorList(): List<WxAuthor> {
        return withContext(Dispatchers.IO) {
            val wxAuthorJson = getWxAuthorJson()
            deserializer(wxAuthorJson)
        }
    }

    private suspend fun getWxAuthorJson(): String {
        return suspendCancellableCoroutine {
            val url = URL("https://wanandroid.com/wxarticle/chapters/json")
            val httpUrlConnection = url.openConnection() as HttpURLConnection
            try {
                httpUrlConnection.requestMethod = "GET"
                httpUrlConnection.connectTimeout = 8000
                httpUrlConnection.readTimeout = 8000
                httpUrlConnection.connect()

                val responseCode = httpUrlConnection.responseCode
                if (responseCode in 200..299) {
                    val json = httpUrlConnection
                        .inputStream
                        .bufferedReader()
                        .use(BufferedReader::readText)
                    it.resume(json)
                } else {
                    val json = httpUrlConnection
                        .errorStream
                        .bufferedReader()
                        .use(BufferedReader::readText)
                    throw RuntimeException(json)
                }
            } catch (throwable: Throwable) {
                it.resumeWithException(throwable)
            } finally {
                httpUrlConnection.disconnect()
            }
        }
    }

    private suspend fun deserializer(json: String): List<WxAuthor> {
        return suspendCoroutine { continuation ->
            try {
                val list = mutableListOf<WxAuthor>()
                val jsonObject = JSONObject(json)
                val data = jsonObject.getJSONArray("data")
                repeat(data.length()) { index ->
                    val wxAuthorJsonObject = data.getJSONObject(index)
                    val id = wxAuthorJsonObject.getLong("id")
                    val name = wxAuthorJsonObject.getString("name")
                    list.add(WxAuthor(id, name))
                }
                continuation.resume(list)
            } catch (throwable: Throwable) {
                continuation.resumeWithException(throwable)
            }
        }
    }

}