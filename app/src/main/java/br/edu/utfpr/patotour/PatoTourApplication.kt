package br.edu.utfpr.patotour

import android.app.Application
import br.edu.utfpr.patotour.data.database.AppDatabase
import br.edu.utfpr.patotour.data.repository.PontoTuristicoRepository
import br.edu.utfpr.patotour.data.repository.PontoTuristicoRepositoryImpl

class PatoTourApplication : Application() {

    val pontoTuristicoRepository: PontoTuristicoRepository by lazy {
        PontoTuristicoRepositoryImpl(AppDatabase.getInstance(this).pontoTuristicoDao())
    }
}
