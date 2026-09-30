package br.edu.utfpr.patotour.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "pontos_turisticos")
@Parcelize
data class PontoTuristico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double,
    val enderecoTextual: String,
    val caminhoImagem: String
) : Parcelable