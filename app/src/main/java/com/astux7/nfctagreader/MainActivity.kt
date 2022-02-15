package com.astux7.nfctagreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.astux7.nfctagreader.ui.theme.NFCTagReaderTheme


// https://www.excellarate.com/blogs/reading-nfc-tags-with-android-kotlin/


class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var mv: NfcViewModel = NfcViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NFCTagReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android", mv.getText())
                }
            }
        }

        Log.e("ans", "I AM HERE")

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val nfc = NfcA.get(tagFromIntent)
        nfc.connect()
        val isConnected= nfc.isConnected()

        if(isConnected)
        {
            val action2 = intent?.action
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == action2) {
                val parcelables = intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                with(parcelables) {
                    val inNdefMessage = this?.get(0) as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    val ndefRecord_0 = inNdefRecords[0]

                    val inMessage = String(ndefRecord_0.payload)
                    // hack not sure where en comes
                    mv.setText(inMessage.drop(3))
                    Log.e("ans", "IS data1" + mv.getText())
                }
            }

            Log.e("ans", "IS connected else")

        } else{
        Log.e("ans", "Not connected")
    }

    }

    private fun enableForegroundDispatch(activity: ComponentActivity, adapter: NfcAdapter?) {
        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)
        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()
        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType("text/plain")
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException(ex)
            }
        }
        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }
    override fun onResume() {
        super.onResume()
        enableForegroundDispatch(this, this.nfcAdapter)
    }
}



@Composable
fun Greeting(name: String, text: String) {
    Column() {
        Text(text = "Hello $name!")

        Text("Tag is scanned with Data: ")
        Text(text)
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NFCTagReaderTheme {
        Greeting("Android","")
    }
}