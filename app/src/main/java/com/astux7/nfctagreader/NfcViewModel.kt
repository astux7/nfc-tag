package com.astux7.nfctagreader

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NfcViewModel : ViewModel() {
    var nfcText: MutableState<String> = mutableStateOf("")
    var nfcConnected: MutableState<Boolean> = mutableStateOf(false)

    fun setText(text: String) {
        nfcText.value = text
    }

    fun getText(): String = nfcText.value

    fun isConnected(): Boolean = nfcConnected.value

    fun setConnected(isOn: Boolean) {
        nfcConnected.value = isOn
    }
}