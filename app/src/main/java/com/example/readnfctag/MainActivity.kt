package com.example.readnfctag


import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var mPendingIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        mPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )

        if (!nfcAdapter.isEnabled) {
            tvNfcTag.text = getString(R.string.nfc_error)
        }
    }


    private fun toReversedHex(bytes: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (i in bytes.indices) {
            if (i > 0) {
                stringBuilder.append("")
            }
            val b = bytes[i].toInt() and 0xff
            if (b < 0x10)
                stringBuilder.append('0')
            stringBuilder.append(Integer.toHexString(b))
        }
        return stringBuilder.toString()
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        val nfcA = NfcA.get(tagFromIntent)
        var epc = ""
        try {
            nfcA.connect()
            val result = nfcA.transceive(
                byteArrayOf(
                    0x3A.toByte(), // COMMAND_READ
                    70.toByte(), // page address start
                    72.toByte() // page address end
                )
            )
            epc = toReversedHex(result)
            tvNfcTag.text = epc

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val writeTagFilters = arrayOf(tagDetected)
        nfcAdapter.enableForegroundDispatch(this, mPendingIntent, writeTagFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }



}
