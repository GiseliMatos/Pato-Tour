package br.edu.utfpr.patotour.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pontos_turisticos")
data class PontoTuristico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double,
    val enderecoTextual: String,
    val caminhoImagem: String
)