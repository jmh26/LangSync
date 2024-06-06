package com.example.langsync.model

data class Mensaje(
    val de: String = "",
    val para: String = "",
    val mensaje: String = "",
    val timestamp: Long = 0
)
