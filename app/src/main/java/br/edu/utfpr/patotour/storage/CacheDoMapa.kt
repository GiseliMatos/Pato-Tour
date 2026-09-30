package br.edu.utfpr.patotour.storage

import android.content.Context
import java.io.File

/** Remove apenas arquivos temporários do aplicativo; pontos, fotos e preferências são preservados. */
object CacheDoMapa {
    fun limpar(context: Context): Boolean {
        val diretorios = buildList {
            add(context.cacheDir)
            add(context.codeCacheDir)
            context.externalCacheDirs.filterNotNull().forEach(::add)
        }.distinct()

        return diretorios.all(::limparDiretorio)
    }

    private fun limparDiretorio(diretorio: File): Boolean {
        if (!diretorio.exists()) return true
        return diretorio.listFiles()?.all { arquivo ->
            if (arquivo.isDirectory) arquivo.deleteRecursively() else arquivo.delete()
        } ?: true
    }
}
