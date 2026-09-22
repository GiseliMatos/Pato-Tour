package br.edu.utfpr.patotour.data.repository

import br.edu.utfpr.patotour.data.model.PontoTuristico
import kotlinx.coroutines.flow.Flow

interface PontoTuristicoRepository {

    val todosPontos: Flow<List<PontoTuristico>>

    suspend fun salvar(ponto: PontoTuristico): Long

    suspend fun buscarPorId(id: Long): PontoTuristico?

    suspend fun excluir(ponto: PontoTuristico)
}