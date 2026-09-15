package br.edu.utfpr.patotour.repository

import br.edu.utfpr.patotour.database.local.PontoTuristicoDao
import br.edu.utfpr.patotour.model.PontoTuristico
import kotlinx.coroutines.flow.Flow

class PontoTuristicoRepository(private val dao: PontoTuristicoDao) {

    val todosPontos: Flow<List<PontoTuristico>> = dao.listarTodos()

    suspend fun salvar(ponto: PontoTuristico): Long {
        return if (ponto.id == 0L) {
            dao.inserir(ponto)
        } else {
            dao.atualizar(ponto)
            ponto.id
        }
    }

    suspend fun buscarPorId(id: Long): PontoTuristico? = dao.buscarPorId(id)

    suspend fun deletar(ponto: PontoTuristico) = dao.excluir(ponto)
}