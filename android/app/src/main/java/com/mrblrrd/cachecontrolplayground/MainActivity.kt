package com.mrblrrd.cachecontrolplayground

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.EMPTY_REQUEST
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val httpClient by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder().cache(Cache(cacheDir, 1024 * 1024)).build()
    }

    private val executor by lazy(LazyThreadSafetyMode.NONE) {
        Executors.newSingleThreadExecutor()
    }

    private val prefs by lazy(LazyThreadSafetyMode.NONE) {
        getPreferences(MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.getButton).setOnClickListener {
            sendRequest("GET")
        }
        findViewById<Button>(R.id.postButton).setOnClickListener {
            sendRequest("POST")
        }
        findViewById<MultiAutoCompleteTextView>(R.id.cacheControlInput).apply {
            setAdapter(
                ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    arrayOf(
                        "null",
                        "no-store",
                        "no-cache",
                        "max-age=",
                        "max-stale=",
                        "min-fresh=",
                        "no-transform",
                        "only-if-cached",
                        "stale-if-error",
                    )
                )
            )
            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.hostInput).text = prefs.getString("host", null)
        findViewById<TextView>(R.id.cacheControlInput).text = prefs.getString("cache-control", null)
    }

    override fun onPause() {
        super.onPause()
        prefs.edit {
            putString("host", findViewById<TextView>(R.id.hostInput).text.trim().toString())
            putString("cache-control", findViewById<TextView>(R.id.cacheControlInput).text.trim().toString())
        }
    }

    private fun sendRequest(method: String) {
        val host = findViewById<TextView>(R.id.hostInput).text.trim().toString()
        val cacheControl = findViewById<TextView>(R.id.cacheControlInput).text.trim().toString()
        executor.submit {
            val request =
                Request.Builder()
                    .method(method, if (method == "POST") EMPTY_REQUEST else null)
                    .url("http://${host}/helloworld")
                    .apply {
                        if (cacheControl == "null") {
                            removeHeader("Cache-Control")
                        } else {
                            header("Cache-Control", cacheControl)
                        }
                    }
                    .build()
            val response =
                try {
                    httpClient
                        .newCall(request)
                        .execute()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "POST error", e)
                    runOnUiThread {
                        Toast
                            .makeText(
                                this@MainActivity,
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    return@submit
                }
            val responseCode = response.code
            val responseBody = response.body?.string()
            runOnUiThread {
                Toast
                    .makeText(
                        this@MainActivity,
                        "${responseCode}: $responseBody",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }

    companion object {

        private const val LOG_TAG = "MainActivity"
    }
}