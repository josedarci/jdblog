package com.josedarci.jdblog

import android.Manifest
import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.josedarci.jdblog.ui.theme.JDBlogTheme

class MainActivity : ComponentActivity() {
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach { entry ->
            val permissionName = entry.key
            val isGranted = entry.value
            if (isGranted) {
                // Permissão foi concedida
                when (permissionName) {
                    Manifest.permission.CAMERA -> {
                        // Faça algo quando a permissão de câmera for concedida
                    }
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        // Faça algo quando as permissões de leitura/escrita forem concedidas
                    }
                }
            } else {
                // Permissão foi negada
                when (permissionName) {
                    Manifest.permission.CAMERA -> {
                        // Informe ao usuário que a permissão de câmera é necessária
                    }
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        // Informe ao usuário que as permissões de leitura/escrita são necessárias
                    }
                }
            }
        }
    }

    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result.resultCode, data))
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JDBlogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewScreen("https://blog.josedarci.com")
                }
            }
        }
        requestPermissions()
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        webView.saveState(outState)
    }

    @Composable
    fun WebViewScreen(url: String) {
        Column(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { context ->
                WebView(context).apply {
                    webView = this
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            // Exibir uma mensagem de erro ou tentar recarregar a página
                            // Exemplo: Toast.makeText(context, "Erro ao carregar a página", Toast.LENGTH_SHORT).show()
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            if (this@MainActivity.filePathCallback != null) {
                                this@MainActivity.filePathCallback?.onReceiveValue(null)
                                this@MainActivity.filePathCallback = null
                            }
                            this@MainActivity.filePathCallback = filePathCallback

                            val intent = fileChooserParams?.createIntent()
                            try {
                                fileChooserLauncher.launch(intent)
                            } catch (e: Exception) {
                                this@MainActivity.filePathCallback = null
                                return false
                            }
                            return true
                        }

                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: android.os.Message?
                        ): Boolean {
                            val newWebView = WebView(view!!.context)
                            newWebView.webViewClient = view.webViewClient
                            newWebView.settings.javaScriptEnabled = true
                            newWebView.settings.domStorageEnabled = true
                            newWebView.settings.javaScriptCanOpenWindowsAutomatically = true
                            newWebView.webChromeClient = this

                            val dialog = android.app.Dialog(this@MainActivity)
                            dialog.setContentView(newWebView)
                            dialog.show()

                            val transport = resultMsg?.obj as WebView.WebViewTransport
                            transport.webView = newWebView
                            resultMsg.sendToTarget()
                            return true
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.safeBrowsingEnabled = true
                    settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    loadUrl(url)
                }
            }, modifier = Modifier.weight(1f))
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun WebViewScreenPreview() {
        JDBlogTheme {
            WebViewScreen("https://blog.josedarci.com")
        }
    }
}
