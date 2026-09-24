 package br.edu.utfpr.patotour
 
 import br.edu.utfpr.patotour.data.local.PontoTuristicoDao
 import br.edu.utfpr.patotour.data.model.PontoTuristico
 import br.edu.utfpr.patotour.data.repository.PontoTuristicoRepositoryImpl
 import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.MutableStateFlow
 import kotlinx.coroutines.runBlocking
 import org.junit.Assert.assertEquals
 import org.junit.Assert.assertNull
 import org.junit.Test
 
 class PontoTuristicoRepositoryTest {
     @Test
     fun crudMantemDadosLocaisSemRede() = runBlocking {
         val dao = FakePontoTuristicoDao()
         val repository = PontoTuristicoRepositoryImpl(dao)
         val original = PontoTuristico(nome = "Parque Nacional", descricao = "Trilha", latitude = -23.55, longitude = -46.63, enderecoTextual = "São Paulo", caminhoImagem = "")
 
         val id = repository.salvar(original)
         assertEquals(1L, id)
         assertEquals(original.copy(id = 1L), repository.buscarPorId(id))
 
         val atualizado = original.copy(id = id, nome = "Parque Atualizado")
         repository.salvar(atualizado)
         assertEquals("Parque Atualizado", repository.buscarPorId(id)?.nome)
 
         repository.excluir(atualizado)
         assertNull(repository.buscarPorId(id))
     }
 
     private class FakePontoTuristicoDao : PontoTuristicoDao {
         private val storage = MutableStateFlow<List<PontoTuristico>>(emptyList())
 
         override suspend fun inserir(pontoTuristico: PontoTuristico): Long {
             val id = (storage.value.maxOfOrNull { it.id } ?: 0L) + 1L
             storage.value = storage.value + pontoTuristico.copy(id = id)
             return id
         }
 
         override fun listarTodos(): Flow<List<PontoTuristico>> = storage
 
         override suspend fun buscarPorId(id: Long): PontoTuristico? = storage.value.firstOrNull { it.id == id }
 
         override suspend fun atualizar(pontoTuristico: PontoTuristico): Int {
             if (storage.value.none { it.id == pontoTuristico.id }) return 0
             storage.value = storage.value.map { if (it.id == pontoTuristico.id) pontoTuristico else it }
             return 1
         }
 
         override suspend fun excluir(pontoTuristico: PontoTuristico): Int {
             val before = storage.value.size
             storage.value = storage.value.filterNot { it.id == pontoTuristico.id }
             return before - storage.value.size
         }
     }
 }
 
