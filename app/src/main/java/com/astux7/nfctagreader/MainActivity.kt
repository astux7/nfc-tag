package com.astux7.nfctagreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.astux7.nfctagreader.ui.theme.NFCTagReaderTheme
import java.nio.charset.StandardCharsets
import java.util.*


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
                    Greeting(mv)
                }
            }
        }

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        val nfc = NfcA.get(tagFromIntent)
        nfc.connect()
        val isConnected = nfc.isConnected()
        mv.setConnected(isConnected)

        if (isConnected) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
                parseNdefMessage(intent)
            }

        } else {
            Log.e("ans", "Not connected")
        }

    }

    // https://stackoverflow.com/questions/59515271/why-android-nfc-reader-adds-en-before-the-message

    private fun parseNdefMessage(intent: Intent) {
        val ndefMessageArray = intent.getParcelableArrayExtra(
            NfcAdapter.EXTRA_NDEF_MESSAGES
        )
        // Test if there is actually a NDef message passed via the Intent
        if (ndefMessageArray != null) {
            val ndefMessage = ndefMessageArray[0] as NdefMessage
            //Get Bytes of payload
            val payloads = ndefMessage.records
            // Read First Byte and then trim off the right length
            var text = ""
            payloads.forEach { record ->
                text += "\n" + parseMultiMessages(record.payload)
            }
            mv.setText(text)
            Log.e("ans", "IS Connected data:" + mv.getText())
        }
    }

    private fun parseMultiMessages(payload: ByteArray): String {
        val textArray: ByteArray =
            Arrays.copyOfRange(payload, payload[0].toInt() + 1, payload.size)
        // Convert to Text
        val text = String(textArray, Charsets.UTF_8)

        Log.e("ans", "IS Connected data:" + text)

        return text
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

    public override fun onPause() {
        super.onPause()
        enableForegroundDispatch(this, this.nfcAdapter)
    }

}


@Composable
fun Greeting( vm: NfcViewModel) {
    Column() {
        Text("NFC scan status:" + vm.isConnected() + "\n")
        if(vm.getText().isNotBlank()) {
            Text("Tag is scanned with Data: ")
            Text(vm.getText())
        }

    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val vm = NfcViewModel()
    NFCTagReaderTheme {
        Greeting(vm)
    }
}