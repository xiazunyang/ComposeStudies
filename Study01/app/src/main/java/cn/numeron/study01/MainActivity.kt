@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class
)

package cn.numeron.study01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
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
            Card(colors = CardDefaults.outlinedCardColors()) {
                ConstraintLayout(constrainSets(), modifier = Modifier.padding(16.dp)) {
                    val softwareKeyboardController = LocalSoftwareKeyboardController.current
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
                            if (loginViewModel.username.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    loginViewModel.dispatch(LoginIntent.ClearUsername)
                                    softwareKeyboardController?.show()
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = if (loginViewModel.password.text.isEmpty()) ImeAction.Next else ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                loginViewModel.dispatch(LoginIntent.Login)
                                softwareKeyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .focusRequester(loginViewModel.usernameFocus)
                            .layoutId("username")
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
                            if (loginViewModel.password.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    softwareKeyboardController?.show()
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                loginViewModel.dispatch(LoginIntent.Login)
                                softwareKeyboardController?.hide()
                            },
                        ),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .focusRequester(loginViewModel.passwordFocus)
                            .layoutId("password")
                    )

                    Text(
                        text = loginViewModel.loginState.event?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .width(0.dp)
                            .layoutId("errorInfo")
                    )

                    AnimatedContent(
                        targetState = loginViewModel.loginState,
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(36.dp)
                            .layoutId("button")
                    ) { loginState ->
                        if (loginState is Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        } else {
                            Button(onClick = {
                                loginViewModel.dispatch(LoginIntent.Login)
                            }) {
                                Text(text = "登录")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun constrainSets(): ConstraintSet {
        return ConstraintSet {
            val username = createRefFor("username")
            val password = createRefFor("password")
            val errorInfo = createRefFor("errorInfo")
            val button = createRefFor("button")

            constrain(password) {
                top.linkTo(username.bottom, margin = 8.dp)
            }

            constrain(errorInfo) {
                top.linkTo(password.bottom)
                start.linkTo(password.start)
                end.linkTo(password.end)
                width = Dimension.fillToConstraints
            }

            constrain(button) {
                top.linkTo(errorInfo.bottom, margin = 4.dp)
                start.linkTo(password.start)
                end.linkTo(password.end)
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
    var usernameFocus = FocusRequester()
    var passwordFocus = FocusRequester()
    var username by mutableStateOf(TextFieldValue())
    var password by mutableStateOf(TextFieldValue())
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
                passwordFocus.requestFocus()
                password = password.copy(selection = TextRange(password.text.length))
            }
            is LoginIntent.ClearUsername -> {
                usernameFocus.requestFocus()
                username = username.copy(text = "")
            }
            is LoginIntent.Login -> {
                var message: String? = null
                if (username.text.isEmpty()) {
                    message = "请输入账号后重试。"
                } else if (username.text.length < 8) {
                    message = "账号长度不得少于8位"
                } else if (password.text.isEmpty()) {
                    message = "请输入密码后重试"
                } else if (password.text.length < 8) {
                    message = "密码长度不得少于8位"
                }
                if (!message.isNullOrEmpty()) {
                    loginState = loginState.postEvent(SnackbarEvent(message))
                    return
                }
                viewModelScope.launch {
                    try {
                        loginState = loginState.withLoading(0f)
                        val accessToken =
                            loginRepository.loginByPassword(username.text, password.text)
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