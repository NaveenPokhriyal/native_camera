package com.root.nativeCamera.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    var showLoading = MutableLiveData<Boolean>().apply { value = false }
    var toastMessage = MutableLiveData<String>()
    var showNoData = MutableLiveData<Boolean>().apply { value = false }
}