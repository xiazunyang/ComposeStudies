@file:OptIn(ExperimentalMaterial3Api::class)

package cn.numeron.study03

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.numeron.study03.ui.theme.Study03Theme
import cn.numeron.uistate.Empty
import cn.numeron.uistate.Failure
import cn.numeron.uistate.Loading
import cn.numeron.uistate.Success
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Study03Theme {
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
            snackbarHost = {
                SnackbarHost(hostState = wxAuthorListViewModel.snackBarHostState)
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "公众号作者列表",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                )
            },
        ) {
            val uiState by wxAuthorListViewModel.wxAuthorListFlow.collectAsState()
            SwipeRefresh(
                state = SwipeRefreshState(uiState.hasValue && uiState is Loading),
                onRefresh = wxAuthorListViewModel::refresh,
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
                StateUI(
                    uiState = uiState,
                    loadAction = wxAuthorListViewModel::getWxAuthorList
                ) { list ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        list.forEach { wxAuthor ->
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
                                )
                                Divider()
                            }
                        }
                    }
                    when (val state = uiState) {
                        is Empty -> {
                            val intent = WxAuthorIntent.SnackBar(state.message, "确定")
                            wxAuthorListViewModel.dispatch(intent)
                        }
                        is Failure -> {
                            val intent = WxAuthorIntent.SnackBar(state.message, "确定")
                            wxAuthorListViewModel.dispatch(intent)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

}

