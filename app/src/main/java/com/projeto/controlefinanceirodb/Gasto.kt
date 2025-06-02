package com.projeto.controlefinanceirodb

data class Gasto(
    val id: Int,
    val tipo: String,
    val descricao: String,
    val valor: Double
)