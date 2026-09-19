package br.edu.utfpr.patotour.preferences

import android.content.Context

class ConfiguracoesPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun salvarZoom(zoom: Float) {
        preferences.edit()
            .putFloat(KEY_ZOOM, zoom)
            .apply()
    }

    fun obterZoom(): Float {
        return preferences.getFloat(KEY_ZOOM, DEFAULT_ZOOM)
    }

    fun salvarTipoMapa(tipoMapa: String) {
        preferences.edit()
            .putString(KEY_TIPO_MAPA, tipoMapa)
            .apply()
    }

    fun obterTipoMapa(): String {
        return preferences.getString(
            KEY_TIPO_MAPA,
            DEFAULT_TIPO_MAPA
        ) ?: DEFAULT_TIPO_MAPA
    }

    companion object {
        private const val PREFERENCES_NAME = "configuracoes_pato_tour"

        private const val KEY_ZOOM = "zoom_padrao"
        private const val KEY_TIPO_MAPA = "tipo_mapa"

        private const val DEFAULT_ZOOM = 12f
        private const val DEFAULT_TIPO_MAPA = "Rodoviário"
    }
}