package br.edu.utfpr.patotour.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.patotour.data.model.PontoTuristico
import br.edu.utfpr.patotour.data.repository.PontoTuristicoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PontoTuristicoViewModel(
    private val repository: PontoTuristicoRepository
) : ViewModel() {

    val points: StateFlow<List<PontoTuristico>> = repository.todosPontos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun salvar(ponto: PontoTuristico) {
        viewModelScope.launch {
            repository.salvar(ponto)
        }
    }
}