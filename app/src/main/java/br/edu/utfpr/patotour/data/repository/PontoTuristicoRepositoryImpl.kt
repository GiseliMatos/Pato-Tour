package br.edu.utfpr.patotour.data.repository

import br.edu.utfpr.patotour.data.local.PontoTuristicoDao
import br.edu.utfpr.patotour.data.model.PontoTuristico
import kotlinx.coroutines.flow.Flow

class PontoTuristicoRepositoryImpl(
    private val dao: PontoTuristicoDao
) : PontoTuristicoRepository {

    override val todosPontos: Flow<List<PontoTuristico>> = dao.listarTodos()

    override suspend fun salvar(ponto: PontoTuristico): Long {
        return if (ponto.id == 0L) {
            dao.inserir(ponto)
        } else {
            dao.atualizar(ponto)
            ponto.id
        }
    }

    override suspend fun buscarPorId(id: Long): PontoTuristico? = dao.buscarPorId(id)

    override suspend fun excluir(ponto: PontoTuristico) {
        dao.excluir(ponto)
    }
}