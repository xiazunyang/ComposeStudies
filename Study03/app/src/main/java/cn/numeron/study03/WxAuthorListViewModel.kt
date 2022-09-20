package cn.numeron.study03

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.numeron.uistate.UIState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
class WxAuthorListViewModel : ViewModel() {

    private val wxAuthorRepository = WxAuthorRepository()

    val snackBarHostState = SnackbarHostState()

    val wxAuthorListFlow = MutableStateFlow(UIState<List<WxAuthor>>())

    private var wxAuthorList by wxAuthorListFlow

    fun dispatch(intent: WxAuthorIntent) {
        when (intent) {
            is WxAuthorIntent.GetList -> getWxAuthorList()
            is WxAuthorIntent.SnackBar -> showSnackBar(intent)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                wxAuthorList = wxAuthorList.toLoading()
                delay(3000)
                throw RuntimeException("test exception.")
            } catch (throwable: Throwable) {
                wxAuthorList = wxAuthorList.toFailure(throwable)
            }
        }
    }

    fun getWxAuthorList() {
        viewModelScope.launch {
            try {
                wxAuthorList = wxAuthorList.toLoading()
                val wxAuthorListValue = wxAuthorRepository.getWxAuthorList {
                    println((it * 100).toInt())
                    wxAuthorList = wxAuthorList.toLoading(it)
                }
                wxAuthorList = wxAuthorList.toSuccess(wxAuthorListValue)
            } catch (throwable: Throwable) {
                wxAuthorList = wxAuthorList.toFailure(throwable)
            }
        }
    }

    fun showSnackBar(intent: WxAuthorIntent.SnackBar) {
        viewModelScope.launch {
            snackBarHostState.showSnackbar(intent)
            wxAuthorList = if (wxAuthorList.hasValue) {
                wxAuthorList.toSuccess(wxAuthorList.value!!)
            } else {
                wxAuthorList.toEmpty()
            }
        }
    }

}

