package com.astux7.nfctagreader

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class NfcViewModel : ViewModel() {
    var nfcText: MutableState<String> = mutableStateOf("")

    fun setText(text: String) {
        nfcText.value = text
    }

    fun getText(): String = nfcText.value
}