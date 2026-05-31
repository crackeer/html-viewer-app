package com.example.htmlbrowser.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.htmlbrowser.databinding.ActivityHtmlViewerBinding
import com.example.htmlbrowser.ui.viewmodels.HtmlViewerViewModel
import com.example.htmlbrowser.utils.LocalHttpServer
import kotlinx.coroutines.launch
import java.io.File

class HtmlViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHtmlViewerBinding
    private lateinit var viewModel: HtmlViewerViewModel
    private var httpServer: LocalHttpServer? = null
    private var currentArchivePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val archiveId = intent.getStringExtra("archive_id")
        if (archiveId == null) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[HtmlViewerViewModel::class.java]

        setupWebView()
        setupBackNavigation()
        observeViewModel()

        viewModel.loadArchive(archiveId)
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        binding.webView.webViewClient = LocalWebViewClient()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun startHttpServer(archivePath: String) {
        stopHttpServer()
        try {
            val port = LocalHttpServer.findFreePort()
            httpServer = LocalHttpServer(port, File(archivePath))
            httpServer?.start()
            currentArchivePath = archivePath
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to start HTTP server: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun stopHttpServer() {
        httpServer?.stop()
        httpServer = null
        currentArchivePath = null
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.archive.collect { archive ->
                if (archive != null) {
                    supportActionBar?.title = archive.title
                    startHttpServer(archive.extractPath)
                    val port = httpServer?.listeningPort ?: 8080
                    binding.webView.loadUrl("http://localhost:$port/index.html")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (message != null) {
                    Toast.makeText(this@HtmlViewerActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        stopHttpServer()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.loadingIndicator.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.loadingIndicator.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) {
                Toast.makeText(
                    this@HtmlViewerActivity,
                    "Failed to load HTML content",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}