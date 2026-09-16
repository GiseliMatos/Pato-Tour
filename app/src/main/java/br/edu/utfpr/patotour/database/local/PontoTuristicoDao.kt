package br.edu.utfpr.patotour.database.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.edu.utfpr.patotour.model.PontoTuristico
import kotlinx.coroutines.flow.Flow

@Dao
interface PontoTuristicoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(pontoTuristico: PontoTuristico): Long

    @Query("SELECT * FROM pontos_turisticos ORDER BY nome ASC")
    fun listarTodos(): Flow<List<PontoTuristico>>

    @Query("SELECT * FROM pontos_turisticos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): PontoTuristico?

    @Update
    suspend fun atualizar(pontoTuristico: PontoTuristico): Int

    @Delete
    suspend fun excluir(pontoTuristico: PontoTuristico): Int
}