package com.example.htmlbrowser.utils

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class LocalHttpServer(
    private val port: Int,
    private val rootDirectory: File
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.removePrefix("/")
        val file = if (uri.isEmpty() || uri == "index.html") {
            File(rootDirectory, "index.html")
        } else {
            File(rootDirectory, uri)
        }

        return when {
            !file.exists() -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_HTML,
                "<html><body><h1>404 - File Not Found</h1></body></html>"
            )
            file.isDirectory -> {
                val indexFile = File(file, "index.html")
                if (indexFile.exists()) {
                    serveFile(indexFile)
                } else {
                    newFixedLengthResponse(
                        Response.Status.FORBIDDEN,
                        MIME_HTML,
                        "<html><body><h1>403 - Directory Listing Not Allowed</h1></body></html>"
                    )
                }
            }
            else -> serveFile(file)
        }
    }

    private fun serveFile(file: File): Response {
        val mime = getMimeType(file.name)
        return try {
            val fis = FileInputStream(file)
            newFixedLengthResponse(
                Response.Status.OK,
                mime,
                fis,
                file.length()
            )
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_HTML,
                "<html><body><h1>500 - Internal Server Error</h1></body></html>"
            )
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "html", "htm" -> MIME_HTML
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "eot" -> "application/vnd.ms-fontobject"
            "otf" -> "font/otf"
            "xml" -> "application/xml"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    companion object {
        fun findFreePort(): Int {
            return 8080
        }
    }
}