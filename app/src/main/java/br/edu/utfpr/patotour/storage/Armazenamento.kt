package br.edu.utfpr.patotour.storage

import android.content.Context
import java.io.File
import java.util.Locale

object Armazenamento {

    fun obterTamanhoUtilizado(context: Context): String {
        var tamanho = 0L

        val diretorioInterno = File(context.applicationInfo.dataDir)
        tamanho += calcularTamanho(diretorioInterno)

        context.getExternalFilesDir(null)?.let {
            tamanho += calcularTamanho(it)
        }

        return formatarTamanho(tamanho)
    }

    private fun calcularTamanho(arquivo: File): Long {
        if (!arquivo.exists()) {
            return 0L
        }

        if (arquivo.isFile) {
            return arquivo.length()
        }

        return arquivo.listFiles()?.sumOf {
            calcularTamanho(it)
        } ?: 0L
    }

    private fun formatarTamanho(bytes: Long): String {
        if (bytes < 1024) {
            return "$bytes B"
        }

        val kb = bytes / 1024.0

        if (kb < 1024) {
            return String.format(
                Locale.getDefault(),
                "%.1f KB",
                kb
            )
        }

        val mb = kb / 1024.0

        return String.format(
            Locale.getDefault(),
            "%.1f MB",
            mb
        )
    }
}