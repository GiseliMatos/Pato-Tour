package br.edu.utfpr.patotour.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

fun salvarImagemLocal(context: Context, origem: Uri): String? = runCatching {
    val pasta = File(context.filesDir, "fotos").apply { mkdirs() }
    val destino = File(pasta, "${UUID.randomUUID()}.jpg")
    context.contentResolver.openInputStream(origem)?.use { input ->
        destino.outputStream().use { output -> input.copyTo(output) }
    } ?: return null
    Uri.fromFile(destino).toString()
}.getOrNull()