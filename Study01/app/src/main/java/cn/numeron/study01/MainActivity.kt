@file:OptIn(ExperimentalMaterial3Api::class)

package cn.numeron.study01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }

}

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel()
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = loginViewModel.username,
                singleLine = true,
                label = { Text(text = "账号") },
                placeholder = { Text(text = "邮箱、手机号码或账号") },
                isError = loginViewModel.loginState.event?.message?.contains("账号") == true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (loginViewModel.username.isNotEmpty()) {
                        IconButton(onClick = {
                            loginViewModel.dispatch(LoginIntent.ClearUsername)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
                onValueChange = {
                    loginViewModel.username = it
                    loginViewModel.loginState.dismantle()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            val passwordVisualTransformation = if (loginViewModel.passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation()
            TextField(
                value = loginViewModel.password,
                singleLine = true,
                visualTransformation = passwordVisualTransformation,
                isError = loginViewModel.loginState.event?.message?.contains("密码") == true,
                label = { Text(text = "密码") },
                onValueChange = {
                    loginViewModel.password = it
                    loginViewModel.loginState.dismantle()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (loginViewModel.password.isNotEmpty()) {
                        IconButton(onClick = {
                            loginViewModel.dispatch(LoginIntent.PasswordVisibleChanged)
                        }) {
                            val iconResId = if (loginViewModel.passwordVisible)
                                R.drawable.ic_baseline_visibility_off_24
                            else R.drawable.ic_baseline_visibility_24
                            Icon(
                                painter = painterResource(id = iconResId),
                                contentDescription = null,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                text = loginViewModel.loginState.event?.message ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(48.dp)) {
                if (loginViewModel.loginState is Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = { loginViewModel.dispatch(LoginIntent.Login) }) {
                        Text(text = "登录")
                    }
                }
            }
        }
    }
}

sealed class LoginIntent : MviIntent {

    object PasswordVisibleChanged : LoginIntent()
    object ClearUsername : LoginIntent()
    object Login : LoginIntent()

}

data class AccessToken(val token: String)

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var loginState by mutableStateOf(MviState<AccessToken>())

    private val loginRepository = LoginRepository()

    fun dispatch(intent: MviIntent) {
        if (intent is DismantleIntent) {
            loginState.dismantle()
        }
        when (intent) {
            is LoginIntent.PasswordVisibleChanged -> {
                passwordVisible = !passwordVisible
            }
            is LoginIntent.ClearUsername -> {
                username = ""
            }
            is LoginIntent.Login -> {
                var message: String? = null
                if (username.isEmpty()) {
                    message = "请输入账号后重试。"
                } else if (username.length < 8) {
                    message = "账号长度不得少于8位"
                } else if (password.isEmpty()) {
                    message = "请输入密码后重试"
                } else if (password.length < 8) {
                    message = "密码长度不得少于8位"
                }
                if (!message.isNullOrEmpty()) {
                    loginState = loginState.postEvent(SnackbarEvent(message))
                    return
                }
                viewModelScope.launch {
                    try {
                        loginState = loginState.withLoading(0f)
                        val accessToken = loginRepository.loginByPassword(username, password)
                        loginState = loginState.withSuccess(accessToken)
                    } catch (throwable: Throwable) {
                        loginState = loginState.withFailure(throwable)
                    }
                }
            }
        }
    }
}

class LoginRepository {

    suspend fun loginByPassword(username: String, password: String): AccessToken {
        delay(3000)
        return AccessToken(username + password)
    }

}